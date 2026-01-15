package com.booklovers.repository;

import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.Rating;
import com.booklovers.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RatingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private User testUser;
    private Book testBook;
    private Rating testRating;

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

        testRating = Rating.builder()
                .value(5)
                .user(testUser)
                .book(testBook)
                .build();
    }

    @Test
    void testSaveRating() {
        Rating savedRating = ratingRepository.save(testRating);
        
        assertThat(savedRating.getId()).isNotNull();
        assertThat(savedRating.getValue()).isEqualTo(5);
    }

    @Test
    void testFindRatingById() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        
        Optional<Rating> foundRating = ratingRepository.findById(savedRating.getId());
        
        assertThat(foundRating).isPresent();
        assertThat(foundRating.get().getValue()).isEqualTo(5);
    }

    @Test
    void testFindByBookId() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        
        List<Rating> ratings = ratingRepository.findByBookId(testBook.getId());
        
        assertThat(ratings).hasSizeGreaterThanOrEqualTo(1);
        assertThat(ratings.get(0).getValue()).isEqualTo(5);
    }

    @Test
    void testFindByUserId() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        
        List<Rating> ratings = ratingRepository.findByUserId(testUser.getId());
        
        assertThat(ratings).hasSizeGreaterThanOrEqualTo(1);
        assertThat(ratings.get(0).getValue()).isEqualTo(5);
    }

    @Test
    void testFindByUserIdAndBookId() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        
        Optional<Rating> foundRating = ratingRepository.findByUserIdAndBookId(testUser.getId(), testBook.getId());
        
        assertThat(foundRating).isPresent();
        assertThat(foundRating.get().getValue()).isEqualTo(5);
    }

    @Test
    void testGetAverageRatingByBookId() {
        Rating rating1 = Rating.builder()
                .value(5)
                .user(testUser)
                .book(testBook)
                .build();
        
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("pass")
                .role(User.Role.USER)
                .build();
        user2 = entityManager.persistAndFlush(user2);
        
        Rating rating2 = Rating.builder()
                .value(3)
                .user(user2)
                .book(testBook)
                .build();
        
        entityManager.persistAndFlush(rating1);
        entityManager.persistAndFlush(rating2);
        
        Double average = ratingRepository.getAverageRatingByBookId(testBook.getId());
        
        assertThat(average).isEqualTo(4.0);
    }

    @Test
    void testCountByBookId() {
        entityManager.persistAndFlush(testRating);
        
        Long count = ratingRepository.countByBookId(testBook.getId());
        
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void testUpdateRating() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        savedRating.setValue(4);
        
        Rating updatedRating = ratingRepository.save(savedRating);
        
        assertThat(updatedRating.getValue()).isEqualTo(4);
    }

    @Test
    void testDeleteRating() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        Long ratingId = savedRating.getId();
        
        ratingRepository.delete(savedRating);
        entityManager.flush();
        
        Optional<Rating> deletedRating = ratingRepository.findById(ratingId);
        assertThat(deletedRating).isEmpty();
    }

    @Test
    void testFindAllRatings() {
        Rating rating1 = Rating.builder()
                .value(5)
                .user(testUser)
                .book(testBook)
                .build();
        
        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .authorEntity(testBook.getAuthorEntity())
                .build();
        book2 = entityManager.persistAndFlush(book2);
        
        Rating rating2 = Rating.builder()
                .value(4)
                .user(testUser)
                .book(book2)
                .build();
        
        entityManager.persistAndFlush(rating1);
        entityManager.persistAndFlush(rating2);
        
        long count = ratingRepository.count();
        
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}
