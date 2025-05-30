package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Integer eventId;
    @NotNull
    private Long timestamp;
    @NotNull
    private Integer userId;
    @NotNull
    private String eventType;
    @NotNull
    private String operation;
    private Integer entityId;
}
