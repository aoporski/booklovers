package com.booklovers.service.export;

import com.booklovers.dto.*;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.book.BookMapper;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewMapper;
import com.booklovers.service.review.ReviewService;
import com.booklovers.repository.UserBookRepository;
import com.booklovers.dto.UserBookDto;
import com.booklovers.service.user.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportServiceImp implements ExportService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BookService bookService;
    private final BookMapper bookMapper;
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final RatingService ratingService;
    private final UserBookRepository userBookRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public UserDataExportDto exportUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        UserDto userDto = userMapper.toDto(user);
        List<BookDto> books = bookService.getUserBooks(userId);
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
        List<RatingDto> ratings = ratingService.getRatingsByUserId(userId);
        List<String> shelves = bookService.getUserShelves(userId);
        
        List<UserBookDto> userBooks = userBookRepository.findByUserId(userId).stream()
                .map(ub -> UserBookDto.builder()
                        .id(ub.getId())
                        .userId(ub.getUser().getId())
                        .username(ub.getUser().getUsername())
                        .bookId(ub.getBook().getId())
                        .bookTitle(ub.getBook().getTitle())
                        .bookAuthor(ub.getBook().getAuthor())
                        .shelfName(ub.getShelfName())
                        .addedAt(ub.getAddedAt())
                        .build())
                .collect(Collectors.toList());
        
        return UserDataExportDto.builder()
                .user(userDto)
                .books(books)
                .reviews(reviews)
                .ratings(ratings)
                .shelves(shelves)
                .userBooks(userBooks)
                .build();
    }
    
    @Override
    public String exportUserDataAsJson(Long userId) {
        try {
            UserDataExportDto data = exportUserData(userId);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (Exception e) {
            throw new com.booklovers.exception.BadRequestException("Error exporting user data", e);
        }
    }
}
