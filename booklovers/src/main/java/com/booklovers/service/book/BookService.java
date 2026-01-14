package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserBookDto;
import com.booklovers.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<BookDto> getAllBooks();
    Optional<BookDto> getBookById(Long id);
    BookDto createBook(BookDto bookDto);
    BookDto updateBook(Long id, BookDto bookDto);
    void deleteBook(Long id);
    List<BookDto> searchBooks(String query);
    List<BookDto> getUserBooks(Long userId);
    List<BookDto> getUserBooksByShelf(Long userId, String shelfName);
    List<String> getUserShelves(Long userId);
    List<String> getDefaultShelves();
    UserBookDto addBookToUserLibrary(Long bookId, String shelfName);
    void removeBookFromUserLibrary(Long bookId, String shelfName);
    void moveBookToShelf(Long bookId, String fromShelf, String toShelf);
    void deleteShelf(Long userId, String shelfName);
}
