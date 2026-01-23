package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "DTO reprezentujące użytkownika w systemie")
public class UserDto {
    @Schema(description = "ID użytkownika", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Nazwa użytkownika (tylko do odczytu - nie można zmienić przez endpoint aktualizacji profilu)", example = "john_doe", required = true, minLength = 3, maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Adres email użytkownika (tylko do odczytu - nie można zmienić przez endpoint aktualizacji profilu)", example = "john.doe@example.com", required = true, format = "email", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Hasło użytkownika (opcjonalne przy aktualizacji, wymagane przy rejestracji)", example = "securePassword123", minLength = 6, writeOnly = true)
    private String password;
    
    @Schema(description = "Imię użytkownika", example = "John")
    private String firstName;
    
    @Schema(description = "Nazwisko użytkownika", example = "Doe")
    private String lastName;
    
    @Schema(description = "Biografia użytkownika", example = "Miłośnik książek i kawy")
    private String bio;
    
    @Schema(description = "URL do awatara użytkownika", example = "/uploads/avatars/avatar-123.jpg")
    private String avatarUrl;
    
    @Schema(description = "Rola użytkownika", example = "USER", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;
    
    @Schema(description = "Czy użytkownik jest zablokowany", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isBlocked;
    
    @Schema(description = "Data utworzenia konta", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Liczba książek w biblioteczce użytkownika", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer booksCount;
    
    @Schema(description = "Liczba recenzji napisanych przez użytkownika", example = "15", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer reviewsCount;
    
    @Schema(description = "Średnia ocena wystawiona przez użytkownika", example = "4.5", accessMode = Schema.AccessMode.READ_ONLY)
    private Double averageRating;
}
