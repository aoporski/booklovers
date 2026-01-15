package com.booklovers.service.rating;

import com.booklovers.dto.RatingDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Rating;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.RatingRepository;
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
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RatingServiceImp ratingService;

    private User user;
    private Book book;
    private Rating rating;
    private RatingDto ratingDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("John Doe")
                .build();

        rating = Rating.builder()
                .id(1L)
                .value(5)
                .user(user)
                .book(book)
                .build();

        ratingDto = RatingDto.builder()
                .id(1L)
                .value(5)
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .bookTitle("Test Book")
                .build();

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testCreateOrUpdateRating_CreateNew() {
        RatingDto inputDto = RatingDto.builder()
                .value(5)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        RatingDto result = ratingService.createOrUpdateRating(1L, inputDto);

        assertNotNull(result);
        assertEquals(5, result.getValue());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookRepository, times(1)).findById(1L);
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    void testCreateOrUpdateRating_UpdateExisting() {
        RatingDto inputDto = RatingDto.builder()
                .value(4)
                .build();

        Rating existingRating = Rating.builder()
                .id(1L)
                .value(5)
                .user(user)
                .book(book)
                .build();

        Rating updatedRating = Rating.builder()
                .id(1L)
                .value(4)
                .user(user)
                .book(book)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(updatedRating);

        RatingDto result = ratingService.createOrUpdateRating(1L, inputDto);

        assertNotNull(result);
        assertEquals(4, result.getValue());
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    void testCreateOrUpdateRating_UserNotFound() {
        RatingDto inputDto = RatingDto.builder()
                .value(5)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ratingService.createOrUpdateRating(1L, inputDto);
        });

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testCreateOrUpdateRating_BookNotFound() {
        RatingDto inputDto = RatingDto.builder()
                .value(5)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ratingService.createOrUpdateRating(1L, inputDto);
        });

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testDeleteRating_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(rating));

        ratingService.deleteRating(1L);

        verify(ratingRepository, times(1)).delete(rating);
    }

    @Test
    void testDeleteRating_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ratingService.deleteRating(1L);
        });

        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void testGetRatingByBookId_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(rating));

        Optional<RatingDto> result = ratingService.getRatingByBookId(1L);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getValue());
        verify(ratingRepository, times(1)).findByUserIdAndBookId(1L, 1L);
    }

    @Test
    void testGetRatingByBookId_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        Optional<RatingDto> result = ratingService.getRatingByBookId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRatingsByBookId() {
        when(ratingRepository.findByBookId(1L)).thenReturn(Arrays.asList(rating));

        List<RatingDto> result = ratingService.getRatingsByBookId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getValue());
        verify(ratingRepository, times(1)).findByBookId(1L);
    }

    @Test
    void testGetRatingsByUserId() {
        when(ratingRepository.findByUserId(1L)).thenReturn(Arrays.asList(rating));

        List<RatingDto> result = ratingService.getRatingsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ratingRepository, times(1)).findByUserId(1L);
    }
}
