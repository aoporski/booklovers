package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.Rating;
import com.booklovers.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BookMapperTest {

    @InjectMocks
    private BookMapper bookMapper;

    private Book book;
    private BookDto bookDto;
    private Author author;

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
                .author("Test Author")
                .authorEntity(author)
                .isbn("978-0-123456-78-9")
                .description("Test description")
                .publisher("Test Publisher")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .pageCount(300)
                .language("English")
                .coverImageUrl("http://example.com/cover.jpg")
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .build();

        bookDto = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .authorId(1L)
                .isbn("978-0-123456-78-9")
                .description("Test description")
                .publisher("Test Publisher")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .pageCount(300)
                .language("English")
                .coverImageUrl("http://example.com/cover.jpg")
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .build();
    }

    @Test
    void testToDto_Success() {
        BookDto result = bookMapper.toDto(book);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("John Doe"); // Pełne imię z authorEntity
        assertThat(result.getAuthorId()).isEqualTo(1L);
        assertThat(result.getIsbn()).isEqualTo("978-0-123456-78-9");
        assertThat(result.getDescription()).isEqualTo("Test description");
        assertThat(result.getPublisher()).isEqualTo("Test Publisher");
        assertThat(result.getPublicationDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(result.getPageCount()).isEqualTo(300);
        assertThat(result.getLanguage()).isEqualTo("English");
        assertThat(result.getCoverImageUrl()).isEqualTo("http://example.com/cover.jpg");
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0));
    }

    @Test
    void testToDto_WithAuthorEntity() {
        BookDto result = bookMapper.toDto(book);

        assertThat(result.getAuthor()).isEqualTo("John Doe");
        assertThat(result.getAuthorId()).isEqualTo(1L);
    }

    @Test
    void testToDto_WithoutAuthorEntity() {
        Book bookWithoutAuthor = Book.builder()
                .id(2L)
                .title("Book Without Author Entity")
                .author("Fallback Author")
                .authorEntity(null)
                .build();

        BookDto result = bookMapper.toDto(bookWithoutAuthor);

        assertThat(result).isNotNull();
        assertThat(result.getAuthor()).isEqualTo("Fallback Author");
        assertThat(result.getAuthorId()).isNull();
    }

    @Test
    void testToDto_WithRatingsAndReviews() {
        Rating rating1 = Rating.builder().id(1L).build();
        Rating rating2 = Rating.builder().id(2L).build();
        Review review1 = Review.builder().id(1L).build();

        book.setRatings(Arrays.asList(rating1, rating2));
        book.setReviews(Arrays.asList(review1));

        BookDto result = bookMapper.toDto(book);

        assertThat(result.getRatingsCount()).isEqualTo(2);
        assertThat(result.getReviewsCount()).isEqualTo(1);
    }

    @Test
    void testToDto_WithNullRatingsAndReviews() {
        book.setRatings(null);
        book.setReviews(null);

        BookDto result = bookMapper.toDto(book);

        assertThat(result.getRatingsCount()).isEqualTo(0);
        assertThat(result.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void testToDto_WithEmptyRatingsAndReviews() {
        book.setRatings(new ArrayList<>());
        book.setReviews(new ArrayList<>());

        BookDto result = bookMapper.toDto(book);

        assertThat(result.getRatingsCount()).isEqualTo(0);
        assertThat(result.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void testToDto_NullBook() {
        BookDto result = bookMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithoutOptionalFields() {
        Book bookMinimal = Book.builder()
                .id(2L)
                .title("Minimal Book")
                .author("Minimal Author")
                .createdAt(LocalDateTime.now())
                .build();

        BookDto result = bookMapper.toDto(bookMinimal);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Minimal Book");
        assertThat(result.getAuthor()).isEqualTo("Minimal Author");
        assertThat(result.getAuthorId()).isNull();
        assertThat(result.getIsbn()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPublisher()).isNull();
        assertThat(result.getPublicationDate()).isNull();
        assertThat(result.getPageCount()).isNull();
        assertThat(result.getLanguage()).isNull();
        assertThat(result.getCoverImageUrl()).isNull();
    }

    @Test
    void testToEntity_Success() {
        Book result = bookMapper.toEntity(bookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("Test Author");
        assertThat(result.getIsbn()).isEqualTo("978-0-123456-78-9");
        assertThat(result.getDescription()).isEqualTo("Test description");
        assertThat(result.getPublisher()).isEqualTo("Test Publisher");
        assertThat(result.getPublicationDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(result.getPageCount()).isEqualTo(300);
        assertThat(result.getLanguage()).isEqualTo("English");
        assertThat(result.getCoverImageUrl()).isEqualTo("http://example.com/cover.jpg");
        // authorEntity nie jest ustawiane w mapperze, tylko w BookServiceImp
        assertThat(result.getAuthorEntity()).isNull();
    }

    @Test
    void testToEntity_NullDto() {
        Book result = bookMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithoutOptionalFields() {
        BookDto dtoMinimal = BookDto.builder()
                .id(2L)
                .title("Minimal Book")
                .author("Minimal Author")
                .build();

        Book result = bookMapper.toEntity(dtoMinimal);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Minimal Book");
        assertThat(result.getAuthor()).isEqualTo("Minimal Author");
        assertThat(result.getIsbn()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPublisher()).isNull();
        assertThat(result.getPublicationDate()).isNull();
        assertThat(result.getPageCount()).isNull();
        assertThat(result.getLanguage()).isNull();
        assertThat(result.getCoverImageUrl()).isNull();
        assertThat(result.getAuthorEntity()).isNull();
    }
}
