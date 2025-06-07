package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDao;

import java.util.List;

@Service
public class DirectorService {

    private final DirectorDao directorDao;

    @Autowired
    public DirectorService(DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

    public Director createDirector(Director director) {
        return directorDao.createDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        return directorDao.updateDirector(director);
    }

    public List<Director> getAllDirectors() {
        return directorDao.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        return directorDao.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public void deleteDirector(int id) {
        getDirectorById(id);
        directorDao.deleteDirector(id);
    }
}
