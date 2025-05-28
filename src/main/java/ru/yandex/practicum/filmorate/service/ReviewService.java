package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review create(Review review) {
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public Review findById(int id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
    }

    public List<Review> findByFilmId(Integer filmId, Integer count) {
        int finalCount = count == null ? 10 : count;
        return reviewStorage.findByFilmId(filmId, finalCount);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeReaction(int reviewId, int userId) {
        reviewStorage.removeReaction(reviewId, userId);
    }
}
