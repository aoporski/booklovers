package com.booklovers.service.stats;

import com.booklovers.dto.BookStatsDto;
import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Rating;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StatsServiceImp statsService;

    private User user;
    private Book book;
    private Rating rating;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        rating = Rating.builder()
                .id(1L)
                .value(5)
                .build();
    }

    @Test
    void testGetGlobalStats_Success() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class)).thenReturn(10L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class)).thenReturn(5L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Long.class)).thenReturn(20L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ratings", Long.class)).thenReturn(15L);
        
        when(jdbcTemplate.queryForObject("SELECT AVG(rating_value) FROM ratings", Double.class)).thenReturn(5.0);
        
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            when(rs.getInt("rating_value")).thenReturn(5);
            when(rs.getLong("count")).thenReturn(15L);
            Object row = mapper.mapRow(rs, 0);
            return Arrays.asList(row);
        });

        StatsDto result = statsService.getGlobalStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalBooks()).isEqualTo(10);
        assertThat(result.getTotalUsers()).isEqualTo(5);
        assertThat(result.getTotalReviews()).isEqualTo(20);
        assertThat(result.getTotalRatings()).isEqualTo(15);
        assertThat(result.getAverageRating()).isEqualTo(5.0);
        assertThat(result.getRatingsDistribution()).containsKey(5);
        assertThat(result.getRatingsDistribution().get(5)).isEqualTo(15L);

        for (int i = 1; i <= 5; i++) {
            assertThat(result.getRatingsDistribution()).containsKey(i);
        }
        
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM books", Long.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM users", Long.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM reviews", Long.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM ratings", Long.class);
        verify(jdbcTemplate).queryForObject("SELECT AVG(rating_value) FROM ratings", Double.class);
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class));
    }

    @Test
    void testGetGlobalStats_NoRatings() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class)).thenReturn(10L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class)).thenReturn(5L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Long.class)).thenReturn(20L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ratings", Long.class)).thenReturn(0L);
        when(jdbcTemplate.queryForObject("SELECT AVG(rating_value) FROM ratings", Double.class)).thenReturn(null);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(Collections.emptyList());

        StatsDto result = statsService.getGlobalStats();

        assertThat(result).isNotNull();
        assertThat(result.getAverageRating()).isEqualTo(0.0);
        assertThat(result.getRatingsDistribution()).isNotNull();

        for (int i = 1; i <= 5; i++) {
            assertThat(result.getRatingsDistribution()).containsKey(i);
        }
    }

    @Test
    void testGetUserStats_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userBookRepository.findDistinctShelfNamesByUserId(1L)).thenReturn(Arrays.asList("Shelf1", "Shelf2"));
        when(userBookRepository.countBooksReadInYear(1L, 2026)).thenReturn(5L);

        UserStatsDto result = statsService.getUserStats(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getShelvesCount()).isEqualTo(2);
        verify(userRepository).findById(1L);
        verify(userBookRepository).findDistinctShelfNamesByUserId(1L);
    }

    @Test
    void testGetUserStats_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statsService.getUserStats(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetBookStats_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.countReadersByBookId(1L)).thenReturn(10L);
        when(ratingRepository.countByBookId(1L)).thenReturn(5L);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(4.5);
        when(ratingRepository.findByBookId(1L)).thenReturn(Arrays.asList(rating));

        BookStatsDto result = statsService.getBookStats(1L);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getBookTitle()).isEqualTo("Test Book");
        assertThat(result.getReadersCount()).isEqualTo(10);
        assertThat(result.getRatingsCount()).isEqualTo(5);
        assertThat(result.getAverageRating()).isEqualTo(4.5);
        verify(bookRepository).findById(1L);
        verify(userBookRepository).countReadersByBookId(1L);
        verify(ratingRepository).countByBookId(1L);
        verify(ratingRepository).getAverageRatingByBookId(1L);
    }

    @Test
    void testGetBookStats_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statsService.getBookStats(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book");

        verify(bookRepository).findById(1L);
    }

    @Test
    void testGetBookStats_NoRatings() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBookRepository.countReadersByBookId(1L)).thenReturn(0L);
        when(ratingRepository.countByBookId(1L)).thenReturn(0L);
        when(ratingRepository.getAverageRatingByBookId(1L)).thenReturn(null);
        when(ratingRepository.findByBookId(1L)).thenReturn(Collections.emptyList());

        BookStatsDto result = statsService.getBookStats(1L);

        assertThat(result).isNotNull();
        assertThat(result.getReadersCount()).isEqualTo(0);
        assertThat(result.getRatingsCount()).isEqualTo(0);
        assertThat(result.getAverageRating()).isEqualTo(0.0);
    }
}
