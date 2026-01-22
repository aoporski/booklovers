package com.booklovers.service.import_;

import com.booklovers.dto.*;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.exception.BadRequestException;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookService bookService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private RatingService ratingService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private ImportServiceImp importService;

    private User user;
    private Book book;
    private Book book2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Another Book")
                .author("Another Author")
                .build();
    }

    @Test
    void testImportUserDataFromJson_EmptyData() {
        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("JSON data is empty");

        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("JSON data is empty");

        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("JSON data is empty");
    }

    @Test
    void testImportUserDataFromJson_InvalidJson() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, "invalid json"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid JSON format");
    }

    @Test
    void testImportUserDataFromJson_UserNotFound() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Collections.emptyList())
                .reviews(Collections.emptyList())
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, "{}"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid JSON format");
    }

    @Test
    void testImportUserDataFromJson_SuccessWithBooks() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        UserBookDto userBook = UserBookDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .shelfName("My Shelf")
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Arrays.asList(userBook))
                .reviews(Collections.emptyList())
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        UserBookDto userBookDto = UserBookDto.builder().id(1L).bookId(1L).shelfName("My Shelf").build();
        when(bookService.addBookToUserLibrary(1L, "My Shelf")).thenReturn(userBookDto);

        importService.importUserDataFromJson(1L, "{}");

        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
    }

    @Test
    void testImportUserDataFromJson_BookNotFound() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        UserBookDto userBook = UserBookDto.builder()
                .bookId(null)
                .bookTitle("Non-existent Book")
                .shelfName("My Shelf")
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Arrays.asList(userBook))
                .reviews(Collections.emptyList())
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Non-existent Book")).thenReturn(Collections.emptyList());

        importService.importUserDataFromJson(1L, "{}");

        verify(userRepository).findById(1L);
        verify(bookRepository).searchBooks("Non-existent Book");
        verify(bookService, never()).addBookToUserLibrary(anyLong(), anyString());
    }

    @Test
    void testImportUserDataFromJson_BookAlreadyInLibrary() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        UserBookDto userBook = UserBookDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .shelfName("My Shelf")
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Arrays.asList(userBook))
                .reviews(Collections.emptyList())
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        doThrow(new ConflictException("Book already in library"))
                .when(bookService).addBookToUserLibrary(1L, "My Shelf");

        importService.importUserDataFromJson(1L, "{}");

        verify(bookService).addBookToUserLibrary(1L, "My Shelf");
    }

    @Test
    void testImportUserDataFromJson_SuccessWithReviews() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        ReviewDto review = ReviewDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .content("Great book!")
                .ratingValue(5)
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Collections.emptyList())
                .reviews(Arrays.asList(review))
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        ReviewDto createdReview = ReviewDto.builder().id(1L).build();
        when(reviewService.createReview(1L, review)).thenReturn(createdReview);
        doNothing().when(reviewService).createRatingAfterReview(1L, 5);

        importService.importUserDataFromJson(1L, "{}");

        verify(reviewService).createReview(1L, review);
        verify(reviewService).createRatingAfterReview(1L, 5);
    }

    @Test
    void testImportUserDataFromJson_ReviewAlreadyExists() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        ReviewDto review = ReviewDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .content("Great book!")
                .ratingValue(5)
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Collections.emptyList())
                .reviews(Arrays.asList(review))
                .ratings(Collections.emptyList())
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        doThrow(new ConflictException("Review already exists"))
                .when(reviewService).createReview(1L, review);

        importService.importUserDataFromJson(1L, "{}");

        verify(reviewService).createReview(1L, review);
        verify(reviewService, never()).createRatingAfterReview(anyLong(), anyInt());
    }

    @Test
    void testImportUserDataFromJson_SuccessWithRatings() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        RatingDto rating = RatingDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .value(4)
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Collections.emptyList())
                .reviews(Collections.emptyList())
                .ratings(Arrays.asList(rating))
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        RatingDto createdRating = RatingDto.builder().id(1L).value(4).build();
        when(ratingService.createOrUpdateRating(1L, rating)).thenReturn(createdRating);

        importService.importUserDataFromJson(1L, "{}");

        verify(ratingService).createOrUpdateRating(1L, rating);
    }

    @Test
    void testImportUserDataFromJson_InvalidRatingValue() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        RatingDto rating = RatingDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .value(10)
                .build();

        UserDataExportDto data = UserDataExportDto.builder()
                .userBooks(Collections.emptyList())
                .reviews(Collections.emptyList())
                .ratings(Arrays.asList(rating))
                .build();

        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class))).thenReturn(data);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        importService.importUserDataFromJson(1L, "{}");

        verify(ratingService, never()).createOrUpdateRating(anyLong(), any());
    }

    @Test
    void testImportUserDataFromCsv_EmptyData() {
        assertThatThrownBy(() -> importService.importUserDataFromCsv(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV data is empty");

        assertThatThrownBy(() -> importService.importUserDataFromCsv(1L, ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV data is empty");

        assertThatThrownBy(() -> importService.importUserDataFromCsv(1L, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV data is empty");
    }

    @Test
    void testImportUserDataFromCsv_WithUserData() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "Email,test@test.com\n" +
                "First Name,Test\n" +
                "Last Name,User\n" +
                "Bio,Test bio\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        importService.importUserDataFromCsv(1L, csvData);

        verify(userRepository).findById(1L);
    }

    @Test
    void testImportUserDataFromCsv_WithBooks() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Books\n" +
                "Title,Author,Year,Shelf\n" +
                "Test Book,Test Author,2020,My Shelf\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        UserBookDto userBookDto = UserBookDto.builder().id(1L).bookId(1L).shelfName("My Shelf").build();
        when(bookService.addBookToUserLibrary(1L, "My Shelf")).thenReturn(userBookDto);

        importService.importUserDataFromCsv(1L, csvData);

        verify(bookRepository).searchBooks("Test Book");
    }

    @Test
    void testImportUserDataFromCsv_WithReviews() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Reviews\n" +
                "Title,Content,Rating\n" +
                "Test Book,Great book!,5\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        ReviewDto createdReview = ReviewDto.builder().id(1L).build();
        when(reviewService.createReview(anyLong(), any(ReviewDto.class))).thenReturn(createdReview);
        doNothing().when(reviewService).createRatingAfterReview(1L, 5);

        importService.importUserDataFromCsv(1L, csvData);

        verify(reviewService).createReview(anyLong(), any(ReviewDto.class));
        verify(reviewService).createRatingAfterReview(anyLong(), anyInt());
    }

    @Test
    void testImportUserDataFromCsv_WithRatings() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Ratings\n" +
                "Title,Rating\n" +
                "Test Book,4\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        RatingDto createdRating = RatingDto.builder().id(1L).value(4).build();
        when(ratingService.createOrUpdateRating(anyLong(), any(RatingDto.class))).thenReturn(createdRating);

        importService.importUserDataFromCsv(1L, csvData);

        verify(ratingService).createOrUpdateRating(eq(1L), any(RatingDto.class));
    }

    @Test
    void testImportUserDataFromCsv_WithQuotes() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "Bio,\"Test bio with, comma\"\n" +
                "\n" +
                "Books\n" +
                "Title,Author,Year,Shelf\n" +
                "\"Test Book\",\"Test Author\",2020,\"My Shelf\"\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        UserBookDto userBookDto = UserBookDto.builder().id(1L).bookId(1L).shelfName("My Shelf").build();
        when(bookService.addBookToUserLibrary(1L, "My Shelf")).thenReturn(userBookDto);

        importService.importUserDataFromCsv(1L, csvData);

        verify(bookRepository).searchBooks("Test Book");
    }

    @Test
    void testImportUserDataFromCsv_InvalidRatingValue() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Ratings\n" +
                "Title,Rating\n" +
                "Test Book,10\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        importService.importUserDataFromCsv(1L, csvData);

        verify(ratingService, never()).createOrUpdateRating(anyLong(), any());
    }

    @Test
    void testImportUserDataFromCsv_BookFoundByTitle() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Books\n" +
                "Title,Author,Year,Shelf\n" +
                "Another Book,Another Author,2021,My Shelf\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Another Book")).thenReturn(Arrays.asList(book2));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        UserBookDto userBookDto = UserBookDto.builder().id(1L).bookId(2L).shelfName("My Shelf").build();
        when(bookService.addBookToUserLibrary(2L, "My Shelf")).thenReturn(userBookDto);

        importService.importUserDataFromCsv(1L, csvData);

        verify(bookRepository).searchBooks("Another Book");
        verify(bookService).addBookToUserLibrary(2L, "My Shelf");
    }

    @Test
    void testImportUserDataFromCsv_DefaultShelfName() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Books\n" +
                "Title,Author,Year,Shelf\n" +
                "Test Book,Test Author,2020,\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        UserBookDto userBookDto = UserBookDto.builder().id(1L).bookId(1L).shelfName("Moja biblioteczka").build();
        when(bookService.addBookToUserLibrary(1L, "Moja biblioteczka")).thenReturn(userBookDto);

        importService.importUserDataFromCsv(1L, csvData);

        verify(bookService).addBookToUserLibrary(1L, "Moja biblioteczka");
    }

    @Test
    void testImportUserDataFromCsv_ReviewWithoutRating() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "\n" +
                "Reviews\n" +
                "Title,Content,Rating\n" +
                "Test Book,Great book!,\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.searchBooks("Test Book")).thenReturn(Arrays.asList(book));
        
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        ReviewDto createdReview = ReviewDto.builder().id(1L).build();
        when(reviewService.createReview(anyLong(), any(ReviewDto.class))).thenReturn(createdReview);

        importService.importUserDataFromCsv(1L, csvData);

        verify(reviewService).createReview(anyLong(), any(ReviewDto.class));
        verify(reviewService, never()).createRatingAfterReview(anyLong(), anyInt());
    }
}
