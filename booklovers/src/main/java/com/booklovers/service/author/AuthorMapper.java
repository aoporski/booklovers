package com.booklovers.service.author;

import com.booklovers.dto.AuthorDto;
import com.booklovers.entity.Author;
import com.booklovers.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorMapper {
    
    private final BookRepository bookRepository;
    
    public AuthorDto toDto(Author author) {
        if (author == null) {
            return null;
        }
        
        // Użyj zapytania zamiast lazy loading, aby uniknąć problemów z relacjami
        Long booksCount = bookRepository.countByAuthorId(author.getId());
        
        return AuthorDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .biography(author.getBiography())
                .dateOfBirth(author.getDateOfBirth())
                .dateOfDeath(author.getDateOfDeath())
                .nationality(author.getNationality())
                .createdAt(author.getCreatedAt())
                .booksCount(booksCount != null ? booksCount.intValue() : 0)
                .build();
    }
    
    public Author toEntity(AuthorDto authorDto) {
        if (authorDto == null) {
            return null;
        }
        
        return Author.builder()
                .id(authorDto.getId())
                .firstName(authorDto.getFirstName())
                .lastName(authorDto.getLastName())
                .biography(authorDto.getBiography())
                .dateOfBirth(authorDto.getDateOfBirth())
                .dateOfDeath(authorDto.getDateOfDeath())
                .nationality(authorDto.getNationality())
                .build();
    }
}
