package ru.yandex.practicum.filmorate.storage.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReviewDbStorageTest {

    @Autowired
    private ReviewDbStorage reviewStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // теперь будет проинициализирован!

    private Review testReview;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM review_reactions");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films");

        // Предполагаем, что mpa_ratings уже заполнены в тестовой базе

        jdbcTemplate.update("INSERT INTO users (id, name, email, login, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "Test User1", "test1@example.com", "testuser1", "1990-01-01");
        jdbcTemplate.update("INSERT INTO users (id, name, email, login, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "Test User2", "test2@example.com", "testuser2", "1991-02-02");

        jdbcTemplate.update("INSERT INTO films (id, name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?, ?)",
                1, "Test Film", "Description", "2000-01-01", 120, 1);

        testReview = new Review();
        testReview.setContent("Отличный фильм!");
        testReview.setIsPositive(true);
        testReview.setUserId(1);
        testReview.setFilmId(1);
    }

    @Test
    void createAndFindById() {
        Review created = reviewStorage.create(testReview);
        assertThat(created.getReviewId()).isPositive();
        assertThat(created.getUseful()).isZero();

        Optional<Review> found = reviewStorage.findById(created.getReviewId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(testReview.getContent());
    }

    @Test
    void updateReview() {
        Review created = reviewStorage.create(testReview);
        created.setContent("Обновленный отзыв");
        created.setIsPositive(false);

        Review updated = reviewStorage.update(created);
        assertThat(updated.getContent()).isEqualTo("Обновленный отзыв");
        assertThat(updated.getIsPositive()).isFalse();
    }

    @Test
    void deleteReview() {
        Review created = reviewStorage.create(testReview);
        int id = created.getReviewId();

        reviewStorage.delete(id);
        Optional<Review> deleted = reviewStorage.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void addLikeAndDislikeAndRemoveReaction() {
        Review created = reviewStorage.create(testReview);
        int reviewId = created.getReviewId();
        int userId = 2; // теперь пользователь с id=2 есть в БД

        // Лайк
        reviewStorage.addLike(reviewId, userId);
        Review afterLike = reviewStorage.findById(reviewId).orElseThrow();
        assertThat(afterLike.getUseful()).isEqualTo(1);

        // Дизлайк заменяет лайк
        reviewStorage.addDislike(reviewId, userId);
        Review afterDislike = reviewStorage.findById(reviewId).orElseThrow();
        assertThat(afterDislike.getUseful()).isEqualTo(-1);

        // Удаляем реакцию
        reviewStorage.removeReaction(reviewId, userId);
        Review afterRemove = reviewStorage.findById(reviewId).orElseThrow();
        assertThat(afterRemove.getUseful()).isZero();
    }
}
