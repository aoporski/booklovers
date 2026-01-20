package com.booklovers.service.stats;

import com.booklovers.dto.BookStatsDto;
import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
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
    
    @Override
    public StatsDto getGlobalStats() {
        log.info("Pobieranie globalnych statystyk");
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalReviews = reviewRepository.count();
        long totalRatings = ratingRepository.count();
        
        log.debug("Statystyki: books={}, users={}, reviews={}, ratings={}", 
                totalBooks, totalUsers, totalReviews, totalRatings);
        
        Double averageRating = ratingRepository.findAll().stream()
                .mapToInt(r -> r.getValue())
                .average()
                .orElse(0.0);
        
        Map<String, Long> booksByGenre = new HashMap<>();
        Map<String, Long> topAuthors = new HashMap<>();
        Map<Integer, Long> ratingsDistribution = new HashMap<>();
        
        ratingRepository.findAll().forEach(rating -> {
            ratingsDistribution.merge(rating.getValue(), 1L, Long::sum);
        });
        
        log.info("Globalne statystyki pobrane: averageRating={}", averageRating);
        return StatsDto.builder()
                .totalBooks((int) totalBooks)
                .totalUsers((int) totalUsers)
                .totalReviews((int) totalReviews)
                .totalRatings((int) totalRatings)
                .averageRating(averageRating)
                .booksByGenre(booksByGenre)
                .topAuthors(topAuthors)
                .ratingsDistribution(ratingsDistribution)
                .build();
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
