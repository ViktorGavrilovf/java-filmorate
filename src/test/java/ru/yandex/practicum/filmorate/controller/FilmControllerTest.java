package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController controller;
    private Film film;

    @BeforeEach
    void setup() {
        controller = new FilmController();
        film = createValidFilm();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Джентельмены");
        film.setDescription("Фильм снял Гай Ричи");
        film.setReleaseDate(LocalDate.of(2019, 12, 3));
        film.setDuration(113);
        return film;
    }

    @Test
    void shouldAddFilmSuccessfully() {
        Film filmAdded = controller.addFilm(film);
        assertNotNull(filmAdded.getId(), "ID должно быть сгенерировано");
        assertEquals(filmAdded.getName(), film.getName());
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        film.setName(" ");
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "Должно быть исключение, название не может быть пустым");
    }

    @Test
    void shouldThrowWhenDescriptionIsTooLong() {
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "Должно быть исключение, описание не можеть быть длинне 200 символов");
    }

    @Test
    void shouldThrowWhenReleaseDateIsTooEarly() {
        film.setReleaseDate(LocalDate.of(1894, 12, 24));
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "Должно быть исключение, дата релиза — не раньше 28 декабря 1895 года");
    }

    @Test
    void shouldThrowWhenDurationIsZero() {
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "Должно быть исключение, продолжительность фильма должна быть положительным числом");
    }
}
