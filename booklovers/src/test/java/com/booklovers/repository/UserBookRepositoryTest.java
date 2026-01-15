package com.booklovers.repository;

import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserBookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserBookRepository userBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private User testUser;
    private Book testBook;
    private UserBook testUserBook;

    @BeforeEach
    void setUp() {
        Author author = Author.builder()
                .firstName("Test")
                .lastName("Author")
                .build();
        author = entityManager.persistAndFlush(author);

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.USER)
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        testBook = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .authorEntity(author)
                .build();
        testBook = entityManager.persistAndFlush(testBook);

        testUserBook = UserBook.builder()
                .user(testUser)
                .book(testBook)
                .shelfName("Przeczytane")
                .build();
    }

    @Test
    void testSaveUserBook() {
        UserBook savedUserBook = userBookRepository.save(testUserBook);
        
        assertThat(savedUserBook.getId()).isNotNull();
        assertThat(savedUserBook.getShelfName()).isEqualTo("Przeczytane");
    }

    @Test
    void testFindUserBookById() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        Optional<UserBook> foundUserBook = userBookRepository.findById(savedUserBook.getId());
        
        assertThat(foundUserBook).isPresent();
        assertThat(foundUserBook.get().getShelfName()).isEqualTo("Przeczytane");
    }

    @Test
    void testFindByUserId() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        List<UserBook> userBooks = userBookRepository.findByUserId(testUser.getId());
        
        assertThat(userBooks).hasSizeGreaterThanOrEqualTo(1);
        assertThat(userBooks.get(0).getShelfName()).isEqualTo("Przeczytane");
    }

    @Test
    void testFindByUserIdAndShelfName() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        List<UserBook> userBooks = userBookRepository.findByUserIdAndShelfName(testUser.getId(), "Przeczytane");
        
        assertThat(userBooks).hasSizeGreaterThanOrEqualTo(1);
        assertThat(userBooks.get(0).getShelfName()).isEqualTo("Przeczytane");
    }

    @Test
    void testFindByBookId() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        List<UserBook> userBooks = userBookRepository.findByBookId(testBook.getId());
        
        assertThat(userBooks).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testFindByUserIdAndBookId() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        List<UserBook> userBooks = userBookRepository.findByUserIdAndBookId(testUser.getId(), testBook.getId());
        
        assertThat(userBooks).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testFindByUserIdAndBookIdAndShelfName() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        
        Optional<UserBook> foundUserBook = userBookRepository.findByUserIdAndBookIdAndShelfName(
                testUser.getId(), testBook.getId(), "Przeczytane");
        
        assertThat(foundUserBook).isPresent();
        assertThat(foundUserBook.get().getShelfName()).isEqualTo("Przeczytane");
    }

    @Test
    void testFindDistinctShelfNamesByUserId() {
        UserBook userBook1 = UserBook.builder()
                .user(testUser)
                .book(testBook)
                .shelfName("Przeczytane")
                .build();
        
        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .authorEntity(testBook.getAuthorEntity())
                .build();
        book2 = entityManager.persistAndFlush(book2);
        
        UserBook userBook2 = UserBook.builder()
                .user(testUser)
                .book(book2)
                .shelfName("Chcę przeczytać")
                .build();
        
        entityManager.persistAndFlush(userBook1);
        entityManager.persistAndFlush(userBook2);
        
        List<String> shelves = userBookRepository.findDistinctShelfNamesByUserId(testUser.getId());
        
        assertThat(shelves).hasSizeGreaterThanOrEqualTo(2);
        assertThat(shelves).contains("Przeczytane", "Chcę przeczytać");
    }

    @Test
    void testCountBooksReadInYear() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        int currentYear = LocalDateTime.now().getYear();
        
        Long count = userBookRepository.countBooksReadInYear(testUser.getId(), currentYear);
        
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void testCountReadersByBookId() {
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("pass")
                .role(User.Role.USER)
                .build();
        user2 = entityManager.persistAndFlush(user2);
        
        UserBook userBook1 = UserBook.builder()
                .user(testUser)
                .book(testBook)
                .shelfName("Przeczytane")
                .build();
        
        UserBook userBook2 = UserBook.builder()
                .user(user2)
                .book(testBook)
                .shelfName("Przeczytane")
                .build();
        
        entityManager.persistAndFlush(userBook1);
        entityManager.persistAndFlush(userBook2);
        
        Long count = userBookRepository.countReadersByBookId(testBook.getId());
        
        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void testUpdateUserBook() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        savedUserBook.setShelfName("Teraz czytam");
        
        UserBook updatedUserBook = userBookRepository.save(savedUserBook);
        
        assertThat(updatedUserBook.getShelfName()).isEqualTo("Teraz czytam");
    }

    @Test
    void testDeleteUserBook() {
        UserBook savedUserBook = entityManager.persistAndFlush(testUserBook);
        Long userBookId = savedUserBook.getId();
        
        userBookRepository.delete(savedUserBook);
        entityManager.flush();
        
        Optional<UserBook> deletedUserBook = userBookRepository.findById(userBookId);
        assertThat(deletedUserBook).isEmpty();
    }

    @Test
    void testEmptyShelf() {
        UserBook emptyShelf = UserBook.builder()
                .user(testUser)
                .book(null)
                .shelfName("Empty Shelf")
                .build();
        
        UserBook savedShelf = entityManager.persistAndFlush(emptyShelf);
        
        assertThat(savedShelf.getId()).isNotNull();
        assertThat(savedShelf.getBook()).isNull();
        assertThat(savedShelf.getShelfName()).isEqualTo("Empty Shelf");
    }
}
