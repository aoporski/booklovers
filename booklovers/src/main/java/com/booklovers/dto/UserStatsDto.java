package com.booklovers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDto {
    private Long userId;
    private String username;
    private Integer booksRead;
    private Integer reviewsWritten;
    private Integer ratingsGiven;
    private Double averageRatingGiven;
    private Integer shelvesCount;
    private Integer favoriteGenresCount;
}
