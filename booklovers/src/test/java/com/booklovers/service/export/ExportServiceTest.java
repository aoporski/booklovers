package com.booklovers.service.export;

import com.booklovers.dto.*;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.UserBookRepository;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.book.BookMapper;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewMapper;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BookService bookService;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private RatingService ratingService;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExportServiceImp exportService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();
    }

    @Test
    void testExportUserData_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Shelf1"));
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        UserDataExportDto result = exportService.exportUserData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(bookService).getUserBooks(1L);
        verify(reviewService).getReviewsByUserId(1L);
        verify(ratingService).getRatingsByUserId(1L);
    }

    @Test
    void testExportUserData_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportUserData(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(1L);
    }

    @Test
    void testExportUserDataAsJson_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        com.fasterxml.jackson.databind.ObjectWriter mockWriter = mock(com.fasterxml.jackson.databind.ObjectWriter.class);
        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any())).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
        when(mockWriter.writeValueAsString(any())).thenReturn("{}");

        String result = exportService.exportUserDataAsJson(1L);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
    }

    @Test
    void testExportUserDataAsCsv_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("User Data Export");
        assertThat(result).contains("Username");
        assertThat(result).contains("Email");
        assertThat(result).contains("Books");
        assertThat(result).contains("Reviews");
        assertThat(result).contains("Ratings");
        verify(userRepository).findById(1L);
    }

    @Test
    void testExportUserDataAsCsv_WithFullData() {
        UserDto fullUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Test bio, with comma")
                .build();

        LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime createdAt = LocalDateTime.of(2024, 2, 1, 12, 0);

        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        UserBook userBookEntity = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName("Przeczytane")
                .addedAt(addedAt)
                .build();

        ReviewDto review = ReviewDto.builder()
                .id(1L)
                .bookTitle("Test Book")
                .content("Great book!")
                .ratingValue(5)
                .createdAt(createdAt)
                .build();

        RatingDto rating = RatingDto.builder()
                .id(1L)
                .bookTitle("Another Book")
                .value(4)
                .createdAt(createdAt)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(fullUserDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Arrays.asList(review));
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Arrays.asList(rating));
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Arrays.asList(userBookEntity));

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("User Data Export");
        assertThat(result).contains("testuser");
        assertThat(result).contains("test@example.com");
        assertThat(result).contains("John");
        assertThat(result).contains("Doe");
        assertThat(result).contains("Test bio; with comma");
        assertThat(result).contains("Test Book");
        assertThat(result).contains("Test Author");
        assertThat(result).contains("Przeczytane");
        assertThat(result).contains("Great book!");
        assertThat(result).contains("5");
        assertThat(result).contains("4");
        assertThat(result).contains("Title,Author,ISBN,Shelf,Added At");
        assertThat(result).contains("Book Title,Content,Rating,Created At");
        assertThat(result).contains("Book Title,Rating Value,Created At");
    }

    @Test
    void testExportUserDataAsCsv_WithNullValues() {
        UserDto userWithNulls = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email(null)
                .firstName(null)
                .lastName(null)
                .bio(null)
                .build();

        UserBookDto userBookWithNulls = UserBookDto.builder()
                .id(1L)
                .bookId(null)
                .bookTitle(null)
                .bookAuthor(null)
                .shelfName("Shelf")
                .addedAt(null)
                .build();

        ReviewDto reviewWithNulls = ReviewDto.builder()
                .id(1L)
                .bookTitle(null)
                .content(null)
                .ratingValue(null)
                .createdAt(null)
                .build();

        RatingDto ratingWithNulls = RatingDto.builder()
                .id(1L)
                .bookTitle(null)
                .value(null)
                .createdAt(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userWithNulls);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Arrays.asList(reviewWithNulls));
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Arrays.asList(ratingWithNulls));
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("testuser");
        assertThat(result).contains("Email,null");
        assertThat(result).contains("First Name,null");
        assertThat(result).contains("Last Name,null");
        assertThat(result).contains("Bio,");
    }

    @Test
    void testExportUserDataAsCsv_WithSpecialCharacters() {
        UserDto userWithSpecialChars = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Bio with, comma and \"quotes\"")
                .build();

        LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime createdAt = LocalDateTime.of(2024, 2, 1, 12, 0);

        Book book = Book.builder()
                .id(1L)
                .title("Book \"Title\" with quotes")
                .author("Author, with comma")
                .build();

        UserBook userBookEntity = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName("Shelf Name")
                .addedAt(addedAt)
                .build();

        ReviewDto review = ReviewDto.builder()
                .id(1L)
                .bookTitle("Book \"Title\"")
                .content("Review with \"quotes\" and\nnew lines")
                .ratingValue(5)
                .createdAt(createdAt)
                .build();

        RatingDto rating = RatingDto.builder()
                .id(1L)
                .bookTitle("Book \"Title\"")
                .value(4)
                .createdAt(createdAt)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userWithSpecialChars);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Arrays.asList(review));
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Arrays.asList(rating));
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Arrays.asList(userBookEntity));

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("Bio with; comma and \"quotes\"");
        assertThat(result).contains("\"Book \"\"Title\"\" with quotes\"");
        assertThat(result).contains("\"Author, with comma\"");
        assertThat(result).contains("Review with \"\"quotes\"\" and new lines");
    }

    @Test
    void testExportUserDataAsCsv_WithEmptyLists() {
        UserDto fullUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Bio")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(fullUserDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("User Data Export");
        assertThat(result).contains("testuser");
        assertThat(result).contains("Books\n");
        assertThat(result).contains("Reviews\n");
        assertThat(result).contains("Ratings\n");
        String[] lines = result.split("\n");
        long bookHeaderLines = java.util.Arrays.stream(lines)
                .filter(line -> line.contains("Title,Author,ISBN,Shelf,Added At"))
                .count();
        assertThat(bookHeaderLines).isEqualTo(1);
    }

    @Test
    void testExportUserDataAsCsv_WithMultipleBooks() {
        UserDto fullUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Bio")
                .build();

        LocalDateTime addedAt1 = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime addedAt2 = LocalDateTime.of(2024, 2, 1, 14, 0);

        Book book1 = Book.builder()
                .id(1L)
                .title("Book 1")
                .author("Author 1")
                .build();

        Book book2 = Book.builder()
                .id(2L)
                .title("Book 2")
                .author("Author 2")
                .build();

        UserBook userBook1 = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book1)
                .shelfName("Shelf 1")
                .addedAt(addedAt1)
                .build();

        UserBook userBook2 = UserBook.builder()
                .id(2L)
                .user(user)
                .book(book2)
                .shelfName("Shelf 2")
                .addedAt(addedAt2)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(fullUserDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Arrays.asList(userBook1, userBook2));

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        assertThat(result).contains("Title,Author,ISBN,Shelf,Added At");
        assertThat(result).contains("Book 1");
        assertThat(result).contains("Book 2");
    }

    @Test
    void testExportUserDataAsCsv_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportUserDataAsCsv(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void testExportUserDataAsCsv_CsvFormat() {
        UserDto fullUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Bio")
                .build();

        LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime createdAt = LocalDateTime.of(2024, 2, 1, 12, 0);

        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        UserBook userBookEntity = UserBook.builder()
                .id(1L)
                .user(user)
                .book(book)
                .shelfName("Przeczytane")
                .addedAt(addedAt)
                .build();

        ReviewDto review = ReviewDto.builder()
                .id(1L)
                .bookTitle("Test Book")
                .content("Great book!")
                .ratingValue(5)
                .createdAt(createdAt)
                .build();

        RatingDto rating = RatingDto.builder()
                .id(1L)
                .bookTitle("Test Book")
                .value(4)
                .createdAt(createdAt)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(fullUserDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Arrays.asList(review));
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Arrays.asList(rating));
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Arrays.asList(userBookEntity));

        String result = exportService.exportUserDataAsCsv(1L);

        assertThat(result).isNotNull();
        String[] lines = result.split("\n");
        assertThat(lines.length).isGreaterThan(5);
        assertThat(lines[0]).isEqualTo("User Data Export");
        assertThat(lines[1]).startsWith("Username,");
        assertThat(result).contains("\n\n");
    }

    @Test
    void testExportUserData_WithBooks() {
        BookDto bookDto = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Arrays.asList(bookDto));
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Shelf1"));
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        UserDataExportDto result = exportService.exportUserData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getBooks()).hasSize(1);
        assertThat(result.getBooks().get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testExportUserData_WithReviews() {
        ReviewDto reviewDto = ReviewDto.builder()
                .id(1L)
                .content("Great book")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Arrays.asList(reviewDto));
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        UserDataExportDto result = exportService.exportUserData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getReviews()).hasSize(1);
        assertThat(result.getReviews().get(0).getContent()).isEqualTo("Great book");
    }

    @Test
    void testExportUserData_WithRatings() {
        RatingDto ratingDto = RatingDto.builder()
                .id(1L)
                .value(5)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Arrays.asList(ratingDto));
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        UserDataExportDto result = exportService.exportUserData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getRatings()).hasSize(1);
        assertThat(result.getRatings().get(0).getValue()).isEqualTo(5);
    }

    @Test
    void testExportUserDataAsJson_Exception() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Collections.emptyList());
        when(reviewService.getReviewsByUserId(1L)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingsByUserId(1L)).thenReturn(Collections.emptyList());
        when(bookService.getUserShelves(1L)).thenReturn(Collections.emptyList());
        when(userBookRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        com.fasterxml.jackson.databind.ObjectWriter mockWriter = mock(com.fasterxml.jackson.databind.ObjectWriter.class);
        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any())).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
        when(mockWriter.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization error"));

        assertThatThrownBy(() -> exportService.exportUserDataAsJson(1L))
                .isInstanceOf(com.booklovers.exception.BadRequestException.class);
    }
}
