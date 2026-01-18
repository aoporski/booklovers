package com.booklovers.service.author;

import com.booklovers.dto.AuthorDto;
import com.booklovers.entity.Author;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorServiceImp authorService;

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
                .nationality("Polish")
                .build();

        authorDto = AuthorDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .nationality("Polish")
                .build();
    }

    @Test
    void testGetAllAuthors_Success() {
        List<Author> authors = Arrays.asList(author);
        when(authorRepository.findAll()).thenReturn(authors);
        when(authorMapper.toDto(author)).thenReturn(authorDto);

        List<AuthorDto> result = authorService.getAllAuthors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        verify(authorRepository).findAll();
        verify(authorMapper).toDto(author);
    }

    @Test
    void testGetAuthorById_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorMapper.toDto(author)).thenReturn(authorDto);

        Optional<AuthorDto> result = authorService.getAuthorById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(authorRepository).findById(1L);
        verify(authorMapper).toDto(author);
    }

    @Test
    void testGetAuthorById_NotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<AuthorDto> result = authorService.getAuthorById(1L);

        assertThat(result).isEmpty();
        verify(authorRepository).findById(1L);
        verify(authorMapper, never()).toDto(any());
    }

    @Test
    void testCreateAuthor_Success() {
        AuthorDto inputDto = AuthorDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Author inputAuthor = Author.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Author savedAuthor = Author.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        AuthorDto savedDto = AuthorDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(authorMapper.toEntity(inputDto)).thenReturn(inputAuthor);
        when(authorRepository.save(inputAuthor)).thenReturn(savedAuthor);
        when(authorMapper.toDto(savedAuthor)).thenReturn(savedDto);

        AuthorDto result = authorService.createAuthor(inputDto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(authorMapper).toEntity(inputDto);
        verify(authorRepository).save(inputAuthor);
        verify(authorMapper).toDto(savedAuthor);
    }

    @Test
    void testUpdateAuthor_Success() {
        AuthorDto updateDto = AuthorDto.builder()
                .firstName("Updated")
                .lastName("Name")
                .biography("Updated biography")
                .build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(authorMapper.toDto(author)).thenReturn(authorDto);

        AuthorDto result = authorService.updateAuthor(1L, updateDto);

        assertThat(result).isNotNull();
        verify(authorRepository).findById(1L);
        verify(authorRepository).save(author);
        verify(authorMapper).toDto(author);
    }

    @Test
    void testUpdateAuthor_NotFound() {
        AuthorDto updateDto = AuthorDto.builder().build();
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.updateAuthor(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");

        verify(authorRepository).findById(1L);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void testDeleteAuthor_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        doNothing().when(authorRepository).delete(author);

        authorService.deleteAuthor(1L);

        verify(authorRepository).findById(1L);
        verify(authorRepository).delete(author);
    }

    @Test
    void testDeleteAuthor_NotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.deleteAuthor(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");

        verify(authorRepository).findById(1L);
        verify(authorRepository, never()).delete(any());
    }

    @Test
    void testSearchAuthors_Success() {
        List<Author> authors = Arrays.asList(author);
        when(authorRepository.searchAuthors("John")).thenReturn(authors);
        when(authorMapper.toDto(author)).thenReturn(authorDto);

        List<AuthorDto> result = authorService.searchAuthors("John");

        assertThat(result).hasSize(1);
        verify(authorRepository).searchAuthors("John");
        verify(authorMapper).toDto(author);
    }

    @Test
    void testSearchAuthors_EmptyResult() {
        when(authorRepository.searchAuthors("NonExistent")).thenReturn(List.of());

        List<AuthorDto> result = authorService.searchAuthors("NonExistent");

        assertThat(result).isEmpty();
        verify(authorRepository).searchAuthors("NonExistent");
        verify(authorMapper, never()).toDto(any());
    }
}
