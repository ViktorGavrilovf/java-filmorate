package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorDao {

    Director createDirector(Director director);

    Director updateDirector(Director director);

    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(int id);

    void deleteDirector(int id);
}
