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
import org.springframework.http.HttpStatus;
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
    
    @Operation(summary = "Eksportuj dane użytkownika", description = "Eksportuje dane zalogowanego użytkownika (książki, recenzje, oceny)")
    @ApiResponse(responseCode = "200", description = "Dane użytkownika")
    @GetMapping("/user")
    public ResponseEntity<UserDataExportDto> exportCurrentUserData() {
        try {
            com.booklovers.dto.UserDto currentUser = userService.getCurrentUser();
            UserDataExportDto data = exportService.exportUserData(currentUser.getId());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Eksportuj dane użytkownika jako JSON", description = "Eksportuje dane użytkownika w formacie JSON do pobrania")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plik JSON z danymi użytkownika"),
            @ApiResponse(responseCode = "500", description = "Błąd podczas eksportu")
    })
    @GetMapping(value = "/user/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportCurrentUserDataAsJson() {
        try {
            com.booklovers.dto.UserDto currentUser = userService.getCurrentUser();
            String json = exportService.exportUserDataAsJson(currentUser.getId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "user-data.json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
