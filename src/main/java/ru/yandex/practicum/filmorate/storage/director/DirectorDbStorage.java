package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        String sql = """
                 INSERT INTO directors (name) VALUES (?)
                 """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE id = ?",
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public List<Director> getAllDirectors() {
        return jdbcTemplate.query("SELECT * FROM directors", this::mapToRowDirector);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sql, this::mapToRowDirector, id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteDirector(int id) {
        jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id);
    }

    private Director mapToRowDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    }
}
