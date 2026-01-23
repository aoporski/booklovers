package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO reprezentujące książkę w systemie")
public class BookDto {
    @Schema(description = "ID książki", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Schema(description = "Tytuł książki", example = "Hobbit", required = true)
    private String title;
    
    @Schema(description = "Nazwa autora (tylko do wyświetlania)", example = "J.R.R. Tolkien", accessMode = Schema.AccessMode.READ_ONLY)
    private String author; // Zachowane dla kompatybilności wstecznej (wyświetlanie)
    
    @Schema(description = "ID autora (wymagane przy tworzeniu/edycji)", example = "1")
    private Long authorId; // ID autora z encji Author (używane przy dodawaniu/edycji) - wymagane przy tworzeniu/edycji
    
    @Schema(description = "Numer ISBN", example = "978-83-7181-510-2")
    private String isbn;
    
    @Schema(description = "Opis książki", example = "Klasyczna powieść fantasy o przygodach Bilba Bagginsa")
    private String description;
    
    @Schema(description = "Wydawnictwo", example = "Wydawnictwo Literackie")
    private String publisher;
    
    @Schema(description = "Data publikacji", example = "1937-09-21")
    private LocalDate publicationDate;
    
    @Schema(description = "Liczba stron", example = "310")
    private Integer pageCount;
    
    @Schema(description = "Język", example = "pl")
    private String language;
    
    @Schema(description = "URL do okładki", example = "/uploads/covers/hobbit.jpg")
    private String coverImageUrl;
    
    @Schema(description = "Data utworzenia rekordu", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Średnia ocena", example = "4.5", accessMode = Schema.AccessMode.READ_ONLY)
    private Double averageRating;
    
    @Schema(description = "Liczba ocen", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer ratingsCount;
    
    @Schema(description = "Liczba recenzji", example = "15", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer reviewsCount;
}
