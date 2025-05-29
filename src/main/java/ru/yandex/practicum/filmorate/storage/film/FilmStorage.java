package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getFilms();

    Optional<Film> findFilmById(Integer id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> findMostPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getCommonFilmsWithFriend(int userId, int friendId);

    List<Film> getRecommendations(int userId);

    void removeFilm(int filmId);
}
