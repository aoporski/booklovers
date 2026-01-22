package com.booklovers.api.controller;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserBookDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "API do zarządzania książkami i biblioteczką użytkownika")
public class BookController {
    
    private final BookService bookService;
    private final UserService userService;
    
    @Operation(summary = "Pobierz wszystkie książki", description = "Zwraca listę wszystkich książek w systemie. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista książek została zwrócona pomyślnie")
    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        List<BookDto> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Pobierz książkę po ID", description = "Zwraca szczegóły książki o podanym ID (tytuł, autor, opis, oceny, recenzje). Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została znaleziona"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@Parameter(description = "ID książki", required = true) @PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Utwórz nową książkę", description = "Dodaje nową książkę do systemu. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Książka została utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany")
    })
    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.createBook(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }
    
    @Operation(summary = "Aktualizuj książkę", description = "Aktualizuje dane książki o podanym ID. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        BookDto updatedBook = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(updatedBook);
    }
    
    @Operation(summary = "Usuń książkę", description = "Usuwa książkę z systemu. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Książka została usunięta pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Wyszukaj książki", description = "Wyszukuje książki po tytule, autorze lub ISBN. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponse(responseCode = "200", description = "Lista znalezionych książek")
    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(
            @Parameter(description = "Zapytanie wyszukiwania", required = true) @RequestParam String q) {
        List<BookDto> books = bookService.searchBooks(q);
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Pobierz moje książki", description = "Zwraca listę książek w biblioteczce zalogowanego użytkownika. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista książek użytkownika została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany")
    })
    @GetMapping("/my-books")
    public ResponseEntity<List<BookDto>> getMyBooks() {
        UserDto currentUser = userService.getCurrentUser();
        List<BookDto> books = bookService.getUserBooks(currentUser.getId());
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Dodaj książkę do biblioteczki", description = "Dodaje książkę do biblioteczki użytkownika na określoną półkę (np. 'Przeczytane', 'Czytam', 'Chcę przeczytać'). Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została dodana do biblioteczki pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (np. książka już jest na tej półce)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PostMapping("/{id}/add-to-library")
    public ResponseEntity<UserBookDto> addBookToLibrary(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Parameter(description = "Nazwa półki", required = false) @RequestParam(defaultValue = "Moja biblioteczka") String shelfName) {
        UserBookDto userBook = bookService.addBookToUserLibrary(id, shelfName);
        return ResponseEntity.ok(userBook);
    }
    
    @Operation(summary = "Usuń książkę z biblioteczki", description = "Usuwa książkę z biblioteczki użytkownika z określonej półki. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Książka została usunięta z biblioteczki pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (np. książka nie jest na tej półce)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @DeleteMapping("/{id}/remove-from-library")
    public ResponseEntity<Void> removeBookFromLibrary(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Parameter(description = "Nazwa półki", required = false) @RequestParam(defaultValue = "Moja biblioteczka") String shelfName) {
        bookService.removeBookFromUserLibrary(id, shelfName);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Pobierz moje półki", description = "Zwraca listę wszystkich półek użytkownika (np. 'Przeczytane', 'Czytam', 'Chcę przeczytać', 'Moja biblioteczka'). Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista półek użytkownika została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany")
    })
    @GetMapping("/my-books/shelves")
    public ResponseEntity<List<String>> getMyShelves() {
        UserDto currentUser = userService.getCurrentUser();
        List<String> shelves = bookService.getUserShelves(currentUser.getId());
        return ResponseEntity.ok(shelves);
    }
    
    @Operation(summary = "Pobierz książki z półki", description = "Zwraca listę książek z określonej półki użytkownika. Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista książek z półki została zwrócona pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany")
    })
    @GetMapping("/my-books/shelves/{shelfName}")
    public ResponseEntity<List<BookDto>> getMyBooksByShelf(
            @Parameter(description = "Nazwa półki", required = true) @PathVariable String shelfName) {
        UserDto currentUser = userService.getCurrentUser();
        List<BookDto> books = bookService.getUserBooksByShelf(currentUser.getId(), shelfName);
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Przenieś książkę między półkami", description = "Przenosi książkę z jednej półki na drugą (np. z 'Chcę przeczytać' na 'Czytam'). Wymaga autoryzacji - użytkownik musi być zalogowany.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została przeniesiona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (np. książka nie jest na źródłowej półce)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji - użytkownik nie jest zalogowany"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PutMapping("/{id}/move-to-shelf")
    public ResponseEntity<Void> moveBookToShelf(
            @Parameter(description = "ID książki", required = true) @PathVariable Long id,
            @Parameter(description = "Nazwa półki źródłowej", required = true) @RequestParam String fromShelf,
            @Parameter(description = "Nazwa półki docelowej", required = true) @RequestParam String toShelf) {
        try {
            bookService.moveBookToShelf(id, fromShelf, toShelf);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
