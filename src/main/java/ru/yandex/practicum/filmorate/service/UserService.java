package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
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
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Удаление из друзей: {} -> {}", userId, friendId);
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);
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
}


