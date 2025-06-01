package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@Qualifier("EventDbStorage")
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(int userId, String eventType, String operation, int entityId) {
        String sql = """
                INSERT INTO events (TIMESTAMP, USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                System.currentTimeMillis(),
                userId,
                eventType,
                operation,
                entityId
                );
        log.info("addEvent called: userId={}, eventType={}, operation={}, entityId={}",
                userId, eventType, operation, entityId);
    }

    @Override
    public List<Event> getFeed(int userId) {
        String sql = """
                SELECT * FROM events
                WHERE USER_ID = ?
                ORDER BY event_id
                """;
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(rs.getString("event_type"));
        event.setOperation(rs.getString("operation"));
        event.setEntityId(rs.getInt("entity_id"));
        return event;
    }
}
