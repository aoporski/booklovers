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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImp implements RatingService {
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Override
    @Transactional
    public RatingDto createOrUpdateRating(Long bookId, RatingDto ratingDto) {
        log.info("Tworzenie/aktualizacja oceny: bookId={}, ratingValue={}", bookId, ratingDto.getValue());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Użytkownik oceniający: username={}, bookId={}", username, bookId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas tworzenia oceny: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono książki podczas tworzenia oceny: bookId={}", bookId);
                    return new ResourceNotFoundException("Book", bookId);
                });
        
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndBookId(user.getId(), bookId);
        
        Rating rating;
        if (existingRating.isPresent()) {
            log.debug("Aktualizacja istniejącej oceny: ratingId={}, userId={}, bookId={}", 
                    existingRating.get().getId(), user.getId(), bookId);
            rating = existingRating.get();
            rating.setValue(ratingDto.getValue());
        } else {
            log.debug("Tworzenie nowej oceny: userId={}, bookId={}", user.getId(), bookId);
            rating = Rating.builder()
                    .value(ratingDto.getValue())
                    .user(user)
                    .book(book)
                    .build();
        }
        
        Rating savedRating = ratingRepository.save(rating);
        log.info("Ocena zapisana pomyślnie: ratingId={}, userId={}, bookId={}, value={}", 
                savedRating.getId(), user.getId(), bookId, savedRating.getValue());
        return toDto(savedRating);
    }
    
    @Override
    @Transactional
    public void deleteRating(Long bookId) {
        log.info("Usuwanie oceny: bookId={}", bookId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Użytkownik usuwający ocenę: username={}, bookId={}", username, bookId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas usuwania oceny: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        Rating rating = ratingRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> {
                    log.warn("Nie znaleziono oceny do usunięcia: userId={}, bookId={}", user.getId(), bookId);
                    return new ResourceNotFoundException("Rating", "not found for book: " + bookId);
                });
        
        ratingRepository.delete(rating);
        log.info("Ocena usunięta pomyślnie: ratingId={}, userId={}, bookId={}", 
                rating.getId(), user.getId(), bookId);
    }
    
    @Override
    public Optional<RatingDto> getRatingByBookId(Long bookId) {
        log.debug("Pobieranie oceny użytkownika dla książki: bookId={}", bookId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas pobierania oceny: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        Optional<RatingDto> rating = ratingRepository.findByUserIdAndBookId(user.getId(), bookId)
                .map(this::toDto);
        if (rating.isEmpty()) {
            log.debug("Nie znaleziono oceny: userId={}, bookId={}", user.getId(), bookId);
        }
        return rating;
    }
    
    @Override
    public List<RatingDto> getRatingsByBookId(Long bookId) {
        log.debug("Pobieranie wszystkich ocen dla książki: bookId={}", bookId);
        List<RatingDto> ratings = ratingRepository.findByBookId(bookId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        log.debug("Znaleziono {} ocen dla książki: bookId={}", ratings.size(), bookId);
        return ratings;
    }
    
    @Override
    public List<RatingDto> getRatingsByUserId(Long userId) {
        log.debug("Pobieranie wszystkich ocen użytkownika: userId={}", userId);
        List<RatingDto> ratings = ratingRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        log.debug("Znaleziono {} ocen użytkownika: userId={}", ratings.size(), userId);
        return ratings;
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
