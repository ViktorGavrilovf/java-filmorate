package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;
    private Set<Integer> friends = new HashSet<>();

    @Email(message = "электронная почта не может быть пустой и должна содержать символ @")
    private String email;

    @NotBlank(message = "логин не может быть пустым")
    private String login;

    private String name;

    @PastOrPresent(message = "дата рождения не может быть в будущем")
    private LocalDate birthday;

    public void addFriend(Integer friendId) {
        friends.add(friendId);
    }

    public void removeFriend(Integer friendId) {
        friends.remove(friendId);
    }
}
