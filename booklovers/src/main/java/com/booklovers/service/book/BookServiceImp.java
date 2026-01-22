package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserBookDto;
import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import com.booklovers.exception.BadRequestException;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.RatingRepository;
import com.booklovers.repository.ReviewRepository;
import com.booklovers.repository.UserBookRepository;
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
public class BookServiceImp implements BookService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDto> getAllBooks() {
        log.debug("Pobieranie wszystkich książek");
        List<BookDto> books = bookRepository.findAll().stream()
                .map(book -> {
                    BookDto dto = bookMapper.toDto(book);
                    Double avgRating = ratingRepository.getAverageRatingByBookId(book.getId());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                })
                .collect(Collectors.toList());
        log.info("Pobrano {} książek", books.size());
        return books;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> getBookById(Long id) {
        log.debug("Pobieranie książki: bookId={}", id);
        Optional<BookDto> book = bookRepository.findById(id)
                .map(b -> {
                    BookDto dto = bookMapper.toDto(b);
                    Double avgRating = ratingRepository.getAverageRatingByBookId(id);
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                });
        if (book.isEmpty()) {
            log.warn("Nie znaleziono książki: bookId={}", id);
        } else {
            log.debug("Znaleziono książkę: bookId={}, title={}", id, book.get().getTitle());
        }
        return book;
    }
    
    @Override
    @Transactional
    public BookDto createBook(BookDto bookDto) {
        log.info("Tworzenie nowej książki: title={}, authorId={}", bookDto.getTitle(), bookDto.getAuthorId());
        Book book = bookMapper.toEntity(bookDto);
        
        if (bookDto.getAuthorId() != null) {
            log.debug("Przypisywanie autora do książki: authorId={}", bookDto.getAuthorId());
            Author author = authorRepository.findById(bookDto.getAuthorId())
                    .orElseThrow(() -> {
                        log.error("Nie znaleziono autora: authorId={}", bookDto.getAuthorId());
                        return new ResourceNotFoundException("Author", bookDto.getAuthorId());
                    });
            book.setAuthorEntity(author);
            book.setAuthor(author.getFullName());
        }
        
        Book savedBook = bookRepository.save(book);
        log.info("Książka utworzona pomyślnie: bookId={}, title={}", savedBook.getId(), savedBook.getTitle());
        return bookMapper.toDto(savedBook);
    }
    
    @Override
    @Transactional
    public BookDto updateBook(Long id, BookDto bookDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        if (bookDto.getTitle() != null) {
            book.setTitle(bookDto.getTitle());
        }
        
        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", bookDto.getAuthorId()));
            book.setAuthorEntity(author);
            book.setAuthor(author.getFullName());
        } else if (bookDto.getAuthor() != null) {
            book.setAuthor(bookDto.getAuthor());
        }
        
        if (bookDto.getIsbn() != null) {
            book.setIsbn(bookDto.getIsbn());
        }
        if (bookDto.getDescription() != null) {
            book.setDescription(bookDto.getDescription());
        }
        if (bookDto.getPublisher() != null) {
            book.setPublisher(bookDto.getPublisher());
        }
        if (bookDto.getPublicationDate() != null) {
            book.setPublicationDate(bookDto.getPublicationDate());
        }
        if (bookDto.getPageCount() != null) {
            book.setPageCount(bookDto.getPageCount());
        }
        if (bookDto.getLanguage() != null) {
            book.setLanguage(bookDto.getLanguage());
        }
        if (bookDto.getCoverImageUrl() != null) {
            book.setCoverImageUrl(bookDto.getCoverImageUrl());
        }
        
