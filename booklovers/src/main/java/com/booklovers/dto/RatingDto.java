package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO reprezentujące ocenę książki")
public class RatingDto {
    @Schema(description = "ID oceny", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Wartość oceny w skali 1-5", example = "5", required = true, minimum = "1", maximum = "5")
    private Integer value;
    
    @Schema(description = "ID użytkownika", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    @Schema(description = "Nazwa użytkownika", example = "john_doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;
    
    @Schema(description = "ID książki", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bookId;
    
    @Schema(description = "Tytuł książki", example = "Hobbit", accessMode = Schema.AccessMode.READ_ONLY)
    private String bookTitle;
    
    @Schema(description = "Data utworzenia oceny", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Data ostatniej aktualizacji oceny", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
