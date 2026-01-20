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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template;
    }
    
    private ObjectMapper getConfiguredObjectMapper() {
        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    @Override
    public void importUserDataFromJson(Long userId, String jsonData) {
        log.info("Import danych użytkownika z JSON: userId={}", userId);
        try {
            if (jsonData == null || jsonData.trim().isEmpty()) {
                log.warn("Próba importu pustych danych JSON: userId={}", userId);
                throw new BadRequestException("JSON data is empty");
            }
            
            ObjectMapper configuredMapper = getConfiguredObjectMapper();
            UserDataExportDto data = configuredMapper.readValue(jsonData, UserDataExportDto.class);
            log.debug("Dane JSON sparsowane pomyślnie: userId={}, books={}, reviews={}, ratings={}", 
                    userId, 
                    data.getUserBooks() != null ? data.getUserBooks().size() : 0,
                    data.getReviews() != null ? data.getReviews().size() : 0,
                    data.getRatings() != null ? data.getRatings().size() : 0);
            
            importUserData(userId, data);
            log.info("Import danych z JSON zakończony pomyślnie: userId={}", userId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas importu danych z JSON: userId={}, error={}", userId, e.getMessage(), e);
            throw new BadRequestException("Invalid JSON format: " + e.getMessage());
        }
    }
    
    @Override
    public void importUserDataFromCsv(Long userId, String csvData) {
        log.info("Import danych użytkownika z CSV: userId={}", userId);
        try {
            if (csvData == null || csvData.trim().isEmpty()) {
                log.warn("Próba importu pustych danych CSV: userId={}", userId);
                throw new BadRequestException("CSV data is empty");
            }
            
            UserDataExportDto data = parseCsvData(csvData);
            log.debug("Dane CSV sparsowane pomyślnie: userId={}, books={}, reviews={}, ratings={}", 
                    userId,
                    data.getUserBooks() != null ? data.getUserBooks().size() : 0,
                    data.getReviews() != null ? data.getReviews().size() : 0,
                    data.getRatings() != null ? data.getRatings().size() : 0);
            
            importUserData(userId, data);
            log.info("Import danych z CSV zakończony pomyślnie: userId={}", userId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas importu danych z CSV: userId={}, error={}", userId, e.getMessage(), e);
            throw new BadRequestException("Invalid CSV format: " + e.getMessage());
        }
    }
    
    private void importUserData(Long userId, UserDataExportDto data) {
        log.info("Rozpoczęcie importu danych: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika podczas importu: userId={}", userId);
                    return new ResourceNotFoundException("User", userId);
                });
        
        int importedBooks = 0;
        int skippedBooks = 0;
        int importedReviews = 0;
        int skippedReviews = 0;
        int importedRatings = 0;
        int skippedRatings = 0;
        
        if (data.getUserBooks() != null && !data.getUserBooks().isEmpty()) {
            log.debug("Importowanie {} książek: userId={}", data.getUserBooks().size(), userId);
            for (UserBookDto userBookDto : data.getUserBooks()) {
                try {
                    Book book = findBook(userBookDto.getBookId(), userBookDto.getBookTitle());
                    
                    if (book != null) {
                        final Book finalBook = book;
                        final String finalShelfName = userBookDto.getShelfName() != null && !userBookDto.getShelfName().isEmpty() 
                                ? userBookDto.getShelfName() 
                                : "Moja biblioteczka";
                        
                        getTransactionTemplate().execute(status -> {
                            try {
                                bookService.addBookToUserLibrary(finalBook.getId(), finalShelfName);
                                log.debug("Książka dodana do biblioteczki: bookId={}, shelf={}", finalBook.getId(), finalShelfName);
                                return true;
                            } catch (com.booklovers.exception.ConflictException e) {
                                log.debug("Książka już w biblioteczce: bookId={}, shelf={}", finalBook.getId(), finalShelfName);
                                return false;
                            } catch (Exception e) {
                                log.warn("Błąd podczas dodawania książki do biblioteczki: bookId={}, error={}", 
                                        finalBook.getId(), e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
                        importedBooks++;
                    } else {
                        log.warn("Nie znaleziono książki do importu: bookId={}, title={}", 
                                userBookDto.getBookId(), userBookDto.getBookTitle());
                        skippedBooks++;
                    }
                } catch (Exception e) {
                    log.warn("Błąd podczas importu książki: title={}, error={}", 
                            userBookDto.getBookTitle(), e.getMessage());
                    skippedBooks++;
                }
            }
            log.info("Import książek zakończony: imported={}, skipped={}", importedBooks, skippedBooks);
        }
        
        if (data.getReviews() != null && !data.getReviews().isEmpty()) {
            log.debug("Importowanie {} recenzji: userId={}", data.getReviews().size(), userId);
            for (ReviewDto reviewDto : data.getReviews()) {
                try {
                    Book book = findBook(reviewDto.getBookId(), reviewDto.getBookTitle());
                    
                    if (book != null) {
                        final Book finalBook = book;
                        final ReviewDto finalReviewDto = reviewDto;
                        
                        Boolean result = getTransactionTemplate().execute(status -> {
                            try {
                                reviewService.createReview(finalBook.getId(), finalReviewDto);
                                if (finalReviewDto.getRatingValue() != null && 
                                    finalReviewDto.getRatingValue() >= 1 && 
                                    finalReviewDto.getRatingValue() <= 5) {
                                    reviewService.createRatingAfterReview(finalBook.getId(), finalReviewDto.getRatingValue());
                                }
                                log.debug("Recenzja utworzona: bookId={}", finalBook.getId());
                                return true;
                            } catch (com.booklovers.exception.ConflictException e) {
                                log.debug("Recenzja już istnieje: bookId={}", finalBook.getId());
                                return false;
                            } catch (Exception e) {
                                log.warn("Błąd podczas tworzenia recenzji: bookId={}, error={}", 
                                        finalBook.getId(), e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
                        
                        if (Boolean.TRUE.equals(result)) {
                            importedReviews++;
                        } else {
                            skippedReviews++;
                        }
                    } else {
                        log.warn("Nie znaleziono książki dla recenzji: bookId={}, title={}", 
                                reviewDto.getBookId(), reviewDto.getBookTitle());
                        skippedReviews++;
                    }
                } catch (Exception e) {
                    log.warn("Błąd podczas importu recenzji: title={}, error={}", 
                            reviewDto.getBookTitle(), e.getMessage());
                    skippedReviews++;
                }
            }
            log.info("Import recenzji zakończony: imported={}, skipped={}", importedReviews, skippedReviews);
        }
        
        if (data.getRatings() != null && !data.getRatings().isEmpty()) {
            log.debug("Importowanie {} ocen: userId={}", data.getRatings().size(), userId);
            for (RatingDto ratingDto : data.getRatings()) {
                try {
                    if (ratingDto.getValue() == null || ratingDto.getValue() < 1 || ratingDto.getValue() > 5) {
                        log.warn("Nieprawidłowa wartość oceny: value={}", ratingDto.getValue());
                        skippedRatings++;
                        continue;
                    }
                    
                    Book book = findBook(ratingDto.getBookId(), ratingDto.getBookTitle());
                    
                    if (book != null) {
                        final Book finalBook = book;
                        final RatingDto finalRatingDto = ratingDto;
                        
                        getTransactionTemplate().execute(status -> {
                            try {
                                ratingService.createOrUpdateRating(finalBook.getId(), finalRatingDto);
                                log.debug("Ocena utworzona/zaktualizowana: bookId={}, value={}", 
                                        finalBook.getId(), finalRatingDto.getValue());
                                return null;
                            } catch (Exception e) {
                                log.warn("Błąd podczas tworzenia oceny: bookId={}, error={}", 
                                        finalBook.getId(), e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
                        importedRatings++;
                    } else {
                        log.warn("Nie znaleziono książki dla oceny: bookId={}, title={}", 
                                ratingDto.getBookId(), ratingDto.getBookTitle());
                        skippedRatings++;
                    }
                } catch (Exception e) {
                    log.warn("Błąd podczas importu oceny: title={}, error={}", 
                            ratingDto.getBookTitle(), e.getMessage());
                    skippedRatings++;
                }
            }
            log.info("Import ocen zakończony: imported={}, skipped={}", importedRatings, skippedRatings);
        }
        
        log.info("Import danych zakończony: userId={}, books(imported={}, skipped={}), reviews(imported={}, skipped={}), ratings(imported={}, skipped={})", 
                userId, importedBooks, skippedBooks, importedReviews, skippedReviews, importedRatings, skippedRatings);
    }
    
    private Book findBook(Long bookId, String bookTitle) {
        if (bookId != null) {
            return bookRepository.findById(bookId).orElse(null);
        }
        if (bookTitle != null && !bookTitle.trim().isEmpty()) {
            List<Book> books = bookRepository.searchBooks(bookTitle.trim());
            return books.stream()
                    .filter(b -> b.getTitle().equalsIgnoreCase(bookTitle.trim()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    private UserDataExportDto parseCsvData(String csvData) {
        log.debug("Rozpoczęcie parsowania danych CSV");
        List<String> lines = new ArrayList<>(Arrays.asList(csvData.split("\\r?\\n")));
        
        UserDto.UserDtoBuilder userBuilder = UserDto.builder();
        List<UserBookDto> userBooks = new ArrayList<>();
        List<ReviewDto> reviews = new ArrayList<>();
        List<RatingDto> ratings = new ArrayList<>();
        
        int lineIndex = 0;
        String currentSection = null;
        
        while (lineIndex < lines.size()) {
            String line = lines.get(lineIndex).trim();
            
            if (line.isEmpty()) {
                lineIndex++;
                continue;
            }
            
            if (line.startsWith("User Data Export") || line.startsWith("Username,") || line.startsWith("Email,")) {
                currentSection = "user";
                if (line.startsWith("User Data Export")) {
                    lineIndex++;
                    continue;
                }
            }
            
            if (line.equals("Books")) {
                currentSection = "books";
                lineIndex++;
                if (lineIndex < lines.size()) {
                    String header = lines.get(lineIndex);
                    log.debug("Nagłówek sekcji Books: {}", header);
                    lineIndex++;
                }
                continue;
            }
            
            if (line.equals("Reviews")) {
                currentSection = "reviews";
                lineIndex++;
                if (lineIndex < lines.size()) {
                    String header = lines.get(lineIndex);
                    log.debug("Nagłówek sekcji Reviews: {}", header);
                    lineIndex++;
                }
                continue;
            }
            
            if (line.equals("Ratings")) {
                currentSection = "ratings";
                lineIndex++;
                if (lineIndex < lines.size()) {
                    String header = lines.get(lineIndex);
                    log.debug("Nagłówek sekcji Ratings: {}", header);
                    lineIndex++;
                }
                continue;
            }
            
            try {
                if ("user".equals(currentSection)) {
                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        switch (key) {
                            case "Username":
                                userBuilder.username(value);
                                break;
                            case "Email":
                                userBuilder.email(value);
                                break;
                            case "First Name":
                                userBuilder.firstName(value);
                                break;
                            case "Last Name":
                                userBuilder.lastName(value);
                                break;
                            case "Bio":
                                userBuilder.bio(value.replace(";", ","));
                                break;
                        }
                    }
                } else if ("books".equals(currentSection)) {
                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 4) {
                        UserBookDto userBook = UserBookDto.builder()
                                .bookTitle(cleanCsvValue(parts[0]))
                                .bookAuthor(cleanCsvValue(parts[1]))
                                .shelfName(cleanCsvValue(parts[3]))
                                .build();
                        userBooks.add(userBook);
                    }
                } else if ("reviews".equals(currentSection)) {
                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 2) {
                        ReviewDto review = ReviewDto.builder()
                                .bookTitle(cleanCsvValue(parts[0]))
                                .content(cleanCsvValue(parts[1]))
                                .ratingValue(parseIntSafely(parts.length >= 3 ? parts[2] : null))
                                .build();
                        reviews.add(review);
                    }
                } else if ("ratings".equals(currentSection)) {
                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 2) {
                        RatingDto rating = RatingDto.builder()
                                .bookTitle(cleanCsvValue(parts[0]))
                                .value(parseIntSafely(parts[1]))
                                .build();
                        ratings.add(rating);
                    }
                }
            } catch (Exception e) {
                log.warn("Błąd podczas parsowania linii CSV: line={}, error={}", line, e.getMessage());
            }
            
            lineIndex++;
        }
        
        log.debug("CSV sparsowany: userBooks={}, reviews={}, ratings={}", 
                userBooks.size(), reviews.size(), ratings.size());
        
        return UserDataExportDto.builder()
                .user(userBuilder.build())
                .userBooks(userBooks)
                .reviews(reviews)
                .ratings(ratings)
                .build();
    }
    
    private String cleanCsvValue(String value) {
        if (value == null) {
            return null;
        }
        return value.trim()
                .replaceAll("^\"|\"$", "")
                .replace("\"\"", "\"");
    }
    
    private Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("Nie można sparsować liczby: {}", value);
            return null;
        }
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
