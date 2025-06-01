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
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // Вспомогательный метод для создания пользователя в тестовой БД
    private void createTestUser(int id) {
        String sql = "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, "user" + id + "@test.com", "login" + id, "User " + id, LocalDate.of(1990,1,1));
    }

    private User createUser(String name, String email, String login, LocalDate birthday) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(birthday);
        return userStorage.addUser(user);
    }

    private Film createFilm(String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        return filmStorage.addFilm(film);
    }

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
        User user1 = createUser("User1", "user1@example.com", "user1",
                LocalDate.of(1990, 1, 1));
        User user2 = createUser("User2", "user2@example.com", "user2",
                LocalDate.of(1995, 1, 1));
        User user3 = createUser("User3", "user3@example.com", "user3",
                LocalDate.of(1997, 1, 1));

        // Создаем фильмы
        Film film1 = createFilm("Film A", "Description A",
                LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film film2 = createFilm("Film B", "Description B",
                LocalDate.of(2010, 1, 1), 120, new Mpa(2, "PG"));
        Film film3 = createFilm("Film C", "Description C",
                LocalDate.of(2020, 1, 1), 90, new Mpa(3, "PG-13"));



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

    @Test
    void shouldReturnRecommendedFilmsBasedOnSimilarUsers() {
        // Создаем пользователей
        User user1 = createUser("User1", "user1@example.com", "user1", LocalDate.of(1990, 1, 1));
        User user2 = createUser("User2", "user2@example.com", "user2", LocalDate.of(1995, 1, 1));
        User user3 = createUser("User3", "user3@example.com", "user3", LocalDate.of(1997, 1, 1));

        // Создаем фильмы
        Film film1 = createFilm("Film A", "Description A", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film film2 = createFilm("Film B", "Description B", LocalDate.of(2010, 1, 1), 120, new Mpa(2, "PG"));
        Film film3 = createFilm("Film C", "Description C", LocalDate.of(2020, 1, 1), 90, new Mpa(3, "PG-13"));
        Film film4 = createFilm("Film D", "Description D", LocalDate.of(2020, 1, 1), 90, new Mpa(3, "PG-13"));


        // Пользователи ставят лайки:
        // - user1 лайкнул film1 и film2
        // - user2 лайкнул film1 и film3 (похож на user1)
        // - user3 лайкнул film4 (не похож на user1)
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user2.getId());

        filmStorage.addLike(film4.getId(), user3.getId());

        // Получаем рекомендации для user1 (должен получить film3, так как его лайкнул user2, у которого схожие вкусы)
        List<Film> recommendations = filmStorage.getRecommendations(user1.getId());

        // Проверяем:
        // 1) Рекомендуется film3 (так как user2 лайкнул film1 и film3, а user1 не лайкнул film3)
        // 2) Не рекомендуются film1 и film2 (user1 их уже лайкнул)
        assertThat(recommendations)
                .hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Film C");
    }

    @Test
    void shouldFindMostPopularFilms() {
        // Создаем пользователей, чтобы лайки ссылались на существующих
        createTestUser(1);
        createTestUser(2);
        createTestUser(3);

        // Создаем фильмы
        Film film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2010, 1, 1));
        film1.setDuration(100);
        film1.setMpa(new Mpa(1, null));
        film1.setGenres(List.of(new Genre(1, null)));

        Film film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2011, 1, 1));
        film2.setDuration(110);
        film2.setMpa(new Mpa(2, null));
        film2.setGenres(List.of(new Genre(2, null)));

        Film film3 = new Film();
        film3.setName("Film Three");
        film3.setDescription("Description Three");
        film3.setReleaseDate(LocalDate.of(2012, 1, 1));
        film3.setDuration(120);
        film3.setMpa(new Mpa(3, null));
        film3.setGenres(List.of(new Genre(1, null)));

        film1 = filmStorage.addFilm(film1);
        film2 = filmStorage.addFilm(film2);
        film3 = filmStorage.addFilm(film3);

        // Добавляем лайки
        filmStorage.addLike(film1.getId(), 1); // film1 - 1 лайк
        filmStorage.addLike(film1.getId(), 2); // film1 - 2 лайка
        filmStorage.addLike(film2.getId(), 1); // film2 - 1 лайк
        filmStorage.addLike(film3.getId(), 1); // film3 - 1 лайк
        filmStorage.addLike(film3.getId(), 2); // film3 - 2 лайка
        filmStorage.addLike(film3.getId(), 3); // film3 - 3 лайка

        // Проверяем сортировку по популярности без фильтров
        var popularFilms = filmStorage.findMostPopularFilms(10, null, null);
        assertThat(popularFilms).hasSize(3);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film3.getId()); // 3 лайка
        assertThat(popularFilms.get(1).getId()).isEqualTo(film1.getId()); // 2 лайка
        assertThat(popularFilms.get(2).getId()).isEqualTo(film2.getId()); // 1 лайк

        // Проверяем фильтр по жанру = 1
        var genreFiltered = filmStorage.findMostPopularFilms(10, 1, null);
        assertThat(genreFiltered).hasSize(2);
        assertThat(genreFiltered.get(0).getId()).isEqualTo(film3.getId());
        assertThat(genreFiltered.get(1).getId()).isEqualTo(film1.getId());

        // Проверяем фильтр по году = 2011
        var yearFiltered = filmStorage.findMostPopularFilms(10, null, 2011);
        assertThat(yearFiltered).hasSize(1);
        assertThat(yearFiltered.get(0).getId()).isEqualTo(film2.getId());

        // Проверяем фильтр по жанру и году вместе
        var genreYearFiltered = filmStorage.findMostPopularFilms(10, 1, 2012);
        assertThat(genreYearFiltered).hasSize(1);
        assertThat(genreYearFiltered.get(0).getId()).isEqualTo(film3.getId());
    }

    @Test
    void shouldGetFilmsByDirectorSortedByYearAndLikes() {
        // Создаем режиссера
        jdbcTemplate.update("INSERT INTO directors (id, name) VALUES (?, ?)", 1, "Director 1");

        // Создаем пользователей для лайков
        createTestUser(1);
        createTestUser(2);

        // Создаем фильмы с разными годами выпуска
        Film film1 = createFilm("Film A", "Description A",
                LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film film2 = createFilm("Film B", "Description B",
                LocalDate.of(2010, 1, 1), 120, new Mpa(2, "PG"));
        Film film3 = createFilm("Film C", "Description C",
                LocalDate.of(2020, 1, 1), 90, new Mpa(3, "PG-13"));

        // Связываем фильмы с режиссером
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                film1.getId(), 1);
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                film2.getId(), 1);
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                film3.getId(), 1);

        // Добавляем лайки (film3 - 2 лайка, film2 - 1 лайк, film1 - 0 лайков)
        filmStorage.addLike(film3.getId(), 1);
        filmStorage.addLike(film3.getId(), 2);
        filmStorage.addLike(film2.getId(), 1);

        // Проверяем сортировку по годам
        List<Film> filmsByYear = filmStorage.getFilmsByDirector(1, "year");
        assertThat(filmsByYear)
                .hasSize(3)
                .extracting(Film::getName)
                .containsExactly("Film A", "Film B", "Film C"); // от старых к новым

        // Проверяем сортировку по лайкам
        List<Film> filmsByLikes = filmStorage.getFilmsByDirector(1, "likes");
        assertThat(filmsByLikes)
                .hasSize(3)
                .extracting(Film::getName)
                .containsExactly("Film C", "Film B", "Film A"); // от популярных к непопулярным
    }

    @Test
    void shouldDeleteFilmById() {
        Film film = new Film();
        film.setName("Film name");
        film.setDescription("Film Description");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(111);
        film.setMpa(new Mpa(1, null));
        film.setGenres(List.of(new Genre(1, null), new Genre(2, null)));

        Film created = filmStorage.addFilm(film);

        Optional<Film> found = filmStorage.findFilmById(created.getId());
        assertThat(found).isPresent();

        filmStorage.removeFilm(created.getId());

        Optional<Film> afterRemove = filmStorage.findFilmById(created.getId());
        assertThat(afterRemove).isEmpty();
    }

    @Test
    void shouldSearchFilmsByTitleAndDirector() {
        // Добавляем режиссеров
        jdbcTemplate.update("INSERT INTO directors (id, name) VALUES (?, ?)", 1, "Крадущийся Режиссер");
        jdbcTemplate.update("INSERT INTO directors (id, name) VALUES (?, ?)", 2, "Другой Режиссер");

        // Добавляем фильмы
        Film film1 = createFilm("Крадущийся тигр", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));
        Film film2 = createFilm("Спящий лев", "Description", LocalDate.of(2001, 1, 1), 100, new Mpa(2, "PG"));
        Film film3 = createFilm("Тайна крадущегося", "Description", LocalDate.of(2002, 1, 1), 110, new Mpa(3, "PG-13"));

        // Привязываем режиссеров
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film1.getId(), 1);
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film2.getId(), 2);
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film3.getId(), 1);

        // Добавляем пользователей
        createTestUser(1);
        createTestUser(2);

        // Добавляем лайки (чтобы проверить сортировку по популярности)
        filmStorage.addLike(film1.getId(), 1);
        filmStorage.addLike(film3.getId(), 1);
        filmStorage.addLike(film3.getId(), 2);

        // Поиск по названию
        List<Film> byTitle = filmStorage.searchFilms("крад", List.of("title"));
        assertThat(byTitle)
                .extracting(Film::getName)
                .containsExactly("Тайна крадущегося", "Крадущийся тигр");

        // Поиск по режиссеру
        List<Film> byDirector = filmStorage.searchFilms("крад", List.of("director"));
        assertThat(byDirector)
                .extracting(Film::getName)
                .containsExactly("Тайна крадущегося", "Крадущийся тигр");

        // Поиск по обоим полям
        List<Film> byBoth = filmStorage.searchFilms("крад", List.of("title", "director"));
        assertThat(byBoth)
                .extracting(Film::getName)
                .containsExactly("Тайна крадущегося", "Крадущийся тигр");
    }
}
