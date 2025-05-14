package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldAddAndFindUserById() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.addUser(user);
        Optional<User> testUser = userStorage.findUserById(created.getId());

        assertThat(testUser).isPresent().hasValueSatisfying(u -> {
            assertThat(u.getId()).isEqualTo(created.getId());
            assertThat(u.getEmail()).isEqualTo("test@email.com");
            assertThat(u.getLogin()).isEqualTo("testuser");
            assertThat(u.getName()).isEqualTo("Test User");
            assertThat(u.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
        });
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("old@email.com");
        user.setLogin("oldlogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.addUser(user);
        created.setEmail("new@email.com");
        created.setLogin("newlogin");
        created.setName("New Name");
        created.setBirthday(LocalDate.of(1995, 5, 5));

        userStorage.updateUser(created);

        Optional<User> updated = userStorage.findUserById(created.getId());

        assertThat(updated).isPresent().hasValueSatisfying(u -> {
            assertThat(u.getEmail()).isEqualTo("new@email.com");
            assertThat(u.getLogin()).isEqualTo("newlogin");
            assertThat(u.getName()).isEqualTo("New Name");
            assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
        });
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@email.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1980, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1990, 2, 2));

        userStorage.addUser(user1);
        userStorage.addUser(user2);

        Collection<User> allUser = userStorage.getUsers();
        assertThat(allUser)
                .hasSize(2)
                .anySatisfy(u -> assertThat(u.getLogin()).isEqualTo("user1"))
                .anySatisfy(u -> assertThat(u.getLogin()).isEqualTo("user2"));
    }
}
