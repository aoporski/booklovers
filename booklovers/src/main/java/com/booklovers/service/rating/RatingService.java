package com.booklovers.service.rating;

import com.booklovers.dto.RatingDto;

import java.util.List;
import java.util.Optional;

public interface RatingService {
    RatingDto createOrUpdateRating(Long bookId, RatingDto ratingDto);
    void deleteRating(Long bookId);
    Optional<RatingDto> getRatingByBookId(Long bookId);
    List<RatingDto> getRatingsByBookId(Long bookId);
    List<RatingDto> getRatingsByUserId(Long userId);
}
