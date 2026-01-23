package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO do rejestracji nowego użytkownika")
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Nazwa użytkownika", example = "john_doe", required = true, minLength = 3, maxLength = 50)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Adres email użytkownika", example = "john.doe@example.com", required = true, format = "email")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Hasło użytkownika", example = "securePassword123", required = true, minLength = 6, writeOnly = true)
    private String password;
    
    @Schema(description = "Imię użytkownika", example = "John")
    private String firstName;
    
    @Schema(description = "Nazwisko użytkownika", example = "Doe")
    private String lastName;
}
