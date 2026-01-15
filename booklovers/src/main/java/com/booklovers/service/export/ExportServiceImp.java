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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    
    private ObjectMapper getConfiguredObjectMapper() {
        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
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
                .filter(ub -> ub.getBook() != null)
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
            ObjectMapper configuredMapper = getConfiguredObjectMapper();
            return configuredMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (Exception e) {
            throw new com.booklovers.exception.BadRequestException("Error exporting user data", e);
        }
    }
    
    @Override
    public String exportUserDataAsCsv(Long userId) {
        UserDataExportDto data = exportUserData(userId);
        StringBuilder csv = new StringBuilder();
        
        csv.append("User Data Export\n");
        csv.append("Username,").append(data.getUser().getUsername()).append("\n");
        csv.append("Email,").append(data.getUser().getEmail()).append("\n");
        csv.append("First Name,").append(data.getUser().getFirstName()).append("\n");
        csv.append("Last Name,").append(data.getUser().getLastName()).append("\n");
        csv.append("Bio,").append(data.getUser().getBio() != null ? data.getUser().getBio().replace(",", ";") : "").append("\n");
        csv.append("\n");
        
        csv.append("Books\n");
        csv.append("Title,Author,ISBN,Shelf,Added At\n");
        for (UserBookDto userBook : data.getUserBooks()) {
            csv.append("\"").append(userBook.getBookTitle() != null ? userBook.getBookTitle().replace("\"", "\"\"") : "").append("\",");
            csv.append("\"").append(userBook.getBookAuthor() != null ? userBook.getBookAuthor().replace("\"", "\"\"") : "").append("\",");
            csv.append(userBook.getBookId() != null ? userBook.getBookId() : "").append(",");
            csv.append("\"").append(userBook.getShelfName() != null ? userBook.getShelfName() : "").append("\",");
            csv.append(userBook.getAddedAt() != null ? userBook.getAddedAt().toString() : "").append("\n");
        }
        csv.append("\n");
        
        csv.append("Reviews\n");
        csv.append("Book Title,Content,Rating,Created At\n");
        for (ReviewDto review : data.getReviews()) {
            csv.append("\"").append(review.getBookTitle() != null ? review.getBookTitle().replace("\"", "\"\"") : "").append("\",");
            csv.append("\"").append(review.getContent() != null ? review.getContent().replace("\"", "\"\"").replace("\n", " ").replace("\r", "") : "").append("\",");
            csv.append(review.getRatingValue() != null ? review.getRatingValue() : "").append(",");
            csv.append(review.getCreatedAt() != null ? review.getCreatedAt().toString() : "").append("\n");
        }
        csv.append("\n");
        
        csv.append("Ratings\n");
        csv.append("Book Title,Rating Value,Created At\n");
        for (RatingDto rating : data.getRatings()) {
            csv.append("\"").append(rating.getBookTitle() != null ? rating.getBookTitle().replace("\"", "\"\"") : "").append("\",");
            csv.append(rating.getValue() != null ? rating.getValue() : "").append(",");
            csv.append(rating.getCreatedAt() != null ? rating.getCreatedAt().toString() : "").append("\n");
        }
        
        return csv.toString();
    }
}
