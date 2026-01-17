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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        
        Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(user.getId(), bookId);
        if (existingReview.isPresent()) {
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only update your own reviews");
        }
        
        if (reviewDto.getContent() != null) {
            review.setContent(reviewDto.getContent());
        }
        
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toDto(updatedReview);
    }
    
    @Override
    @Transactional
    public ReviewDto updateReviewAsAdmin(Long id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        
        if (reviewDto.getContent() != null) {
            review.setContent(reviewDto.getContent());
        }
        
        Review updatedReview = reviewRepository.save(review);
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
        
        log.error("=== ReviewService.deleteReview START: reviewId={}, currentUserId={}, reviewUserId={} ===", id, user.getId(), review.getUser().getId());
        
        if (!review.getUser().getId().equals(user.getId())) {
            log.error("=== User {} attempted to delete review {} owned by user {} ===", user.getId(), id, review.getUser().getId());
            throw new ForbiddenException("You can only delete your own reviews");
        }
        
        log.error("=== About to delete review {} using native query ===", id);
        int deletedCount = reviewRepository.deleteReviewById(id);
        reviewRepository.flush();
        
        log.error("=== Native delete query executed. Deleted count: {} ===", deletedCount);
        
        boolean stillExists = reviewRepository.existsById(id);
        log.error("=== Review {} deleted. Still exists: {} ===", id, stillExists);
        
        if (stillExists) {
            log.error("=== Review {} was not deleted by native query! Attempting deleteById. ===", id);
            reviewRepository.deleteById(id);
            reviewRepository.flush();
            
            stillExists = reviewRepository.existsById(id);
            if (stillExists) {
                log.error("=== Review {} still exists after deleteById! Attempting delete(review). ===", id);
                reviewRepository.delete(review);
                reviewRepository.flush();
                
                stillExists = reviewRepository.existsById(id);
                log.error("=== Review {} still exists after delete(review): {} ===", id, stillExists);
            }
        }
        
        log.error("=== ReviewService.deleteReview END: reviewId={} ===", id);
    }
    
    @Override
    @Transactional
    public void deleteReviewAsAdmin(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", id);
        }
        
        reviewRepository.deleteById(id);
        reviewRepository.flush();
    }
    
    @Override
    public Optional<ReviewDto> getReviewById(Long id) {
        return reviewRepository.findById(id)
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
