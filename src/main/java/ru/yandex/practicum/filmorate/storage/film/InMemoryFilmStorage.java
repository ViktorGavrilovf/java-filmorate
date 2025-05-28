package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setId(idGenerate());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> findFilmById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) throw new NotFoundException("Фильм не найден");
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        findFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден")).addLike(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        findFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден")).removeLike(userId);
    }

    @Override
    public List<Film> findMostPopularFilms(int count, Integer genreId, Integer year) {
        log.warn("Вызван заглушечный метод findMostPopularFilms: count={}, genreId={}, year={}",
                count, genreId, year);
        return List.of();
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        return List.of();
    }

    @Override
    public List<Film> getCommonFilmsWithFriend(int userId, int friendId) {
        log.warn("Метод getCommonFilmsWithFriend в классе InMemoryFilmStorage не реализован");
        throw new UnsupportedOperationException("Метод не реализован");
    }

    private Integer idGenerate() {
        return films.keySet()
                .stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
