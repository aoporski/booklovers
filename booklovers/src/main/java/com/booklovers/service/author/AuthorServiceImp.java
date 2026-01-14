package com.booklovers.service.author;

import com.booklovers.dto.AuthorDto;
import com.booklovers.entity.Author;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImp implements AuthorService {
    
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorById(Long id) {
        return authorRepository.findById(id)
                .map(authorMapper::toDto);
    }
    
    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        Author author = authorMapper.toEntity(authorDto);
        Author saved = authorRepository.save(author);
        return authorMapper.toDto(saved);
    }
    
    @Override
    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto authorDto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        
        author.setFirstName(authorDto.getFirstName());
        author.setLastName(authorDto.getLastName());
        author.setBiography(authorDto.getBiography());
        author.setDateOfBirth(authorDto.getDateOfBirth());
        author.setDateOfDeath(authorDto.getDateOfDeath());
        author.setNationality(authorDto.getNationality());
        
        Author updated = authorRepository.save(author);
        return authorMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        authorRepository.delete(author);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> searchAuthors(String query) {
        return authorRepository.searchAuthors(query).stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }
}
