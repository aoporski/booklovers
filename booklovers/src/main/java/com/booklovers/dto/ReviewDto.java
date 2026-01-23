package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO reprezentujące recenzję książki")
public class ReviewDto {
    @Schema(description = "ID recenzji", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Review content is required")
    @Size(max = 5000, message = "Review content must not exceed 5000 characters")
    @Schema(description = "Treść recenzji", example = "Świetna książka! Polecam każdemu miłośnikowi fantasy.", required = true, maxLength = 5000)
    private String content;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Schema(description = "Ocena w skali 1-5", example = "5", required = true, minimum = "1", maximum = "5")
    private Integer ratingValue; // Wymagana ocena (1-5 gwiazdek)
    
    @Schema(description = "ID użytkownika", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    @Schema(description = "Nazwa użytkownika", example = "john_doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;
    
    @Schema(description = "ID książki", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bookId;
    
    @Schema(description = "Tytuł książki", example = "Hobbit", accessMode = Schema.AccessMode.READ_ONLY)
    private String bookTitle;
    
    @Schema(description = "Data utworzenia recenzji", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Data ostatniej aktualizacji recenzji", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
