package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Review with id " + review.getReviewId() + " not found");
        }

        return findById(review.getReviewId()).orElseThrow(() ->
                new IllegalStateException("Review was just updated but not found: " + review.getReviewId())
        );
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public Optional<Review> findById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public List<Review> findByFilmId(Integer filmId, int count) {
        String sql = (filmId == null)
                ? "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

        return filmId == null
                ? jdbcTemplate.query(sql, this::mapRowToReview, count)
                : jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        addReaction(reviewId, userId, true);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        addReaction(reviewId, userId, false);
    }

    @Override
    public void removeReaction(int reviewId, int userId) {
        removeReactionWithoutRecalculation(reviewId, userId);
        recalculateUseful(reviewId);
    }

    private void addReaction(int reviewId, int userId, boolean isLike) {
        removeReactionWithoutRecalculation(reviewId, userId); // удаляем старую реакцию без перерасчёта
        String sql = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, isLike);
        recalculateUseful(reviewId);
    }

    private void recalculateUseful(int reviewId) {
        String sql = "SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END) FROM review_reactions WHERE review_id = ?";
        Integer useful = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        jdbcTemplate.update("UPDATE reviews SET useful = ? WHERE review_id = ?", useful == null ? 0 : useful, reviewId);
    }

    private Review mapRowToReview(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }

    private void removeReactionWithoutRecalculation(int reviewId, int userId) {
        String sql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
    }
}
