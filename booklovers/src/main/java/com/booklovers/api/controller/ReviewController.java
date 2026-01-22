package com.booklovers.api.controller;

import com.booklovers.dto.ReviewDto;
import com.booklovers.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API do zarządzania recenzjami książek")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @Operation(summary = "Utwórz recenzję", description = "Dodaje nową recenzję do książki. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recenzja została utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (np. recenzja już istnieje dla tej książki)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PostMapping("/books/{bookId}")
    public ResponseEntity<ReviewDto> createReview(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId,
            @Valid @RequestBody ReviewDto reviewDto) {
        ReviewDto createdReview = reviewService.createReview(bookId, reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }
    
    @Operation(summary = "Aktualizuj recenzję", description = "Aktualizuje recenzję. Tylko właściciel recenzji może ją edytować. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recenzja została zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do edycji recenzji - tylko właściciel może edytować"),
            @ApiResponse(responseCode = "404", description = "Recenzja nie została znaleziona")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(
            @Parameter(description = "ID recenzji", required = true) @PathVariable Long id,
            @Valid @RequestBody ReviewDto reviewDto) {
        ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }
    
    @Operation(summary = "Usuń recenzję", description = "Usuwa recenzję. Tylko właściciel recenzji może ją usunąć. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recenzja została usunięta pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do usunięcia recenzji - tylko właściciel może usunąć"),
            @ApiResponse(responseCode = "404", description = "Recenzja nie została znaleziona")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID recenzji", required = true) @PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Pobierz recenzję po ID", description = "Zwraca szczegóły recenzji o podanym ID. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recenzja została znaleziona"),
            @ApiResponse(responseCode = "404", description = "Recenzja nie została znaleziona")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(
            @Parameter(description = "ID recenzji", required = true) @PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Pobierz recenzje książki", description = "Zwraca wszystkie recenzje dla danej książki. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista recenzji książki została zwrócona pomyślnie")
    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByBookId(
            @Parameter(description = "ID książki", required = true) @PathVariable Long bookId) {
        List<ReviewDto> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }
    
    @Operation(summary = "Pobierz recenzje użytkownika", description = "Zwraca wszystkie recenzje napisane przez użytkownika. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista recenzji użytkownika została zwrócona pomyślnie")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUserId(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }
}
