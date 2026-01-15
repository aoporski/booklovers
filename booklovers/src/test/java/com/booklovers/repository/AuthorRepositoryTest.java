package com.booklovers.repository;

import com.booklovers.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = Author.builder()
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .nationality("Polish")
                .build();
    }

    @Test
    void testSaveAuthor() {
        Author savedAuthor = authorRepository.save(testAuthor);
        
        assertThat(savedAuthor.getId()).isNotNull();
        assertThat(savedAuthor.getFirstName()).isEqualTo("John");
        assertThat(savedAuthor.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testFindAuthorById() {
        Author savedAuthor = entityManager.persistAndFlush(testAuthor);
        
        Optional<Author> foundAuthor = authorRepository.findById(savedAuthor.getId());
        
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByFirstNameAndLastName() {
        entityManager.persistAndFlush(testAuthor);
        
        Optional<Author> foundAuthor = authorRepository.findByFirstNameAndLastName("John", "Doe");
        
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getNationality()).isEqualTo("Polish");
    }

    @Test
    void testSearchAuthors() {
        Author author1 = Author.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        Author author2 = Author.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();
        
        entityManager.persistAndFlush(author1);
        entityManager.persistAndFlush(author2);
        
        List<Author> results = authorRepository.searchAuthors("John");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testFindByFirstNameContainingIgnoreCase() {
        entityManager.persistAndFlush(testAuthor);
        
        List<Author> results = authorRepository.findByFirstNameContainingIgnoreCase("john");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testFindByLastNameContainingIgnoreCase() {
        entityManager.persistAndFlush(testAuthor);
        
        List<Author> results = authorRepository.findByLastNameContainingIgnoreCase("doe");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void testUpdateAuthor() {
        Author savedAuthor = entityManager.persistAndFlush(testAuthor);
        savedAuthor.setBiography("Updated biography");
        
        Author updatedAuthor = authorRepository.save(savedAuthor);
        
        assertThat(updatedAuthor.getBiography()).isEqualTo("Updated biography");
    }

    @Test
    void testDeleteAuthor() {
        Author savedAuthor = entityManager.persistAndFlush(testAuthor);
        Long authorId = savedAuthor.getId();
        
        authorRepository.delete(savedAuthor);
        entityManager.flush();
        
        Optional<Author> deletedAuthor = authorRepository.findById(authorId);
        assertThat(deletedAuthor).isEmpty();
    }

    @Test
    void testFindAllAuthors() {
        Author author1 = Author.builder()
                .firstName("Author1")
                .lastName("Last1")
                .build();
        Author author2 = Author.builder()
                .firstName("Author2")
                .lastName("Last2")
                .build();
        
        entityManager.persistAndFlush(author1);
        entityManager.persistAndFlush(author2);
        
        long count = authorRepository.count();
        
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testGetFullName() {
        Author savedAuthor = entityManager.persistAndFlush(testAuthor);
        
        assertThat(savedAuthor.getFullName()).isEqualTo("John Doe");
    }
}
