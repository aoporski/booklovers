package com.booklovers.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    
    @NotBlank(message = "Review content is required")
    @Size(max = 5000, message = "Review content must not exceed 5000 characters")
    private String content;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer ratingValue; // Opcjonalna ocena (1-5 gwiazdek)
    
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
