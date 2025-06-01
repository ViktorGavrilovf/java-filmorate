package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage,
                       @Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("EventDbStorage") EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventStorage = eventStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User getUserOrThrow(int id) {
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public User createUser(User user) {
        checkUserNameAndLogin(user);
        log.info("Создание пользователя: {}", user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        checkUserNameAndLogin(user);
        getUserOrThrow(user.getId());
        log.info("Обновление пользователя: {}", user);
        return userStorage.updateUser(user);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Добавление в друзья {} -> {}", userId, friendId);
        userStorage.addFriend(userId, friendId);

        eventStorage.addEvent(userId, "FRIEND", "ADD", friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Удаление из друзей: {} -> {}", userId, friendId);
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);

        eventStorage.addEvent(userId, "FRIEND", "REMOVE", friendId);
    }

    public List<User> getFriends(int userId) {
        log.info("Получение списка друзей пользователя с userId {}", userId);
        getUserOrThrow(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void checkUserNameAndLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("логин не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public List<Film> getRecommendations(int userId) {
        getUserOrThrow(userId);
        return filmStorage.getRecommendations(userId);
    }

    public void removeUser(int userId) {
        getUserOrThrow(userId);
        userStorage.removeUser(userId);
    }

    public List<Event> getFeed(int userId) {
        getUserOrThrow(userId);
        return eventStorage.getFeed(userId);
    }
}


