package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void shouldAddAndGetFilmById() {
        Film film = new Film();
        film.setName("Film name");
        film.setDescription("Film Description");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(111);
        film.setMpa(new Mpa(1, null));
        film.setGenres(List.of(new Genre(1, null), new Genre(2, null)));

        Film created = filmStorage.addFilm(film);
        Optional<Film> testFilm = filmStorage.findFilmById(created.getId());

        assertThat(testFilm).isPresent().hasValueSatisfying(f -> {
            assertThat(f.getId()).isEqualTo(created.getId());
            assertThat(f.getName()).isEqualTo("Film name");
            assertThat(f.getDescription()).isEqualTo("Film Description");
            assertThat(f.getReleaseDate()).isEqualTo(LocalDate.of(2014, 11, 7));
            assertThat(f.getDuration()).isEqualTo(111);
            assertThat(f.getMpa().getId()).isEqualTo(1);
            assertThat(f.getGenres()).isEqualTo(List.of(new Genre(1, null), new Genre(2, null)));
        });
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Film old name");
        film.setDescription("Film old Description");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(111);
        film.setMpa(new Mpa(1, null));
        film.setGenres(List.of(new Genre(1, null), new Genre(2, null)));

        Film created = filmStorage.addFilm(film);

        created.setName("Film new name");
        created.setDescription("Film new Description");
        created.setDuration(222);
        created.setMpa(new Mpa(3, null));
        created.setGenres(List.of(new Genre(4, null), new Genre(5, null)));

        filmStorage.updateFilm(created);

        Optional<Film> updated = filmStorage.findFilmById(created.getId());
        assertThat(updated).isPresent().hasValueSatisfying(f -> {
            assertThat(f.getName()).isEqualTo("Film new name");
            assertThat(f.getDescription()).isEqualTo("Film new Description");
            assertThat(f.getDuration()).isEqualTo(222);
            assertThat(f.getMpa().getId()).isEqualTo(3);
            assertThat(f.getGenres()).isEqualTo(List.of(new Genre(4, null), new Genre(5, null)));
        });
    }

    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Film A");
        film1.setDescription("A");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        film1.setMpa(new Mpa(1, null));

        Film film2 = new Film();
        film2.setName("Film B");
        film2.setDescription("B");
        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
        film2.setDuration(120);
        film2.setMpa(new Mpa(2, null));

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        Collection<Film> allFilms = filmStorage.getFilms();

        assertThat(allFilms)
                .hasSize(2)
                .anySatisfy(f -> assertThat(f.getName()).isEqualTo("Film A"))
                .anySatisfy(f -> assertThat(f.getName()).isEqualTo("Film B"));
    }

    @Test
    void shouldReturnCommonFilmsWithFriendSortedByPopularity() {
        // Создаем пользователей
        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        userStorage.addUser(user2);

        User user3 = new User();
        user3.setName("User3");
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setBirthday(LocalDate.of(1997, 1, 1));
        userStorage.addUser(user3);

        // Создаем фильмы (популярность = количество лайков)
        Film film1 = new Film();
        film1.setName("Film A");
        film1.setDescription("Description A");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        film1.setMpa(new Mpa(1, "G"));
        filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film B");
        film2.setDescription("Description B");
        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
        film2.setDuration(120);
        film2.setMpa(new Mpa(2, "PG"));
        filmStorage.addFilm(film2);

        Film film3 = new Film();
        film3.setName("Film C");
        film3.setDescription("Description C");
        film3.setReleaseDate(LocalDate.of(2020, 1, 1));
        film3.setDuration(90);
        film3.setMpa(new Mpa(3, "PG-13"));
        filmStorage.addFilm(film3);

        // Пользователи ставят лайки (определяем популярность)
        // film1: 2 лайка (самый популярный)
        // film2: 1 лайк
        // film3: 0 лайков (не должен попасть в общие)
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user3.getId());

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        filmStorage.addLike(film3.getId(), user1.getId());

        // Вызываем метод, который тестируем
        List<Film> commonFilms = filmStorage.getCommonFilmsWithFriend(user1.getId(), user2.getId());

        // Проверяем:
        // 1) Возвращаются 2 общих фильма (film1 и film2)
        // 2) Сортировка по популярности: film1 (3 лайка) -> film2 (2 лайка)
        assertThat(commonFilms)
                .hasSize(2) // film1 и film2 общие
                .extracting(Film::getName)
                .containsExactly("Film A", "Film B"); // film1 первый, film2 второй
    }
}
