package com.booklovers.service.export;

import com.booklovers.dto.*;
import com.booklovers.entity.User;
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
        assertThat(result).contains("User");
        verify(userRepository).findById(1L);
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
