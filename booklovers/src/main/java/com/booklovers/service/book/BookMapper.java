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
        
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
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
        
        return Book.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .author(dto.getAuthor())
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
