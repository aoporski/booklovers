package com.booklovers.service.review;

import com.booklovers.dto.ReviewDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Review;
import com.booklovers.entity.User;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ForbiddenException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.ReviewRepository;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.rating.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RatingService ratingService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewServiceImp reviewService;

    private User user;
    private Book book;
    private Review review;
    private ReviewDto reviewDto;

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

        review = Review.builder()
                .id(1L)
                .content("Great book!")
                .user(user)
                .book(book)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewDto = ReviewDto.builder()
                .id(1L)
                .content("Great book!")
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .bookTitle("Test Book")
                .ratingValue(5)
                .build();

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testCreateReview_Success() {
        ReviewDto inputDto = ReviewDto.builder()
                .content("Great book!")
                .ratingValue(5)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        ReviewDto result = reviewService.createReview(1L, inputDto);

        assertNotNull(result);
        assertEquals("Great book!", result.getContent());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewRepository, times(1)).flush();
    }

    @Test
    void testCreateReview_UserNotFound() {
        ReviewDto inputDto = ReviewDto.builder()
                .content("Great book!")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(1L, inputDto);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_BookNotFound() {
        ReviewDto inputDto = ReviewDto.builder()
                .content("Great book!")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(1L, inputDto);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_AlreadyExists() {
        ReviewDto inputDto = ReviewDto.builder()
                .content("Great book!")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(review));

        assertThrows(ConflictException.class, () -> {
            reviewService.createReview(1L, inputDto);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testUpdateReview_Success() {
        ReviewDto updateDto = ReviewDto.builder()
                .content("Updated review")
                .build();

        Review updatedReview = Review.builder()
                .id(1L)
                .content("Updated review")
                .user(user)
                .book(book)
                .build();

        ReviewDto outputDto = ReviewDto.builder()
                .id(1L)
                .content("Updated review")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(outputDto);

        ReviewDto result = reviewService.updateReview(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated review", result.getContent());
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testUpdateReview_NotOwner() {
        ReviewDto updateDto = ReviewDto.builder()
                .content("Updated review")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .build();

        Review otherReview = Review.builder()
                .id(1L)
                .content("Original review")
                .user(otherUser)
                .book(book)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(otherReview));

        assertThrows(ForbiddenException.class, () -> {
            reviewService.updateReview(1L, updateDto);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testDeleteReview_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewRepository.findByIdWithUser(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.deleteReviewById(1L)).thenReturn(1);
        when(reviewRepository.existsById(1L)).thenReturn(false);

        reviewService.deleteReview(1L);

        verify(reviewRepository).findByIdWithUser(1L);
        verify(reviewRepository).deleteReviewById(1L);
        verify(reviewRepository).flush();
        verify(reviewRepository).existsById(1L);
    }

    @Test
    void testDeleteReview_NotOwner() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .build();

        Review otherReview = Review.builder()
                .id(1L)
                .user(otherUser)
                .book(book)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewRepository.findByIdWithUser(1L)).thenReturn(Optional.of(otherReview));

        assertThrows(ForbiddenException.class, () -> {
            reviewService.deleteReview(1L);
        });

        verify(reviewRepository).findByIdWithUser(1L);
        verify(reviewRepository, never()).delete(any(Review.class));
        verify(reviewRepository, never()).flush();
    }

    @Test
    void testDeleteReviewAsAdmin_Success() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReviewAsAdmin(1L);

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository).deleteById(1L);
        verify(reviewRepository).flush();
    }

    @Test
    void testGetReviewsByBookId() {
        when(reviewRepository.findByBookId(1L)).thenReturn(Arrays.asList(review));
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        List<ReviewDto> result = reviewService.getReviewsByBookId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great book!", result.get(0).getContent());
        verify(reviewRepository, times(1)).findByBookId(1L);
    }

    @Test
    void testCreateRatingAfterReview() {
        Integer ratingValue = 5;

        reviewService.createRatingAfterReview(1L, ratingValue);

        verify(ratingService, times(1)).createOrUpdateRating(eq(1L), any());
    }

    @Test
    void testCreateRatingAfterReview_InvalidRating() {
        Integer ratingValue = 10; // Invalid

        reviewService.createRatingAfterReview(1L, ratingValue);

        verify(ratingService, never()).createOrUpdateRating(anyLong(), any());
    }

    @Test
    void testCreateRatingAfterReview_NullRating() {
        reviewService.createRatingAfterReview(1L, null);

        verify(ratingService, never()).createOrUpdateRating(anyLong(), any());
    }

    @Test
    void testCreateRatingAfterReview_RatingTooLow() {
        Integer ratingValue = 0; // Invalid

        reviewService.createRatingAfterReview(1L, ratingValue);

        verify(ratingService, never()).createOrUpdateRating(anyLong(), any());
    }

    @Test
    void testGetReviewById_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        Optional<ReviewDto> result = reviewService.getReviewById(1L);

        assertTrue(result.isPresent());
        assertEquals("Great book!", result.get().getContent());
        verify(reviewRepository).findById(1L);
        verify(reviewMapper).toDto(review);
    }

    @Test
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ReviewDto> result = reviewService.getReviewById(1L);

        assertFalse(result.isPresent());
        verify(reviewRepository).findById(1L);
        verify(reviewMapper, never()).toDto(any());
    }

    @Test
    void testGetReviewsByUserId() {
        when(reviewRepository.findByUserId(1L)).thenReturn(Arrays.asList(review));
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        List<ReviewDto> result = reviewService.getReviewsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByUserId(1L);
    }

    @Test
    void testGetAllReviews() {
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review));
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        List<ReviewDto> result = reviewService.getAllReviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findAll();
    }

    @Test
    void testDeleteReviewAsAdmin_NotFound() {
        when(reviewRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.deleteReviewAsAdmin(1L);
        });

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
    }
}
