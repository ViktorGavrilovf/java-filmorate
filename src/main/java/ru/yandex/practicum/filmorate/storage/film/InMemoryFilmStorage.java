package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        film.setId(idGenerate());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) throw new NotFoundException("Фильм не найден");
        films.put(film.getId(), film);
        return film;
    }

    private Integer idGenerate() {
        return films.keySet()
                .stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
