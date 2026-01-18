package com.booklovers.service.author;

import com.booklovers.dto.AuthorDto;
import com.booklovers.entity.Author;
import com.booklovers.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorMapperTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorMapper authorMapper;

    private Author author;
    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .dateOfDeath(LocalDate.of(2020, 12, 31))
                .nationality("American")
                .createdAt(LocalDateTime.now())
                .build();

        authorDto = AuthorDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .dateOfDeath(LocalDate.of(2020, 12, 31))
                .nationality("American")
                .build();
    }

    @Test
    void testToDto_Success() {
        when(bookRepository.countByAuthorId(1L)).thenReturn(5L);

        AuthorDto result = authorMapper.toDto(author);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBiography()).isEqualTo("Test biography");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 1, 1));
        assertThat(result.getDateOfDeath()).isEqualTo(LocalDate.of(2020, 12, 31));
        assertThat(result.getNationality()).isEqualTo("American");
        assertThat(result.getBooksCount()).isEqualTo(5);
    }

    @Test
    void testToDto_NullAuthor() {
        AuthorDto result = authorMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_BooksCountZero() {
        when(bookRepository.countByAuthorId(1L)).thenReturn(0L);

        AuthorDto result = authorMapper.toDto(author);

        assertThat(result.getBooksCount()).isEqualTo(0);
    }

    @Test
    void testToDto_BooksCountNull() {
        when(bookRepository.countByAuthorId(1L)).thenReturn(null);

        AuthorDto result = authorMapper.toDto(author);

        assertThat(result.getBooksCount()).isEqualTo(0);
    }

    @Test
    void testToDto_WithoutOptionalFields() {
        Author authorWithoutOptional = Author.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookRepository.countByAuthorId(2L)).thenReturn(3L);

        AuthorDto result = authorMapper.toDto(authorWithoutOptional);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getBiography()).isNull();
        assertThat(result.getDateOfBirth()).isNull();
        assertThat(result.getDateOfDeath()).isNull();
        assertThat(result.getNationality()).isNull();
        assertThat(result.getBooksCount()).isEqualTo(3);
    }

    @Test
    void testToEntity_Success() {
        Author result = authorMapper.toEntity(authorDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBiography()).isEqualTo("Test biography");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 1, 1));
        assertThat(result.getDateOfDeath()).isEqualTo(LocalDate.of(2020, 12, 31));
        assertThat(result.getNationality()).isEqualTo("American");
    }

    @Test
    void testToEntity_NullDto() {
        Author result = authorMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithoutOptionalFields() {
        AuthorDto dtoWithoutOptional = AuthorDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Author result = authorMapper.toEntity(dtoWithoutOptional);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getBiography()).isNull();
        assertThat(result.getDateOfBirth()).isNull();
        assertThat(result.getDateOfDeath()).isNull();
        assertThat(result.getNationality()).isNull();
    }
}
