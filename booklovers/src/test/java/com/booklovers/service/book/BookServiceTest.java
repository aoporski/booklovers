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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
}
