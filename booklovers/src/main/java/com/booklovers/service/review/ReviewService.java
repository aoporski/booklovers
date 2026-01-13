package com.booklovers.service.review;

import com.booklovers.dto.ReviewDto;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewDto createReview(Long bookId, ReviewDto reviewDto);
    ReviewDto updateReview(Long id, ReviewDto reviewDto);
    void deleteReview(Long id);
    void deleteReviewAsAdmin(Long id); 
    Optional<ReviewDto> getReviewById(Long id);
    List<ReviewDto> getReviewsByBookId(Long bookId);
    List<ReviewDto> getReviewsByUserId(Long userId);
    List<ReviewDto> getAllReviews();
    void createRatingAfterReview(Long bookId, Integer ratingValue);
}
