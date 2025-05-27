package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        String sql = """
                 INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)
                 """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        updateGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                     UPDATE films SET name = ?, description = ?, release_date = ?,
                     duration = ?, mpa_rating_id = ? WHERE id = ?
                     """;
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateGenres(film);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return jdbcTemplate.query("SELECT * FROM films", this::mapToRowFilm);
    }

    @Override
    public Optional<Film> findFilmById(Integer id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapToRowFilm, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopular(int count) {
        String sql = """
                     SELECT f.*
                     FROM films f
                     LEFT JOIN film_likes fl ON f.id = fl.film_id
                     GROUP BY f.id
                     ORDER BY COUNT(fl.user_id) DESC
                     LIMIT ?
                     """;
        return jdbcTemplate.query(sql, this::mapToRowFilm, count);
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        String sql = """
                     WITH common_likes AS (
                     SELECT
                     fl_by_other.user_id,
                     COUNT(fl.film_id) AS count_of_likes
                     FROM film_likes fl
                     INNER JOIN film_likes fl_by_other ON fl.film_id = fl_by_other.film_id
                     AND fl.user_id <> fl_by_other.user_id
                     WHERE fl.user_id = ?
                     GROUP BY fl_by_other.user_id
                     ),
                     max_common_likes AS (
                     SELECT MAX(count_of_likes) AS max_count
                     FROM common_likes
                     ),
                     most_similar_users AS (
                     SELECT user_id
                     FROM common_likes
                     WHERE count_of_likes = (SELECT max_count FROM max_common_likes)
                     )
                
                     SELECT DISTINCT f.*
                     FROM films f
                     WHERE
                     NOT EXISTS (
                     SELECT 1 FROM film_likes
                     WHERE film_id = f.id AND user_id = ?
                     )
                     AND EXISTS (
                     SELECT 1 FROM film_likes
                     WHERE film_id = f.id
                     AND user_id IN (SELECT user_id FROM most_similar_users)
                     );
                     """;
        return jdbcTemplate.query(sql, this::mapToRowFilm, userId, userId);
    }

    private void updateGenres(Film film) {
        if (film.getGenres() == null) return;
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .forEach(genreId -> jdbcTemplate.update(sql, film.getId(), genreId));
    }

    private Film mapToRowFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(getMpa(rs.getInt("mpa_rating_id")));
        film.setGenres(getGenres(rs.getInt("id")));
        return film;
    }

    private Mpa getMpa(int mpaId) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum)
                -> new Mpa(rs.getInt("id"), rs.getString("name")), mpaId);
    }

    private List<Genre> getGenres(int filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        return jdbcTemplate.query(sql, ((rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name"))), filmId);
    }
}
