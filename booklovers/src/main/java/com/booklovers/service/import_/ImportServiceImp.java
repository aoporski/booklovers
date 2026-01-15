package com.booklovers.service.import_;

import com.booklovers.dto.*;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.exception.BadRequestException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImp implements ImportService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final ReviewService reviewService;
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    
    private TransactionTemplate getTransactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
    
    private ObjectMapper getConfiguredObjectMapper() {
        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    @Override
    @Transactional(noRollbackFor = {com.booklovers.exception.ConflictException.class})
    public void importUserDataFromJson(Long userId, String jsonData) {
        try {
            ObjectMapper configuredMapper = getConfiguredObjectMapper();
            UserDataExportDto data = configuredMapper.readValue(jsonData, UserDataExportDto.class);
            importUserData(userId, data);
        } catch (Exception e) {
            log.error("Error importing user data from JSON: ", e);
            throw new BadRequestException("Invalid JSON format: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(noRollbackFor = {com.booklovers.exception.ConflictException.class})
    public void importUserDataFromCsv(Long userId, String csvData) {
        try {
            UserDataExportDto data = parseCsvData(csvData);
            importUserData(userId, data);
        } catch (Exception e) {
            log.error("Error importing user data from CSV: ", e);
            throw new BadRequestException("Invalid CSV format: " + e.getMessage());
        }
    }
    
    private void importUserData(Long userId, UserDataExportDto data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (data.getUserBooks() != null) {
            for (UserBookDto userBookDto : data.getUserBooks()) {
                try {
                    Book book = null;
                    if (userBookDto.getBookId() != null) {
                        book = bookRepository.findById(userBookDto.getBookId()).orElse(null);
                    }
                    if (book == null && userBookDto.getBookTitle() != null) {
                        List<Book> books = bookRepository.searchBooks(userBookDto.getBookTitle());
                        book = books.stream()
                                .filter(b -> b.getTitle().equalsIgnoreCase(userBookDto.getBookTitle()))
                                .findFirst()
                                .orElse(null);
                    }
                    
                    if (book != null) {
                        final Book finalBook = book;
                        final String finalShelfName = userBookDto.getShelfName() != null ? userBookDto.getShelfName() : "Moja biblioteczka";
                        try {
                            getTransactionTemplate().execute(status -> {
                                try {
                                    bookService.addBookToUserLibrary(finalBook.getId(), finalShelfName);
                                } catch (com.booklovers.exception.ConflictException e) {
                                    log.debug("Book already in library: {}", finalBook.getId());
                                    status.setRollbackOnly();
                                } catch (Exception e) {
                                    log.warn("Error adding book to library: {}", e.getMessage());
                                    status.setRollbackOnly();
                                }
                                return null;
                            });
                        } catch (Exception e) {
                            log.warn("Transaction failed for book {}: {}", finalBook.getId(), e.getMessage());
                        }
                    } else {
                        log.warn("Book not found: {}", userBookDto.getBookTitle());
                    }
                } catch (Exception e) {
                    log.warn("Error importing book {}: {}", userBookDto.getBookTitle(), e.getMessage());
                }
            }
        }
        
        if (data.getReviews() != null) {
            for (ReviewDto reviewDto : data.getReviews()) {
                try {
                    Book book = null;
                    if (reviewDto.getBookId() != null) {
                        book = bookRepository.findById(reviewDto.getBookId()).orElse(null);
                    }
                    if (book == null && reviewDto.getBookTitle() != null) {
                        List<Book> books = bookRepository.searchBooks(reviewDto.getBookTitle());
                        book = books.stream()
                                .filter(b -> b.getTitle().equalsIgnoreCase(reviewDto.getBookTitle()))
                                .findFirst()
                                .orElse(null);
                    }
                    
                    if (book != null) {
                        final Book finalBook = book;
                        final ReviewDto finalReviewDto = reviewDto;
                        try {
                            getTransactionTemplate().execute(status -> {
                                try {
                                    reviewService.createReview(finalBook.getId(), finalReviewDto);
                                    if (finalReviewDto.getRatingValue() != null && finalReviewDto.getRatingValue() >= 1 && finalReviewDto.getRatingValue() <= 5) {
                                        reviewService.createRatingAfterReview(finalBook.getId(), finalReviewDto.getRatingValue());
                                    }
                                } catch (com.booklovers.exception.ConflictException e) {
                                    log.debug("Review already exists for book: {}", finalBook.getId());
                                    status.setRollbackOnly();
                                } catch (Exception e) {
                                    log.warn("Error creating review: {}", e.getMessage());
                                    status.setRollbackOnly();
                                }
                                return null;
                            });
                        } catch (Exception e) {
                            log.warn("Transaction failed for review: {}", e.getMessage());
                        }
                    } else {
                        log.warn("Book not found for review: {}", reviewDto.getBookTitle());
                    }
                } catch (Exception e) {
                    log.warn("Error importing review for book {}: {}", reviewDto.getBookTitle(), e.getMessage());
                }
            }
        }
        
        if (data.getRatings() != null) {
            for (RatingDto ratingDto : data.getRatings()) {
                try {
                    Book book = null;
                    if (ratingDto.getBookId() != null) {
                        book = bookRepository.findById(ratingDto.getBookId()).orElse(null);
                    }
                    if (book == null && ratingDto.getBookTitle() != null) {
                        List<Book> books = bookRepository.searchBooks(ratingDto.getBookTitle());
                        book = books.stream()
                                .filter(b -> b.getTitle().equalsIgnoreCase(ratingDto.getBookTitle()))
                                .findFirst()
                                .orElse(null);
                    }
                    
                    if (book != null && ratingDto.getValue() != null && ratingDto.getValue() >= 1 && ratingDto.getValue() <= 5) {
                        final Book finalBook = book;
                        final RatingDto finalRatingDto = ratingDto;
                        try {
                            getTransactionTemplate().execute(status -> {
                                try {
                                    ratingService.createOrUpdateRating(finalBook.getId(), finalRatingDto);
                                } catch (Exception e) {
                                    log.warn("Error creating rating: {}", e.getMessage());
                                    status.setRollbackOnly();
                                }
                                return null;
                            });
                        } catch (Exception e) {
                            log.warn("Transaction failed for rating: {}", e.getMessage());
                        }
                    } else {
                        log.warn("Book not found for rating: {}", ratingDto.getBookTitle());
                    }
                } catch (Exception e) {
                    log.warn("Error importing rating for book {}: {}", ratingDto.getBookTitle(), e.getMessage());
                }
            }
        }
    }
    
    private UserDataExportDto parseCsvData(String csvData) {
        UserDataExportDto.UserDataExportDtoBuilder builder = UserDataExportDto.builder();
        List<String> lines = Arrays.asList(csvData.split("\n"));
        
        UserDto.UserDtoBuilder userBuilder = UserDto.builder();
        java.util.List<UserBookDto> userBooks = new java.util.ArrayList<>();
        java.util.List<ReviewDto> reviews = new java.util.ArrayList<>();
        java.util.List<RatingDto> ratings = new java.util.ArrayList<>();
        
        int lineIndex = 0;
        while (lineIndex < lines.size()) {
            String line = lines.get(lineIndex).trim();
            
            if (line.startsWith("User Data Export")) {
                lineIndex++;
                while (lineIndex < lines.size() && !lines.get(lineIndex).trim().isEmpty()) {
                    String[] parts = lines.get(lineIndex).split(",", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if ("Username".equals(key)) {
                            userBuilder.username(value);
                        } else if ("Email".equals(key)) {
                            userBuilder.email(value);
                        } else if ("First Name".equals(key)) {
                            userBuilder.firstName(value);
                        } else if ("Last Name".equals(key)) {
                            userBuilder.lastName(value);
                        } else if ("Bio".equals(key)) {
                            userBuilder.bio(value.replace(";", ","));
                        }
                    }
                    lineIndex++;
                }
            } else if (line.equals("Books")) {
                lineIndex++;
                if (lineIndex < lines.size()) {
                    String header = lines.get(lineIndex);
                    lineIndex++;
                    while (lineIndex < lines.size() && !lines.get(lineIndex).trim().isEmpty()) {
                        String bookLine = lines.get(lineIndex);
                        String[] parts = parseCsvLine(bookLine);
                        if (parts.length >= 4) {
                            UserBookDto userBook = UserBookDto.builder()
                                    .bookTitle(parts[0].replace("\"", ""))
                                    .bookAuthor(parts[1].replace("\"", ""))
                                    .shelfName(parts[3].replace("\"", ""))
                                    .build();
                            userBooks.add(userBook);
                        }
                        lineIndex++;
                    }
                }
            } else if (line.equals("Reviews")) {
                lineIndex++;
                if (lineIndex < lines.size()) {
                    lineIndex++;
                    while (lineIndex < lines.size() && !lines.get(lineIndex).trim().isEmpty()) {
                        String reviewLine = lines.get(lineIndex);
                        String[] parts = parseCsvLine(reviewLine);
                        if (parts.length >= 2) {
                            ReviewDto review = ReviewDto.builder()
                                    .bookTitle(parts[0].replace("\"", ""))
                                    .content(parts[1].replace("\"", "").replace("\"\"", "\""))
                                    .ratingValue(parts.length >= 3 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : null)
                                    .build();
                            reviews.add(review);
                        }
                        lineIndex++;
                    }
                }
            } else if (line.equals("Ratings")) {
                lineIndex++;
                if (lineIndex < lines.size()) {
                    lineIndex++;
                    while (lineIndex < lines.size() && !lines.get(lineIndex).trim().isEmpty()) {
                        String ratingLine = lines.get(lineIndex);
                        String[] parts = parseCsvLine(ratingLine);
                        if (parts.length >= 2) {
                            RatingDto rating = RatingDto.builder()
                                    .bookTitle(parts[0].replace("\"", ""))
                                    .value(parts.length >= 2 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : null)
                                    .build();
                            ratings.add(rating);
                        }
                        lineIndex++;
                    }
                }
            }
            lineIndex++;
        }
        
        return builder
                .user(userBuilder.build())
                .userBooks(userBooks)
                .reviews(reviews)
                .ratings(ratings)
                .build();
    }
    
    private String[] parseCsvLine(String line) {
        List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
}
