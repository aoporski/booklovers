package com.booklovers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsDto {
    private Integer totalBooks;
    private Integer totalUsers;
    private Integer totalReviews;
    private Integer totalRatings;
    private Double averageRating;
    private Map<String, Long> booksByGenre;
    private Map<String, Long> topAuthors;
    private Map<Integer, Long> ratingsDistribution;
}
