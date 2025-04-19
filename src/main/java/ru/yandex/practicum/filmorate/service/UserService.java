package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User getUserOrThrow(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public User createUser(User user) {
        checkUserNameAndLogin(user);
        log.info("Обновление пользователя: {}", user);
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        checkUserNameAndLogin(user);
        getUserOrThrow(user.getId());
        log.info("Обновление пользователя: {}", user);
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        log.debug("Добавление в друзья {} -> {}", userId, friendId);
        user.addFriend(friend.getId());
        friend.addFriend(user.getId());
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        log.debug("Удаление из друзей: {} -> {}", userId, friendId);
        user.removeFriend(friend.getId());
        friend.removeFriend(user.getId());
    }

    public List<User> getFriends(int id) {
        log.info("Получение списка друзей пользователя с id {}", id);
        return getUserOrThrow(id).getFriends().stream()
                .map(this::getUserOrThrow)
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        Set<Integer> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());
        return common.stream().map(this::getUserOrThrow).toList();
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