        Book updatedBook = bookRepository.save(book);
        BookDto dto = bookMapper.toDto(updatedBook);
        Double avgRating = ratingRepository.getAverageRatingByBookId(id);
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        return dto;
    }
    
    @Override
    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDto> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        String searchQuery = "%" + query.trim() + "%";
        return bookRepository.searchBooks(searchQuery).stream()
                .map(book -> {
                    BookDto dto = bookMapper.toDto(book);
                    Double avgRating = ratingRepository.getAverageRatingByBookId(book.getId());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookDto> getUserBooks(Long userId) {
        List<UserBook> userBooks = userBookRepository.findByUserId(userId);
        return userBooks.stream()
                .filter(ub -> ub.getBook() != null)
                .map(ub -> {
                    BookDto dto = bookMapper.toDto(ub.getBook());
                    Double avgRating = ratingRepository.getAverageRatingByBookId(ub.getBook().getId());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                })
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookDto> getUserBooksByShelf(Long userId, String shelfName) {
        List<UserBook> userBooks = userBookRepository.findByUserIdAndShelfName(userId, shelfName);
        return userBooks.stream()
                .filter(ub -> ub.getBook() != null)
                .map(ub -> {
                    BookDto dto = bookMapper.toDto(ub.getBook());
                    Double avgRating = ratingRepository.getAverageRatingByBookId(ub.getBook().getId());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getUserShelves(Long userId) {
        List<String> shelves = userBookRepository.findDistinctShelfNamesByUserId(userId);
        if (shelves.isEmpty()) {
            return getDefaultShelves();
        }
        List<String> defaultShelves = getDefaultShelves();
        for (String defaultShelf : defaultShelves) {
            if (!shelves.contains(defaultShelf)) {
                shelves.add(defaultShelf);
            }
        }
        return shelves;
    }
    
    @Override
    public List<String> getDefaultShelves() {
        return List.of("Przeczytane", "Chcę przeczytać", "Teraz czytam");
    }
    
    @Override
    @Transactional
    public void createShelf(Long userId, String shelfName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        List<String> defaultShelves = getDefaultShelves();
        if (defaultShelves.contains(shelfName)) {
            throw new BadRequestException("Cannot create default shelf: " + shelfName);
        }
        
        List<String> existingShelves = userBookRepository.findDistinctShelfNamesByUserId(userId);
        if (existingShelves.contains(shelfName)) {
            throw new ConflictException("Shelf already exists: " + shelfName);
        }
        
        UserBook emptyShelf = UserBook.builder()
                .user(user)
                .book(null)
                .shelfName(shelfName)
                .build();
        userBookRepository.save(emptyShelf);
    }
    
    @Override
    @Transactional
    public UserBookDto addBookToUserLibrary(Long bookId, String shelfName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        
        if (shelfName == null || shelfName.trim().isEmpty()) {
            shelfName = "Moja biblioteczka";
        }
        
        Optional<UserBook> existing = userBookRepository.findByUserIdAndBookIdAndShelfName(
                user.getId(), bookId, shelfName);
        
        if (existing.isPresent()) {
            throw new ConflictException("Book already exists in this shelf");
        }
        
        List<String> defaultShelves = getDefaultShelves();
        boolean isDefaultShelf = defaultShelves.contains(shelfName);
        
        if (isDefaultShelf) {
            List<UserBook> existingInDefaultShelves = userBookRepository.findByUserIdAndBookId(user.getId(), bookId)
                    .stream()
                    .filter(ub -> ub.getBook() != null && defaultShelves.contains(ub.getShelfName()))
                    .collect(Collectors.toList());
            
            if (!existingInDefaultShelves.isEmpty()) {
                UserBook existingUserBook = existingInDefaultShelves.get(0);
                String fromShelf = existingUserBook.getShelfName();
                
                if (fromShelf.equals(shelfName)) {
                    throw new ConflictException("Book already exists in this shelf");
                }
                
                moveBookToShelf(bookId, fromShelf, shelfName);
                UserBook moved = userBookRepository.findByUserIdAndBookIdAndShelfName(
                        user.getId(), bookId, shelfName)
                        .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
                return toUserBookDto(moved);
            }
        }
        
        List<UserBook> emptyShelves = userBookRepository.findByUserIdAndShelfName(user.getId(), shelfName)
                .stream()
                .filter(ub -> ub.getBook() == null)
                .collect(Collectors.toList());
        if (!emptyShelves.isEmpty()) {
            userBookRepository.deleteAll(emptyShelves);
        }
        
        UserBook userBook = UserBook.builder()
                .user(user)
                .book(book)
                .shelfName(shelfName)
                .build();
        
        UserBook saved = userBookRepository.save(userBook);
        return toUserBookDto(saved);
    }
    
    @Override
    @Transactional
    public void removeBookFromUserLibrary(Long bookId, String shelfName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        final String finalShelfName = (shelfName == null || shelfName.trim().isEmpty()) 
                ? "Moja biblioteczka" 
                : shelfName;
        
        UserBook userBook = userBookRepository.findByUserIdAndBookIdAndShelfName(
                user.getId(), bookId, finalShelfName)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "not found in shelf: " + finalShelfName));
        
        userBookRepository.delete(userBook);
    }
    
    @Override
    @Transactional
    public void moveBookToShelf(Long bookId, String fromShelf, String toShelf) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        final String finalFromShelf = (fromShelf == null || fromShelf.trim().isEmpty()) 
                ? "Moja biblioteczka" 
                : fromShelf;
        final String finalToShelf = (toShelf == null || toShelf.trim().isEmpty()) 
                ? "Moja biblioteczka" 
                : toShelf;
        
        UserBook userBook = userBookRepository.findByUserIdAndBookIdAndShelfName(
                user.getId(), bookId, finalFromShelf)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "not found in source shelf: " + finalFromShelf));
        
        Optional<UserBook> existing = userBookRepository.findByUserIdAndBookIdAndShelfName(
                user.getId(), bookId, finalToShelf);
        
        if (existing.isPresent()) {
            throw new ConflictException("Book already exists in target shelf");
        }
        
        List<UserBook> emptyShelves = userBookRepository.findByUserIdAndShelfName(user.getId(), finalToShelf)
                .stream()
                .filter(ub -> ub.getBook() == null)
                .collect(Collectors.toList());
        if (!emptyShelves.isEmpty()) {
            userBookRepository.deleteAll(emptyShelves);
            userBookRepository.flush();
        }
        
        userBook.setShelfName(finalToShelf);
        userBookRepository.save(userBook);
    }
    
    @Override
    @Transactional
    public void deleteShelf(Long userId, String shelfName) {
        List<String> defaultShelves = getDefaultShelves();
        if (defaultShelves.contains(shelfName)) {
            throw new BadRequestException("Cannot delete default shelf: " + shelfName);
        }
        
        List<UserBook> userBooks = userBookRepository.findByUserIdAndShelfName(userId, shelfName);
        if (userBooks != null && !userBooks.isEmpty()) {
            userBookRepository.deleteAll(userBooks);
            userBookRepository.flush();
        }
    }
    
    private UserBookDto toUserBookDto(UserBook userBook) {
        return UserBookDto.builder()
                .id(userBook.getId())
                .userId(userBook.getUser() != null ? userBook.getUser().getId() : null)
                .username(userBook.getUser() != null ? userBook.getUser().getUsername() : null)
                .bookId(userBook.getBook() != null ? userBook.getBook().getId() : null)
                .bookTitle(userBook.getBook() != null ? userBook.getBook().getTitle() : null)
                .bookAuthor(userBook.getBook() != null ? userBook.getBook().getAuthor() : null)
                .shelfName(userBook.getShelfName())
                .addedAt(userBook.getAddedAt())
                .build();
    }
}
