package com.booklovers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO do logowania użytkownika")
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Nazwa użytkownika", example = "john_doe", required = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Schema(description = "Hasło użytkownika", example = "securePassword123", required = true, writeOnly = true)
    private String password;
}
