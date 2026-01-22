package com.booklovers.api.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserDto;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.review.ReviewService;
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
    private final AuthorService authorService;
    private final ReviewService reviewService;
    
    @Operation(summary = "Pobierz wszystkie książki (Admin)", description = "Zwraca listę wszystkich książek w systemie. Wymaga roli ADMIN - tylko administratorzy mają dostęp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista książek została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN")
    })
    @GetMapping("/books")
    public ResponseEntity<List<BookDto>> getAllBooks() {
        List<BookDto> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Utwórz książkę (Admin)", description = "Dodaje nową książkę do systemu. Wymaga roli ADMIN - tylko administratorzy mogą tworzyć książki.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Książka została utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN")
    })
    @PostMapping("/books")
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.createBook(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }
    
    @Operation(summary = "Aktualizuj książkę (Admin)", description = "Aktualizuje dane książki. Wymaga roli ADMIN - tylko administratorzy mogą aktualizować książki.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PutMapping("/books/{id}")
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        BookDto updatedBook = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(updatedBook);
    }
    
    @Operation(summary = "Usuń książkę (Admin)", description = "Usuwa książkę z systemu. Wymaga roli ADMIN - tylko administratorzy mogą usuwać książki.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Książka została usunięta pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Pobierz wszystkich użytkowników (Admin)", description = "Zwraca listę wszystkich użytkowników w systemie. Wymaga roli ADMIN - tylko administratorzy mają dostęp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista użytkowników została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "Usuń użytkownika (Admin)", description = "Usuwa użytkownika z systemu. Wymaga roli ADMIN - tylko administratorzy mogą usuwać użytkowników.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Użytkownik został usunięty pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Zablokuj użytkownika (Admin)", description = "Blokuje konto użytkownika. Zablokowany użytkownik nie może się zalogować. Wymaga roli ADMIN - tylko administratorzy mogą blokować użytkowników.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik został zablokowany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @PutMapping("/users/{id}/block")
    public ResponseEntity<UserDto> blockUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long id) {
        UserDto user = userService.blockUser(id);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Odblokuj użytkownika (Admin)", description = "Odblokowuje konto użytkownika. Odblokowany użytkownik może ponownie się zalogować. Wymaga roli ADMIN - tylko administratorzy mogą odblokowywać użytkowników.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik został odblokowany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie został znaleziony")
    })
    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<UserDto> unblockUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable Long id) {
        UserDto user = userService.unblockUser(id);
        return ResponseEntity.ok(user);
    }
    
    // ========== AUTHOR MANAGEMENT ==========
    
    @Operation(summary = "Pobierz wszystkich autorów (Admin)", description = "Zwraca listę wszystkich autorów w systemie. Wymaga roli ADMIN - tylko administratorzy mają dostęp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista autorów została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN")
    })
    @GetMapping("/authors")
    public ResponseEntity<List<AuthorDto>> getAllAuthors() {
        List<AuthorDto> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Pobierz autora po ID (Admin)", description = "Zwraca szczegóły autora o podanym ID. Wymaga roli ADMIN - tylko administratorzy mają dostęp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor został znaleziony"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Autor nie został znaleziony")
    })
    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorDto> getAuthorById(
            @Parameter(description = "ID autora", required = true) @PathVariable Long id) {
        return authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Utwórz autora (Admin)", description = "Dodaje nowego autora do systemu. Wymaga roli ADMIN - tylko administratorzy mogą tworzyć autorów.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor został utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN")
    })
    @PostMapping("/authors")
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorDto authorDto) {
        AuthorDto createdAuthor = authorService.createAuthor(authorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }
    
    @Operation(summary = "Aktualizuj autora (Admin)", description = "Aktualizuje dane autora. Wymaga roli ADMIN - tylko administratorzy mogą aktualizować autorów.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor został zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Autor nie został znaleziony")
    })
    @PutMapping("/authors/{id}")
    public ResponseEntity<AuthorDto> updateAuthor(
            @Parameter(description = "ID autora", required = true) @PathVariable Long id,
            @Valid @RequestBody AuthorDto authorDto) {
        AuthorDto updatedAuthor = authorService.updateAuthor(id, authorDto);
        return ResponseEntity.ok(updatedAuthor);
    }
    
    @Operation(summary = "Usuń autora (Admin)", description = "Usuwa autora z systemu. Wymaga roli ADMIN - tylko administratorzy mogą usuwać autorów.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Autor został usunięty pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Autor nie został znaleziony")
    })
    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "ID autora", required = true) @PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========== REVIEW MODERATION ==========
    
    @Operation(summary = "Usuń recenzję jako admin (Admin)", description = "Usuwa recenzję z systemu - moderacja treści obraźliwych. Administratorzy mogą usuwać recenzje bez względu na właściciela. Wymaga roli ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recenzja została usunięta pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN"),
            @ApiResponse(responseCode = "404", description = "Recenzja nie została znaleziona")
    })
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReviewAsAdmin(
            @Parameter(description = "ID recenzji", required = true) @PathVariable Long id) {
        reviewService.deleteReviewAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
