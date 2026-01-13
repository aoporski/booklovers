package com.booklovers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDataExportDto {
    private UserDto user;
    private List<BookDto> books;
    private List<ReviewDto> reviews;
    private List<RatingDto> ratings;
    private List<String> shelves; // Lista nazw kategorii/półek
    private List<UserBookDto> userBooks; // Książki z kategoriami
}
