package com.booklovers.service.review;

import com.booklovers.dto.ReviewDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Review;
import com.booklovers.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

    @InjectMocks
    private ReviewMapper reviewMapper;

    private Review review;
    private ReviewDto reviewDto;
    private User user;
    private Book book;

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
                .author("Test Author")
                .build();

        review = Review.builder()
                .id(1L)
                .content("This is a test review")
                .user(user)
                .book(book)
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2020, 1, 2, 12, 0))
                .build();

        reviewDto = ReviewDto.builder()
                .id(1L)
                .content("This is a test review")
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .bookTitle("Test Book")
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2020, 1, 2, 12, 0))
                .build();
    }

    @Test
    void testToDto_Success() {
        ReviewDto result = reviewMapper.toDto(review);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("This is a test review");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getBookTitle()).isEqualTo("Test Book");
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0));
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 2, 12, 0));
    }

    @Test
    void testToDto_WithNullUser() {
        review.setUser(null);

        ReviewDto result = reviewMapper.toDto(review);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isNull();
        assertThat(result.getUsername()).isNull();
    }

    @Test
    void testToDto_WithNullBook() {
        review.setBook(null);

        ReviewDto result = reviewMapper.toDto(review);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isNull();
        assertThat(result.getBookTitle()).isNull();
    }

    @Test
    void testToDto_WithNullUserAndBook() {
        review.setUser(null);
        review.setBook(null);

        ReviewDto result = reviewMapper.toDto(review);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("This is a test review");
        assertThat(result.getUserId()).isNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getBookId()).isNull();
        assertThat(result.getBookTitle()).isNull();
    }

    @Test
    void testToDto_NullReview() {
        ReviewDto result = reviewMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithMinimalFields() {
        Review minimalReview = Review.builder()
                .id(2L)
                .content("Minimal review")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReviewDto result = reviewMapper.toDto(minimalReview);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getContent()).isEqualTo("Minimal review");
        assertThat(result.getUserId()).isNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getBookId()).isNull();
        assertThat(result.getBookTitle()).isNull();
    }

    @Test
    void testToEntity_Success() {
        Review result = reviewMapper.toEntity(reviewDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("This is a test review");
        // user i book nie są ustawiane w mapperze, tylko w ReviewServiceImp
        assertThat(result.getUser()).isNull();
        assertThat(result.getBook()).isNull();
    }

    @Test
    void testToEntity_WithoutId() {
        ReviewDto dtoWithoutId = ReviewDto.builder()
                .content("Review without ID")
                .build();

        Review result = reviewMapper.toEntity(dtoWithoutId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getContent()).isEqualTo("Review without ID");
    }

    @Test
    void testToEntity_WithId() {
        Review result = reviewMapper.toEntity(reviewDto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testToEntity_NullDto() {
        Review result = reviewMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithMinimalFields() {
        ReviewDto dtoMinimal = ReviewDto.builder()
                .content("Minimal review")
                .build();

        Review result = reviewMapper.toEntity(dtoMinimal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Minimal review");
        assertThat(result.getId()).isNull();
        assertThat(result.getUser()).isNull();
        assertThat(result.getBook()).isNull();
    }

    @Test
    void testToEntity_WithAllFields() {
        Review result = reviewMapper.toEntity(reviewDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("This is a test review");
        // userId, username, bookId, bookTitle nie są mapowane do encji
        // createdAt i updatedAt są ustawiane automatycznie przez JPA
    }
}
