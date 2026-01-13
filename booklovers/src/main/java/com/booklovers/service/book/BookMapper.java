package com.booklovers.service.book;

import com.booklovers.dto.BookDto;
import com.booklovers.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    
    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        
        // Jeśli książka ma przypisanego autora (authorEntity), użyj jego pełnego imienia
        String authorName = book.getAuthor();
        if (book.getAuthorEntity() != null) {
            authorName = book.getAuthorEntity().getFullName();
        }
        
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(authorName) // Pełne imię autora lub fallback do pola author
                .authorId(book.getAuthorEntity() != null ? book.getAuthorEntity().getId() : null)
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .publisher(book.getPublisher())
                .publicationDate(book.getPublicationDate())
                .pageCount(book.getPageCount())
                .language(book.getLanguage())
                .coverImageUrl(book.getCoverImageUrl())
                .createdAt(book.getCreatedAt())
                .ratingsCount(book.getRatings() != null ? book.getRatings().size() : 0)
                .reviewsCount(book.getReviews() != null ? book.getReviews().size() : 0)
                .build();
    }
    
    public Book toEntity(BookDto dto) {
        if (dto == null) {
            return null;
        }
        
        // authorEntity będzie ustawione w BookServiceImp na podstawie authorId
        return Book.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .author(dto.getAuthor()) // Zachowane dla kompatybilności wstecznej
                .isbn(dto.getIsbn())
                .description(dto.getDescription())
                .publisher(dto.getPublisher())
                .publicationDate(dto.getPublicationDate())
                .pageCount(dto.getPageCount())
                .language(dto.getLanguage())
                .coverImageUrl(dto.getCoverImageUrl())
                .build();
    }
}
