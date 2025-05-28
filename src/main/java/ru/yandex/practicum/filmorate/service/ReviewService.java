package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(@Valid Review review) {

        // Проверяем, что пользователь существует
        userStorage.findUserById(review.getUserId())
                .orElseThrow(() -> new NotFoundException("User with id " + review.getUserId() + " not found"));

        // Проверяем, что фильм существует
        filmStorage.findFilmById(review.getFilmId())
                .orElseThrow(() -> new NotFoundException("Film with id " + review.getFilmId() + " not found"));

        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        // Проверяем, что отзыв существует
        reviewStorage.findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review with id " + review.getReviewId() + " not found"));

        // Обновляем отзыв, теперь update может быть уверен, что отзыв есть
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Review with id " + id + " not found"));

        reviewStorage.delete(id);
    }

    public Review findById(int id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public List<Review> findByFilmId(Integer filmId, Integer count) {
        if (count != null && count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        int finalCount = count == null ? 10 : count;
        return reviewStorage.findByFilmId(filmId, finalCount);
    }

    public void addLike(int reviewId, int userId) {
        // Проверяем, что отзыв существует
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with id " + reviewId + " not found"));

        // Проверяем, что пользователь существует
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        // Если проверки пройдены — добавляем лайк
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeReaction(int reviewId, int userId) {
        reviewStorage.removeReaction(reviewId, userId);
    }
}
