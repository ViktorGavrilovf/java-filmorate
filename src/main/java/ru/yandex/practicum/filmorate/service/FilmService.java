package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmOrThrow(int id) {
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film createFilm(Film film) {
        checkReleaseDate(film);
        log.info("СОздание фильма: {}", film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        checkReleaseDate(film);
        getFilmOrThrow(film.getId());
        log.info("Обновление фильма: {}", film);
        return filmStorage.updateFilm(film);
    }

    public void addLike(int filmId, int userId) {
        userService.getUserOrThrow(userId);
        log.debug("Пользователь {} лайкнул фильм {}", userId, filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userService.getUserOrThrow(userId);
        log.debug("Пользователь {} удалил лайк к фильму {}", userId, filmId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopular(int count) {
        log.info("Запрос популярных фильмов. Количество: {}", count);
        return filmStorage.getMostPopular(count);
    }

    public List<Film> getCommon(int userId, int friendId) {
        userService.getUserOrThrow(userId);
        userService.getUserOrThrow(friendId);
        log.info("Запрос общих с другом фильмов. userId: {}, friendId: {}", userId, friendId);
        return filmStorage.getCommon(userId, friendId);
    }


    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
