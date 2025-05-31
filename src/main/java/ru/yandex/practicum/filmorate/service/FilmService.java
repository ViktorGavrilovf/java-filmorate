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
import java.util.Arrays;
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

    public List<Film> getCommonFilmsWithFriend(int userId, int friendId) {
        userService.getUserOrThrow(userId);
        userService.getUserOrThrow(friendId);
        log.info("Запрос общих с другом фильмов. userId: {}, friendId: {}", userId, friendId);
        return filmStorage.getCommonFilmsWithFriend(userId, friendId);
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        log.info("Запрос популярных фильмов. Количество: {}, жанр: {}, год: {}", count, genreId, year);
        return filmStorage.findMostPopularFilms(count, genreId, year);
    }

    public void removeFilm(int filmId) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        filmStorage.removeFilm(filmId);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, String by) {
        List<String> byList = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        log.info("Поиск фильмов по запросу '{}', по полям {}", query, byList);

        return filmStorage.searchFilms(query, byList);
    }
}
