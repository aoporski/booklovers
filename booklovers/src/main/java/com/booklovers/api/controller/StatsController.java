package com.booklovers.api.controller;

import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "API do statystyk czytelnictwa")
public class StatsController {
    
    private final StatsService statsService;
    private final UserService userService;
    
    @Operation(summary = "Pobierz statystyki globalne", description = "Zwraca zagregowane statystyki dla całej platformy")
    @ApiResponse(responseCode = "200", description = "Statystyki globalne")
    @GetMapping("/books")
    public ResponseEntity<StatsDto> getGlobalStats() {
        StatsDto stats = statsService.getGlobalStats();
        return ResponseEntity.ok(stats);
    }
    
    @Operation(summary = "Pobierz statystyki zalogowanego użytkownika", description = "Zwraca statystyki czytelnictwa dla zalogowanego użytkownika")
    @ApiResponse(responseCode = "200", description = "Statystyki użytkownika")
    @GetMapping("/user")
    public ResponseEntity<UserStatsDto> getCurrentUserStats() {
        try {
            com.booklovers.dto.UserDto currentUser = userService.getCurrentUser();
            UserStatsDto stats = statsService.getUserStats(currentUser.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Pobierz statystyki użytkownika", description = "Zwraca statystyki czytelnictwa dla określonego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statystyki użytkownika"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserStatsDto> getUserStats(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long userId) {
        try {
            UserStatsDto stats = statsService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
