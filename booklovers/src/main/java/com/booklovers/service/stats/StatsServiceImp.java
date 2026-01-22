package com.booklovers.service.stats;

import com.booklovers.dto.BookStatsDto;
import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImp implements StatsService {
    
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RatingRepository ratingRepository;
    private final com.booklovers.repository.UserBookRepository userBookRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public StatsDto getGlobalStats() {
        log.info("Pobieranie globalnych statystyk");
        
        Long totalBooks = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class);
        Long totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long totalReviews = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Long.class);
        Long totalRatings = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ratings", Long.class);
        
        log.debug("Statystyki: books={}, users={}, reviews={}, ratings={}", 
                totalBooks, totalUsers, totalReviews, totalRatings);
        

        Double averageRating = jdbcTemplate.queryForObject(
            "SELECT AVG(rating_value) FROM ratings", 
            Double.class
        );
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        String sql = "SELECT rating_value, COUNT(*) as count FROM ratings GROUP BY rating_value";
        List<RatingDistributionRow> rows = jdbcTemplate.query(sql, new RatingDistributionRowMapper());
        
        Map<Integer, Long> ratingsDistribution = new HashMap<>();
        for (RatingDistributionRow row : rows) {
            ratingsDistribution.put(row.getRatingValue(), row.getCount());
        }
        for (int i = 1; i <= 5; i++) {
            ratingsDistribution.putIfAbsent(i, 0L);
        }
        
        log.info("Globalne statystyki pobrane: averageRating={}", averageRating);
        return StatsDto.builder()
                .totalBooks(totalBooks != null ? totalBooks.intValue() : 0)
                .totalUsers(totalUsers != null ? totalUsers.intValue() : 0)
                .totalReviews(totalReviews != null ? totalReviews.intValue() : 0)
                .totalRatings(totalRatings != null ? totalRatings.intValue() : 0)
                .averageRating(averageRating)
                .booksByGenre(new HashMap<>())
                .topAuthors(new HashMap<>())
                .ratingsDistribution(ratingsDistribution)
                .build();
    }
    

    private static class RatingDistributionRowMapper implements RowMapper<RatingDistributionRow> {
        @Override
        public RatingDistributionRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return RatingDistributionRow.builder()
                .ratingValue(rs.getInt("rating_value"))
                .count(rs.getLong("count"))
                .build();
        }
    }
    
    @Builder
    @Data
    private static class RatingDistributionRow {
        private Integer ratingValue;
        private Long count;
    }
    
    @Override
    public UserStatsDto getUserStats(Long userId) {
        log.info("Pobieranie statystyk użytkownika: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas pobierania statystyk: userId={}", userId);
                    return new ResourceNotFoundException("User", userId);
                });
        
        int booksRead = user.getUserBooks() != null ? 
            (int) user.getUserBooks().stream()
                .filter(ub -> ub.getBook() != null)
                .map(ub -> ub.getBook().getId())
                .distinct()
                .count() : 0;
        int reviewsWritten = user.getReviews() != null ? user.getReviews().size() : 0;
        int ratingsGiven = user.getRatings() != null ? user.getRatings().size() : 0;
        int shelvesCount = userBookRepository.findDistinctShelfNamesByUserId(userId).size();
        
        double averageRatingGiven = user.getRatings() != null && !user.getRatings().isEmpty() ?
                user.getRatings().stream()
                        .mapToInt(r -> r.getValue())
                        .average()
                        .orElse(0.0) : 0.0;
        
        int currentYear = LocalDate.now().getYear();
        Long booksReadThisYear = userBookRepository.countBooksReadInYear(userId, currentYear);
        
        log.debug("Statystyki użytkownika: userId={}, booksRead={}, reviewsWritten={}, ratingsGiven={}, averageRating={}", 
                userId, booksRead, reviewsWritten, ratingsGiven, averageRatingGiven);
        
        return UserStatsDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .booksRead(booksRead)
                .reviewsWritten(reviewsWritten)
                .ratingsGiven(ratingsGiven)
                .averageRatingGiven(averageRatingGiven)
                .shelvesCount(shelvesCount)
                .favoriteGenresCount(0)
                .booksReadThisYear(booksReadThisYear != null ? booksReadThisYear.intValue() : 0)
                .readingChallengeGoal(null)
                .build();
    }
    
    @Override
    public BookStatsDto getBookStats(Long bookId) {
        log.info("Pobieranie statystyk książki: bookId={}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono książki podczas pobierania statystyk: bookId={}", bookId);
                    return new ResourceNotFoundException("Book", bookId);
                });
        
        Long readersCount = userBookRepository.countReadersByBookId(bookId);
        Long ratingsCount = ratingRepository.countByBookId(bookId);
        Double averageRating = ratingRepository.getAverageRatingByBookId(bookId);
        
        log.debug("Statystyki książki: bookId={}, readersCount={}, ratingsCount={}, averageRating={}", 
                bookId, readersCount, ratingsCount, averageRating);
        
        Map<Integer, Long> ratingsDistribution = new HashMap<>();
        ratingRepository.findByBookId(bookId).forEach(rating -> {
            ratingsDistribution.merge(rating.getValue(), 1L, Long::sum);
        });
        
        for (int i = 1; i <= 5; i++) {
            ratingsDistribution.putIfAbsent(i, 0L);
        }
        
        return BookStatsDto.builder()
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .readersCount(readersCount != null ? readersCount.intValue() : 0)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .ratingsCount(ratingsCount != null ? ratingsCount.intValue() : 0)
                .ratingsDistribution(ratingsDistribution)
                .build();
    }
}
