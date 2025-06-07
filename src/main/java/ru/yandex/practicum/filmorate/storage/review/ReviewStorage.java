package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(int id);

    Optional<Review> findById(int id);

    List<Review> findByFilmId(Integer filmId, int count);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeReaction(int reviewId, int userId);
}
