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
public class BookStatsDto {
    private Long bookId;
    private String bookTitle;
    private Integer readersCount;
    private Double averageRating;
    private Integer ratingsCount;
    private Map<Integer, Long> ratingsDistribution;
}
