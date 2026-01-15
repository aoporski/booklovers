package com.booklovers.repository;

import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
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
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = Author.builder()
                .firstName("Test")
                .lastName("Author")
                .build();
        testAuthor = entityManager.persistAndFlush(testAuthor);
        
        testBook = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .description("Test description")
                .authorEntity(testAuthor)
                .build();
    }

    @Test
    void testSaveBook() {
        Book savedBook = bookRepository.save(testBook);
        
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Test Book");
        assertThat(savedBook.getIsbn()).isEqualTo("1234567890");
    }

    @Test
    void testFindBookById() {
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByIsbn() {
        entityManager.persistAndFlush(testBook);
        
        Optional<Book> foundBook = bookRepository.findByIsbn("1234567890");
        
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByTitleContainingIgnoreCase() {
        entityManager.persistAndFlush(testBook);
        
        List<Book> results = bookRepository.findByTitleContainingIgnoreCase("test");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByAuthorContainingIgnoreCase() {
        entityManager.persistAndFlush(testBook);
        
        List<Book> results = bookRepository.findByAuthorContainingIgnoreCase("test");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        assertThat(results.get(0).getAuthor()).containsIgnoringCase("test");
    }

    @Test
    void testSearchBooks() {
        entityManager.persistAndFlush(testBook);
        
        List<Book> results = bookRepository.searchBooks("%Test%");
        
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testCountByAuthorId() {
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        Long count = bookRepository.countByAuthorId(testAuthor.getId());
        
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void testUpdateBook() {
        Book savedBook = entityManager.persistAndFlush(testBook);
        savedBook.setDescription("Updated description");
        
        Book updatedBook = bookRepository.save(savedBook);
        
        assertThat(updatedBook.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testDeleteBook() {
        Book savedBook = entityManager.persistAndFlush(testBook);
        Long bookId = savedBook.getId();
        
        bookRepository.delete(savedBook);
        entityManager.flush();
        
        Optional<Book> deletedBook = bookRepository.findById(bookId);
        assertThat(deletedBook).isEmpty();
    }

    @Test
    void testFindAllBooks() {
        Book book1 = Book.builder()
                .title("Book 1")
                .author("Author 1")
                .authorEntity(testAuthor)
                .build();
        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .authorEntity(testAuthor)
                .build();
        
        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);
        
        long count = bookRepository.count();
        
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}
