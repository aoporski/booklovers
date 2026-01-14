package com.booklovers.api.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API do zarządzania użytkownikami")
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "Pobierz aktualnego użytkownika", description = "Zwraca dane zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zostały zwrócone"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
  
    }
    
    @Operation(summary = "Aktualizuj profil użytkownika", description = "Aktualizuje dane profilu zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil został zaktualizowany"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UserDto userDto) {
            UserDto updatedUser = userService.updateUser(userDto);
            return ResponseEntity.ok(updatedUser);
    }
    
    @Operation(summary = "Pobierz wszystkich użytkowników", description = "Zwraca listę wszystkich użytkowników w systemie")
    @ApiResponse(responseCode = "200", description = "Lista użytkowników")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "Usuń użytkownika", description = "Usuwa użytkownika z systemu (wymaga autoryzacji)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Użytkownik został usunięty"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long id) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
    }
}
