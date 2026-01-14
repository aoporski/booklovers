package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserBookDto;
import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.RatingRepository;
import com.booklovers.repository.ReviewRepository;
import com.booklovers.repository.UserBookRepository;
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
public class BookServiceImp implements BookService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    
    @Override
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> {
                    BookDto dto = bookMapper.toDto(book);
                    Double avgRating = ratingRepository.getAverageRatingByBookId(book.getId());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<BookDto> getBookById(Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    BookDto dto = bookMapper.toDto(book);
                    Double avgRating = ratingRepository.getAverageRatingByBookId(id);
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return dto;
                });
    }
    
    @Override
    @Transactional
    public BookDto createBook(BookDto bookDto) {
        Book book = bookMapper.toEntity(bookDto);
        
        // Jeśli podano authorId, przypisz autora do książki
        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", bookDto.getAuthorId()));
            book.setAuthorEntity(author);
            book.setAuthor(author.getFullName());
        }
        
        Book savedBook = bookRepository.save(book);
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
        
        // Jeśli podano authorId, przypisz autora do książki
        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", bookDto.getAuthorId()));
            book.setAuthorEntity(author);
            // Ustaw również pole author jako pełne imię autora dla kompatybilności wstecznej
            book.setAuthor(author.getFullName());
        } else if (bookDto.getAuthor() != null) {
            // Fallback: jeśli nie podano authorId, użyj pola author
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
    public List<BookDto> searchBooks(String query) {
        return bookRepository.searchBooks(query).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookDto> getUserBooks(Long userId) {
        List<UserBook> userBooks = userBookRepository.findByUserId(userId);
        return userBooks.stream()
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
        return userBookRepository.findDistinctShelfNamesByUserId(userId);
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
        
        // Sprawdź czy książka już jest w tej kategorii
        Optional<UserBook> existing = userBookRepository.findByUserIdAndBookIdAndShelfName(
                user.getId(), bookId, shelfName);
        
        if (existing.isPresent()) {
            throw new ConflictException("Book already exists in this shelf");
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
        
        userBook.setShelfName(finalToShelf);
        userBookRepository.save(userBook);
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
