package com.booklovers.service.review;

import com.booklovers.dto.ReviewDto;
import com.booklovers.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    
    public ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .username(review.getUser() != null ? review.getUser().getUsername() : null)
                .bookId(review.getBook() != null ? review.getBook().getId() : null)
                .bookTitle(review.getBook() != null ? review.getBook().getTitle() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    public Review toEntity(ReviewDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Review.builder()
                .id(dto.getId())
                .content(dto.getContent())
                .build();
    }
}
