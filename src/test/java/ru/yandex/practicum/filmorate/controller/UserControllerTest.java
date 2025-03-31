package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController controller;
    private User user;

    @BeforeEach
    void setup() {
        controller = new UserController();
        user = createValidUser();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldAddUserSuccessfully() {
        User userAdded = controller.addUser(user);
        assertNotNull(userAdded.getId(), "ID должно быть сгенерировано");
        assertEquals(userAdded.getName(), user.getName());
    }

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        user.setEmail("email");
        assertThrows(ValidationException.class, () -> controller.addUser(user),
                "Должно быть исключение, email без @");
        user.setEmail(" ");
        assertThrows(ValidationException.class, () -> controller.addUser(user),
                "Должно быть исключение, email пустой");
    }

    @Test
    void shouldThrowWhenLoginContainsSpace() {
        user.setLogin("bad login");
        assertThrows(ValidationException.class, () -> controller.addUser(user),
                "Должно быть исключение, логин с пробелом");
    }

    @Test
    void shouldSetNameToLoginIfEmpty() {
        user.setName(" ");
        controller.addUser(user);
        assertEquals(user.getName(), user.getLogin(), "Имя и логин должны быть равны");
    }

    @Test
    void shouldThrowWhenBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(2));
        assertThrows(ValidationException.class, () -> controller.addUser(user),
                "Должно быть исключение, дата рождения не может быть в будущем");
    }
}
