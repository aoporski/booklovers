package com.booklovers.service.rating;

import com.booklovers.dto.RatingDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Rating;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.RatingRepository;
import com.booklovers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImp implements RatingService {
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Override
    @Transactional
    public RatingDto createOrUpdateRating(Long bookId, RatingDto ratingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndBookId(user.getId(), bookId);
        
        Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setValue(ratingDto.getValue());
        } else {
            rating = Rating.builder()
                    .value(ratingDto.getValue())
                    .user(user)
                    .book(book)
                    .build();
        }
        
        Rating savedRating = ratingRepository.save(rating);
        return toDto(savedRating);
    }
    
    @Override
    @Transactional
    public void deleteRating(Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Rating rating = ratingRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "not found for book: " + bookId));
        
        ratingRepository.delete(rating);
    }
    
    @Override
    public Optional<RatingDto> getRatingByBookId(Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        return ratingRepository.findByUserIdAndBookId(user.getId(), bookId)
                .map(this::toDto);
    }
    
    @Override
    public List<RatingDto> getRatingsByBookId(Long bookId) {
        return ratingRepository.findByBookId(bookId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RatingDto> getRatingsByUserId(Long userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private RatingDto toDto(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .value(rating.getValue())
                .userId(rating.getUser() != null ? rating.getUser().getId() : null)
                .username(rating.getUser() != null ? rating.getUser().getUsername() : null)
                .bookId(rating.getBook() != null ? rating.getBook().getId() : null)
                .bookTitle(rating.getBook() != null ? rating.getBook().getTitle() : null)
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
