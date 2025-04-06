package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;

    @Email(message = "электронная почта не может быть пустой и должна содержать символ @")
    private String email;

    @NotBlank(message = "логин не может быть пустым")
    private String login;


    private String name;
    private LocalDate birthday;
}
