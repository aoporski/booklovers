package com.booklovers.api.controller;

import com.booklovers.dto.RatingDto;
import com.booklovers.service.rating.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "API do zarządzania ocenami książek")
public class RatingController {
    
    private final RatingService ratingService;
    
    @Operation(summary = "Utwórz lub zaktualizuj ocenę", description = "Dodaje nową ocenę (1-5) lub aktualizuje istniejącą dla książki. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocena została zapisana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (ocena musi być w zakresie 1-5)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PostMapping("/books/{bookId}")
    public ResponseEntity<RatingDto> createOrUpdateRating(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId,
            @Valid @RequestBody RatingDto ratingDto) {
        RatingDto rating = ratingService.createOrUpdateRating(bookId, ratingDto);
        return ResponseEntity.ok(rating);
    }
    
    @Operation(summary = "Usuń ocenę", description = "Usuwa ocenę książki. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ocena została usunięta pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Ocena nie została znaleziona")
    })
    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Void> deleteRating(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId) {
        ratingService.deleteRating(bookId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Pobierz moją ocenę książki", description = "Zwraca ocenę zalogowanego użytkownika dla danej książki. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocena została znaleziona"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Ocena nie została znaleziona")
    })
    @GetMapping("/books/{bookId}/my-rating")
    public ResponseEntity<RatingDto> getMyRating(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId) {
        return ratingService.getRatingByBookId(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Pobierz oceny książki", description = "Zwraca wszystkie oceny dla danej książki. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista ocen książki została zwrócona pomyślnie")
    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<RatingDto>> getRatingsByBookId(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId) {
        List<RatingDto> ratings = ratingService.getRatingsByBookId(bookId);
        return ResponseEntity.ok(ratings);
    }
    
    @Operation(summary = "Pobierz oceny użytkownika", description = "Zwraca wszystkie oceny wystawione przez użytkownika. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista ocen użytkownika została zwrócona pomyślnie")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<RatingDto>> getRatingsByUserId(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long userId) {
        List<RatingDto> ratings = ratingService.getRatingsByUserId(userId);
        return ResponseEntity.ok(ratings);
    }
}
