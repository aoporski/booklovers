package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import com.booklovers.exception.BadRequestException;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.RatingRepository;
import com.booklovers.repository.ReviewRepository;
import com.booklovers.repository.UserBookRepository;
import com.booklovers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookServiceImp bookService;

    private Book book;
    private BookDto bookDto;
    private Author author;
    private User user;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("John Doe")
                .authorEntity(author)
                .isbn("1234567890")
                .build();

        bookDto = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("John Doe")
                .authorId(1L)
                .isbn("1234567890")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        assertEquals(4.5, result.get(0).getAverageRating());
        verify(bookRepository, times(1)).findAll();
        verify(ratingRepository, times(1)).getAverageRatingByBookId(1L);
    }

    @Test
    void testGetBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        Optional<BookDto> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Book", result.get().getTitle());
        assertEquals(4.5, result.get().getAverageRating());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<BookDto> result = bookService.getBookById(1L);

        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateBook_WithAuthor() {
        BookDto inputDto = BookDto.builder()
                .title("New Book")
                .authorId(1L)
                .build();

        Book newBook = Book.builder()
                .title("New Book")
                .authorEntity(author)
                .build();

        Book savedBook = Book.builder()
                .id(2L)
                .title("New Book")
                .authorEntity(author)
                .author("John Doe")
                .build();

        BookDto outputDto = BookDto.builder()
                .id(2L)
                .title("New Book")
                .author("John Doe")
                .authorId(1L)
                .build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookMapper.toEntity(inputDto)).thenReturn(newBook);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(outputDto);

        BookDto result = bookService.createBook(inputDto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Book", result.getTitle());
        verify(authorRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testCreateBook_AuthorNotFound() {
        BookDto inputDto = BookDto.builder()
                .title("New Book")
                .authorId(999L)
                .build();

        Book newBook = Book.builder()
                .title("New Book")
                .build();

        when(bookMapper.toEntity(inputDto)).thenReturn(newBook);
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.createBook(inputDto);
        });

        verify(authorRepository, times(1)).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testUpdateBook_Success() {
        BookDto updateDto = BookDto.builder()
                .title("Updated Book")
                .authorId(1L)
                .build();

        Book existingBook = Book.builder()
                .id(1L)
                .title("Original Book")
                .build();

        Book updatedBook = Book.builder()
                .id(1L)
                .title("Updated Book")
                .authorEntity(author)
                .author("John Doe")
                .build();

        BookDto outputDto = BookDto.builder()
                .id(1L)
                .title("Updated Book")
                .author("John Doe")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(outputDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.0);

        BookDto result = bookService.updateBook(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Book", result.getTitle());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBook_NotFound() {
        BookDto updateDto = BookDto.builder()
                .title("Updated Book")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.updateBook(1L, updateDto);
        });

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBook() {
        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSearchBooks() {
        String query = "Test";
        when(bookRepository.searchBooks("%" + query + "%")).thenReturn(Arrays.asList(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.searchBooks(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository, times(1)).searchBooks("%" + query + "%");
    }

    @Test
    void testAddBookToUserLibrary_Success() {
        String shelfName = "Przeczytane";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.empty());
        when(userBookRepository.findByUserIdAndShelfName(1L, shelfName))
                .thenReturn(Arrays.asList());

        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);

        com.booklovers.dto.UserBookDto result = bookService.addBookToUserLibrary(1L, shelfName);

        assertNotNull(result);
        assertEquals(shelfName, result.getShelfName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookRepository, times(1)).findById(1L);
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }

    @Test
    void testAddBookToUserLibrary_BookAlreadyExists() {
        String shelfName = "Przeczytane";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.of(UserBook.builder().build()));

        assertThrows(ConflictException.class, () -> {
            bookService.addBookToUserLibrary(1L, shelfName);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void testCreateShelf_Success() {
        String shelfName = "Moja półka";
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userBookRepository.findDistinctShelfNamesByUserId(1L))
                .thenReturn(Arrays.asList("Przeczytane"));

        UserBook emptyShelf = UserBook.builder()
                .user(user)
                .book(null)
                .shelfName(shelfName)
                .build();

        when(userBookRepository.save(any(UserBook.class))).thenReturn(emptyShelf);

        bookService.createShelf(1L, shelfName);

        verify(userRepository, times(1)).findById(1L);
        verify(userBookRepository, times(1)).save(any(UserBook.class));
    }

    @Test
    void testCreateShelf_DefaultShelf() {
        String shelfName = "Przeczytane";
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> {
            bookService.createShelf(1L, shelfName);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void testCreateShelf_AlreadyExists() {
        String shelfName = "Moja półka";
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userBookRepository.findDistinctShelfNamesByUserId(1L))
                .thenReturn(Arrays.asList("Przeczytane", shelfName));

        assertThrows(ConflictException.class, () -> {
            bookService.createShelf(1L, shelfName);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void testDeleteShelf_Success() {
        String shelfName = "Moja półka";
        when(userBookRepository.findByUserIdAndShelfName(1L, shelfName))
                .thenReturn(Arrays.asList(
                        UserBook.builder().id(1L).user(user).book(book).shelfName(shelfName).build()
                ));
        doNothing().when(userBookRepository).deleteAll(any());
        doNothing().when(userBookRepository).flush();

        bookService.deleteShelf(1L, shelfName);

        verify(userBookRepository).findByUserIdAndShelfName(1L, shelfName);
        verify(userBookRepository).deleteAll(any());
        verify(userBookRepository).flush();
    }

    @Test
    void testDeleteShelf_DefaultShelf() {
        String shelfName = "Przeczytane";

        assertThrows(BadRequestException.class, () -> {
            bookService.deleteShelf(1L, shelfName);
        });

        verify(userBookRepository, never()).deleteAll(any());
    }

    @Test
    void testGetUserBooks_Success() {
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName("Przeczytane")
                .build();

        when(userBookRepository.findByUserId(1L)).thenReturn(Arrays.asList(userBook));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.getUserBooks(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userBookRepository).findByUserId(1L);
    }

    @Test
    void testGetUserBooksByShelf_Success() {
        String shelfName = "Przeczytane";
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userBookRepository.findByUserIdAndShelfName(1L, shelfName))
                .thenReturn(Arrays.asList(userBook));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.getUserBooksByShelf(1L, shelfName);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userBookRepository).findByUserIdAndShelfName(1L, shelfName);
    }

    @Test
    void testMoveBookToShelf_Success() {
        String oldShelf = "Przeczytane";
        String newShelf = "Chcę przeczytać";
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(oldShelf)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, oldShelf))
                .thenReturn(Optional.of(userBook));
        when(userBookRepository.findByUserIdAndShelfName(1L, newShelf))
                .thenReturn(Collections.emptyList());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(userBook);

        bookService.moveBookToShelf(1L, oldShelf, newShelf);

        verify(userBookRepository).findByUserIdAndBookIdAndShelfName(1L, 1L, oldShelf);
        verify(userBookRepository).save(any(UserBook.class));
    }

    @Test
    void testGetUserShelves_Success() {
        java.util.ArrayList<String> shelves = new java.util.ArrayList<>();
        shelves.add("Przeczytane");
        shelves.add("Chcę przeczytać");
        when(userBookRepository.findDistinctShelfNamesByUserId(1L))
                .thenReturn(shelves);

        List<String> result = bookService.getUserShelves(1L);

        assertNotNull(result);
        assertTrue(result.contains("Przeczytane"));
        assertTrue(result.contains("Chcę przeczytać"));
        verify(userBookRepository).findDistinctShelfNamesByUserId(1L);
    }

    @Test
    void testGetUserShelves_Empty() {
        when(userBookRepository.findDistinctShelfNamesByUserId(1L))
                .thenReturn(Collections.emptyList());

        List<String> result = bookService.getUserShelves(1L);

        assertNotNull(result);
        assertEquals(3, result.size()); // Should return default shelves
        assertTrue(result.contains("Przeczytane"));
        assertTrue(result.contains("Chcę przeczytać"));
        assertTrue(result.contains("Teraz czytam"));
    }

    @Test
    void testGetDefaultShelves() {
        List<String> result = bookService.getDefaultShelves();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Przeczytane"));
        assertTrue(result.contains("Chcę przeczytać"));
        assertTrue(result.contains("Teraz czytam"));
    }

    @Test
    void testUpdateBook_WithAuthorFallback() {
        BookDto updateDto = BookDto.builder()
                .title("Updated Book")
                .author("Fallback Author")
                .build();

        Book existingBook = Book.builder()
                .id(1L)
                .title("Original Book")
                .build();

        Book updatedBook = Book.builder()
                .id(1L)
                .title("Updated Book")
                .author("Fallback Author")
                .build();

        BookDto outputDto = BookDto.builder()
                .id(1L)
                .title("Updated Book")
                .author("Fallback Author")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(outputDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.0);

        BookDto result = bookService.updateBook(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Book", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testUpdateBook_WithAllFields() {
        java.time.LocalDate publicationDate = java.time.LocalDate.of(2020, 1, 15);
        
        BookDto updateDto = BookDto.builder()
                .title("Updated Book")
                .isbn("978-1234567890")
                .description("Updated description")
                .publisher("Updated Publisher")
                .publicationDate(publicationDate)
                .pageCount(300)
                .language("English")
                .coverImageUrl("https://example.com/cover.jpg")
                .build();

        Book existingBook = Book.builder()
                .id(1L)
                .title("Original Book")
                .isbn("1234567890")
                .description("Original description")
                .publisher("Original Publisher")
                .publicationDate(java.time.LocalDate.of(2019, 1, 1))
                .pageCount(200)
                .language("Polish")
                .coverImageUrl("https://example.com/old-cover.jpg")
                .build();

        Book updatedBook = Book.builder()
                .id(1L)
                .title("Updated Book")
                .isbn("978-1234567890")
                .description("Updated description")
                .publisher("Updated Publisher")
                .publicationDate(publicationDate)
                .pageCount(300)
                .language("English")
                .coverImageUrl("https://example.com/cover.jpg")
                .build();

        BookDto outputDto = BookDto.builder()
                .id(1L)
                .title("Updated Book")
                .isbn("978-1234567890")
                .description("Updated description")
                .publisher("Updated Publisher")
                .publicationDate(publicationDate)
                .pageCount(300)
                .language("English")
                .coverImageUrl("https://example.com/cover.jpg")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(outputDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        BookDto result = bookService.updateBook(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Book", result.getTitle());
        assertEquals("978-1234567890", result.getIsbn());
        assertEquals("Updated description", result.getDescription());
        assertEquals("Updated Publisher", result.getPublisher());
        assertEquals(publicationDate, result.getPublicationDate());
        assertEquals(300, result.getPageCount());
        assertEquals("English", result.getLanguage());
        assertEquals("https://example.com/cover.jpg", result.getCoverImageUrl());
        assertEquals(4.5, result.getAverageRating());
        
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
        verify(ratingRepository).getAverageRatingByBookId(1L);
    }

    @Test
    void testRemoveBookFromUserLibrary_Success() {
        String shelfName = "Przeczytane";
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.of(userBook));
        doNothing().when(userBookRepository).delete(any(UserBook.class));

        bookService.removeBookFromUserLibrary(1L, shelfName);

        verify(userRepository).findByUsername("testuser");
        verify(userBookRepository).findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName);
        verify(userBookRepository).delete(userBook);
    }

    @Test
    void testRemoveBookFromUserLibrary_DefaultShelf() {
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName("Moja biblioteczka")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, "Moja biblioteczka"))
                .thenReturn(Optional.of(userBook));
        doNothing().when(userBookRepository).delete(any(UserBook.class));

        bookService.removeBookFromUserLibrary(1L, null);

        verify(userBookRepository).findByUserIdAndBookIdAndShelfName(1L, 1L, "Moja biblioteczka");
        verify(userBookRepository).delete(userBook);
    }

    @Test
    void testRemoveBookFromUserLibrary_NotFound() {
        String shelfName = "Przeczytane";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.removeBookFromUserLibrary(1L, shelfName);
        });

        verify(userBookRepository, never()).delete(any(UserBook.class));
    }

    @Test
    void testMoveBookToShelf_BookAlreadyInTargetShelf() {
        String oldShelf = "Przeczytane";
        String newShelf = "Chcę przeczytać";
        UserBook userBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(oldShelf)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, oldShelf))
                .thenReturn(Optional.of(userBook));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, newShelf))
                .thenReturn(Optional.of(UserBook.builder().build()));

        assertThrows(ConflictException.class, () -> {
            bookService.moveBookToShelf(1L, oldShelf, newShelf);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void testSearchBooks_EmptyQuery() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.searchBooks("");

        assertNotNull(result);
        verify(bookRepository).findAll();
    }

    @Test
    void testSearchBooks_NullQuery() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);

        List<BookDto> result = bookService.searchBooks(null);

        assertNotNull(result);
        verify(bookRepository).findAll();
    }

    @Test
    void testAddBookToUserLibrary_NewBookToDefaultShelf() {
        String shelfName = "Przeczytane";
        UserBook savedUserBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.empty());
        when(userBookRepository.findByUserIdAndBookId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserIdAndShelfName(1L, shelfName))
                .thenReturn(Collections.emptyList());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(savedUserBook);

        com.booklovers.dto.UserBookDto result = bookService.addBookToUserLibrary(1L, shelfName);

        assertNotNull(result);
        assertEquals(shelfName, result.getShelfName());
        verify(userBookRepository).save(any(UserBook.class));
    }

    @Test
    void testAddBookToUserLibrary_MoveFromDefaultShelfToAnotherDefaultShelf() {
        String fromShelf = "Przeczytane";
        String toShelf = "Chcę przeczytać";
        UserBook existingUserBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(fromShelf)
                .build();
        UserBook movedUserBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(toShelf)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookId(1L, 1L))
                .thenReturn(Arrays.asList(existingUserBook));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(eq(1L), eq(1L), eq(toShelf)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(movedUserBook));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(eq(1L), eq(1L), eq(fromShelf)))
                .thenReturn(Optional.of(existingUserBook));
        when(userBookRepository.findByUserIdAndShelfName(eq(1L), eq(toShelf)))
                .thenReturn(Collections.emptyList());
        lenient().when(userBookRepository.findByUserIdAndShelfName(eq(1L), eq(fromShelf)))
                .thenReturn(Collections.emptyList());
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(invocation -> {
            UserBook ub = invocation.getArgument(0);
            ub.setShelfName(toShelf);
            return ub;
        });

        com.booklovers.dto.UserBookDto result = bookService.addBookToUserLibrary(1L, toShelf);

        assertNotNull(result);
        assertEquals(toShelf, result.getShelfName());
        verify(userBookRepository, atLeastOnce()).save(any(UserBook.class));
    }

    @Test
    void testAddBookToUserLibrary_AddToCustomShelfWhenInDefaultShelf() {
        String defaultShelf = "Przeczytane";
        String customShelf = "Moja biblioteczka";
        UserBook newUserBook = UserBook.builder()
                .id(2L)
                .user(user)
                .book(book)
                .shelfName(customShelf)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, customShelf))
                .thenReturn(Optional.empty());
        when(userBookRepository.findByUserIdAndShelfName(1L, customShelf))
                .thenReturn(Collections.emptyList());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(newUserBook);

        com.booklovers.dto.UserBookDto result = bookService.addBookToUserLibrary(1L, customShelf);

        assertNotNull(result);
        assertEquals(customShelf, result.getShelfName());
        verify(userBookRepository).save(any(UserBook.class));
    }

    @Test
    void testAddBookToUserLibrary_AlreadyInSameShelf() {
        String shelfName = "Przeczytane";
        UserBook existingUserBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.of(existingUserBook));

        assertThrows(ConflictException.class, () -> {
            bookService.addBookToUserLibrary(1L, shelfName);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void testAddBookToUserLibrary_MoveFromDefaultShelfToSameDefaultShelf() {
        String shelfName = "Przeczytane";
        UserBook existingUserBook = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.findByUserIdAndBookIdAndShelfName(1L, 1L, shelfName))
                .thenReturn(Optional.of(existingUserBook));

        assertThrows(ConflictException.class, () -> {
            bookService.addBookToUserLibrary(1L, shelfName);
        });

        verify(userBookRepository, never()).save(any(UserBook.class));
    }
}
