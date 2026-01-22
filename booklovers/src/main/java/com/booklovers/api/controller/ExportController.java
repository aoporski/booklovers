package com.booklovers.api.controller;

import com.booklovers.dto.UserDataExportDto;
import com.booklovers.service.export.ExportService;
import com.booklovers.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "API do eksportu danych użytkownika")
public class ExportController {
    
    private final ExportService exportService;
    private final UserService userService;
    
    @Operation(summary = "Eksportuj dane użytkownika", description = "Eksportuje dane zalogowanego użytkownika (książki, recenzje, oceny) w formacie DTO. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały zwrócone pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @GetMapping("/user")
    public ResponseEntity<UserDataExportDto> exportCurrentUserData() {
        com.booklovers.dto.UserDto currentUser = userService.getCurrentUser();
        UserDataExportDto data = exportService.exportUserData(currentUser.getId());
        return ResponseEntity.ok(data);
    }
    
    @Operation(summary = "Eksportuj dane użytkownika jako JSON", description = "Eksportuje dane zalogowanego użytkownika w formacie JSON do pobrania. Zwraca plik JSON z wszystkimi danymi użytkownika (książki, recenzje, oceny, półki). Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plik JSON z danymi użytkownika został wygenerowany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony"),
            @ApiResponse(responseCode = "500", description = "Błąd wewnętrzny serwera podczas eksportu danych")
    })
    @GetMapping(value = "/user/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportCurrentUserDataAsJson() {
        com.booklovers.dto.UserDto currentUser = userService.getCurrentUser();
        String json = exportService.exportUserDataAsJson(currentUser.getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "user-data.json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(json);
    }
}
