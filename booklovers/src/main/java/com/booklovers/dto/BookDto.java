package com.booklovers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String author; // Zachowane dla kompatybilności wstecznej (wyświetlanie)
    
    private Long authorId; // ID autora z encji Author (używane przy dodawaniu/edycji) - wymagane przy tworzeniu/edycji
    
    private String isbn;
    private String description;
    private String publisher;
    private LocalDate publicationDate;
    private Integer pageCount;
    private String language;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private Double averageRating;
    private Integer ratingsCount;
    private Integer reviewsCount;
}
