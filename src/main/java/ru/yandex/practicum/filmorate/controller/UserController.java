package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        checkUser(user);
        user.setId(idGenerate());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (user.getId() == null) throw new ValidationException("Id должен быть указан");
        if (!users.containsKey(user.getId())) throw new ValidationException("Пользователь с таким Id не найден");
        checkUser(user);
        users.put(user.getId(), user);
        return user;
    }

    public Integer idGenerate() {
        return users.keySet()
                .stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void checkUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Невалидный email: {}", user.getEmail());
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Невалидный логин {}", user.getLogin());
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое — будет использован логин: '{}'", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }
}
