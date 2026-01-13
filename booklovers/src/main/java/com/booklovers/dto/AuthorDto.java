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
public class AuthorDto {
    private Long id;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String biography;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private String nationality;
    private LocalDateTime createdAt;
    private Integer booksCount;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
