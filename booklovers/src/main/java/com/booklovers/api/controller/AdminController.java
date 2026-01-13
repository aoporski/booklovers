package com.booklovers.api.controller;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserDto;
import com.booklovers.service.book.BookService;
import com.booklovers.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "API administracyjne - zarządzanie treścią i użytkownikami (wymaga roli ADMIN)")
public class AdminController {
    
    private final BookService bookService;
    private final UserService userService;
    
    @Operation(summary = "Pobierz wszystkie książki (Admin)", description = "Zwraca listę wszystkich książek (dostępne tylko dla administratorów)")
    @ApiResponse(responseCode = "200", description = "Lista książek")
    @GetMapping("/books")
    public ResponseEntity<List<BookDto>> getAllBooks() {
        List<BookDto> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Utwórz książkę (Admin)", description = "Dodaje nową książkę do systemu (dostępne tylko dla administratorów)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Książka została utworzona"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    @PostMapping("/books")
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        try {
            BookDto createdBook = bookService.createBook(bookDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Aktualizuj książkę (Admin)", description = "Aktualizuje dane książki (dostępne tylko dla administratorów)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PutMapping("/books/{id}")
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        try {
            BookDto updatedBook = bookService.updateBook(id, bookDto);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Usuń książkę (Admin)", description = "Usuwa książkę z systemu (dostępne tylko dla administratorów)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Książka została usunięta"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Pobierz wszystkich użytkowników (Admin)", description = "Zwraca listę wszystkich użytkowników (dostępne tylko dla administratorów)")
    @ApiResponse(responseCode = "200", description = "Lista użytkowników")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "Usuń użytkownika (Admin)", description = "Usuwa użytkownika z systemu (dostępne tylko dla administratorów)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Użytkownik został usunięty"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
