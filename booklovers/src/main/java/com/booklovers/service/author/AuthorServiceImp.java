package com.booklovers.service.author;

import com.booklovers.dto.AuthorDto;
import com.booklovers.entity.Author;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImp implements AuthorService {
    
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        log.debug("Pobieranie wszystkich autorów");
        List<AuthorDto> authors = authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Znaleziono {} autorów", authors.size());
        return authors;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorById(Long id) {
        log.debug("Pobieranie autora: authorId={}", id);
        Optional<AuthorDto> author = authorRepository.findById(id)
                .map(authorMapper::toDto);
        if (author.isEmpty()) {
            log.warn("Nie znaleziono autora: authorId={}", id);
        }
        return author;
    }
    
    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        log.info("Tworzenie nowego autora: firstName={}, lastName={}", 
                authorDto.getFirstName(), authorDto.getLastName());
        Author author = authorMapper.toEntity(authorDto);
        Author saved = authorRepository.save(author);
        log.info("Autor utworzony pomyślnie: authorId={}, fullName={}", 
                saved.getId(), saved.getFullName());
        return authorMapper.toDto(saved);
    }
    
    @Override
    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto authorDto) {
        log.info("Aktualizacja autora: authorId={}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono autora do aktualizacji: authorId={}", id);
                    return new ResourceNotFoundException("Author", id);
                });
        
        author.setFirstName(authorDto.getFirstName());
        author.setLastName(authorDto.getLastName());
        author.setBiography(authorDto.getBiography());
        author.setDateOfBirth(authorDto.getDateOfBirth());
        author.setDateOfDeath(authorDto.getDateOfDeath());
        author.setNationality(authorDto.getNationality());
        
        Author updated = authorRepository.save(author);
        log.info("Autor zaktualizowany pomyślnie: authorId={}, fullName={}", 
                updated.getId(), updated.getFullName());
        return authorMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        log.info("Usuwanie autora: authorId={}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Próba usunięcia nieistniejącego autora: authorId={}", id);
                    return new ResourceNotFoundException("Author", id);
                });
        authorRepository.delete(author);
        log.info("Autor usunięty pomyślnie: authorId={}, fullName={}", id, author.getFullName());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> searchAuthors(String query) {
        log.debug("Wyszukiwanie autorów: query={}", query);
        List<AuthorDto> authors = authorRepository.searchAuthors(query).stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Znaleziono {} autorów dla zapytania: query={}", authors.size(), query);
        return authors;
    }
}
