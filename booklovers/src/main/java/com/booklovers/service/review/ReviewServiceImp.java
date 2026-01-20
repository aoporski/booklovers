package com.booklovers.service.review;

import com.booklovers.dto.RatingDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImp implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RatingService ratingService;
    
    @Override
    @Transactional
    public ReviewDto createReview(Long bookId, ReviewDto reviewDto) {
        log.info("Tworzenie recenzji: bookId={}", bookId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Użytkownik tworzący recenzję: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas tworzenia recenzji: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono książki podczas tworzenia recenzji: bookId={}", bookId);
                    return new ResourceNotFoundException("Book", bookId);
                });
        
        Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(user.getId(), bookId);
        if (existingReview.isPresent()) {
            log.warn("Próba utworzenia duplikatu recenzji: userId={}, bookId={}", user.getId(), bookId);
            throw new ConflictException("User already has a review for this book");
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Review review = Review.builder()
                .id(null) 
                .content(reviewDto.getContent())
                .user(user)
                .book(book)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        reviewRepository.flush();
        
        log.info("Recenzja utworzona pomyślnie: reviewId={}, userId={}, bookId={}", 
                savedReview.getId(), user.getId(), bookId);
        return reviewMapper.toDto(savedReview);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRatingAfterReview(Long bookId, Integer ratingValue) {
        if (ratingValue != null && ratingValue >= 1 && ratingValue <= 5) {
            RatingDto ratingDto = RatingDto.builder()
                    .value(ratingValue)
                    .build();
            ratingService.createOrUpdateRating(bookId, ratingDto);
        }
    }
    
    @Override
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        log.info("Aktualizacja recenzji: reviewId={}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Użytkownik aktualizujący recenzję: username={}, reviewId={}", username, id);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas aktualizacji recenzji: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        Review review = reviewRepository.findByIdWithUser(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono recenzji do aktualizacji: reviewId={}", id);
                    return new ResourceNotFoundException("Review", id);
                });
        
        if (!review.getUser().getId().equals(user.getId())) {
            log.warn("Próba aktualizacji cudzej recenzji: userId={}, reviewId={}, reviewOwnerId={}", 
                    user.getId(), id, review.getUser().getId());
            throw new ForbiddenException("You can only update your own reviews");
        }
        
        if (reviewDto.getContent() != null) {
            review.setContent(reviewDto.getContent());
        }
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Recenzja zaktualizowana pomyślnie: reviewId={}, userId={}", updatedReview.getId(), user.getId());
        return reviewMapper.toDto(updatedReview);
    }
    
    @Override
    @Transactional
    public ReviewDto updateReviewAsAdmin(Long id, ReviewDto reviewDto) {
        log.info("Aktualizacja recenzji przez administratora: reviewId={}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono recenzji do aktualizacji przez admina: reviewId={}", id);
                    return new ResourceNotFoundException("Review", id);
                });
        
        if (reviewDto.getContent() != null) {
            review.setContent(reviewDto.getContent());
        }
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Recenzja zaktualizowana przez administratora: reviewId={}", updatedReview.getId());
        return reviewMapper.toDto(updatedReview);
    }
    
    @Override
    @Transactional
    public void deleteReview(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Review review = reviewRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        
        log.info("Usuwanie recenzji: reviewId={}, userId={}", id, user.getId());
        
        if (!review.getUser().getId().equals(user.getId())) {
            log.warn("Próba usunięcia cudzej recenzji: userId={}, reviewId={}, reviewOwnerId={}", 
                    user.getId(), id, review.getUser().getId());
            throw new ForbiddenException("You can only delete your own reviews");
        }
        
        reviewRepository.deleteById(id);
        reviewRepository.flush();
        log.info("Recenzja usunięta pomyślnie: reviewId={}, userId={}", id, user.getId());
    }
    
    @Override
    @Transactional
    public void deleteReviewAsAdmin(Long id) {
        log.info("Usuwanie recenzji przez administratora: reviewId={}", id);
        if (!reviewRepository.existsById(id)) {
            log.warn("Próba usunięcia nieistniejącej recenzji przez admina: reviewId={}", id);
            throw new ResourceNotFoundException("Review", id);
        }
        
        reviewRepository.deleteById(id);
        reviewRepository.flush();
        log.info("Recenzja usunięta przez administratora: reviewId={}", id);
    }
    
    @Override
    public Optional<ReviewDto> getReviewById(Long id) {
        return reviewRepository.findByIdWithUser(id)
                .map(reviewMapper::toDto);
    }
    
    @Override
    public List<ReviewDto> getReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
