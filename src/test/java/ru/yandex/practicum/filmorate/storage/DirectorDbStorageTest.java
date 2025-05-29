package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DirectorDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DirectorDbStorage directorStorage;

    private Director director;

    @BeforeEach
    void setUp() {
        director = new Director();
        director.setName("Test Director");
        jdbcTemplate.update("DELETE FROM directors");
    }

    @Test
    void createDirector_ShouldReturnDirectorWithId() {
        Director created = directorStorage.createDirector(director);

        assertNotNull(created.getId());
        assertEquals(director.getName(), created.getName());
    }

    @Test
    void updateDirector_ShouldUpdateName() {
        Director created = directorStorage.createDirector(director);
        created.setName("Updated Name");

        Director updated = directorStorage.updateDirector(created);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void getAllDirectors_ShouldReturnAllDirectors() {
        directorStorage.createDirector(director);
        directorStorage.createDirector(new Director(null, "Another Director"));

        List<Director> directors = directorStorage.getAllDirectors();

        assertThat(directors).hasSize(2);
    }

    @Test
    void getDirectorById_ShouldReturnDirectorIfExists() {
        Director created = directorStorage.createDirector(director);

        Optional<Director> found = directorStorage.getDirectorById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    void getDirectorById_ShouldReturnEmptyOptionalIfNotExists() {
        Optional<Director> found = directorStorage.getDirectorById(999);

        assertTrue(found.isEmpty());
    }

    @Test
    void deleteDirector_ShouldRemoveDirector() {
        Director created = directorStorage.createDirector(director);
        directorStorage.deleteDirector(created.getId());

        Optional<Director> deleted = directorStorage.getDirectorById(created.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    void createDirector_ShouldThrowExceptionWhenNameIsNull() {
        director.setName(null);

        assertThrows(DataIntegrityViolationException.class,
                () -> directorStorage.createDirector(director));
    }
}