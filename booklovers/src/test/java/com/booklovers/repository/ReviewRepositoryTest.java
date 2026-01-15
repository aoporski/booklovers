package com.booklovers.repository;

import com.booklovers.entity.Author;
import com.booklovers.entity.Book;
import com.booklovers.entity.Review;
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
class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private User testUser;
    private Book testBook;
    private Review testReview;

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

        testReview = Review.builder()
                .content("Great book!")
                .user(testUser)
                .book(testBook)
                .build();
    }

    @Test
    void testSaveReview() {
        Review savedReview = reviewRepository.save(testReview);
        
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getContent()).isEqualTo("Great book!");
    }

    @Test
    void testFindReviewById() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        
        Optional<Review> foundReview = reviewRepository.findById(savedReview.getId());
        
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("Great book!");
    }

    @Test
    void testFindByBookId() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        
        List<Review> reviews = reviewRepository.findByBookId(testBook.getId());
        
        assertThat(reviews).hasSizeGreaterThanOrEqualTo(1);
        assertThat(reviews.get(0).getContent()).isEqualTo("Great book!");
    }

    @Test
    void testFindByUserId() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        
        List<Review> reviews = reviewRepository.findByUserId(testUser.getId());
        
        assertThat(reviews).hasSizeGreaterThanOrEqualTo(1);
        assertThat(reviews.get(0).getContent()).isEqualTo("Great book!");
    }

    @Test
    void testFindByUserIdAndBookId() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        
        Optional<Review> foundReview = reviewRepository.findByUserIdAndBookId(testUser.getId(), testBook.getId());
        
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("Great book!");
    }

    @Test
    void testCountByBookId() {
        Review review1 = Review.builder()
                .content("Review 1")
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
        
        Review review2 = Review.builder()
                .content("Review 2")
                .user(user2)
                .book(testBook)
                .build();
        
        entityManager.persistAndFlush(review1);
        entityManager.persistAndFlush(review2);
        
        Long count = reviewRepository.countByBookId(testBook.getId());
        
        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void testUpdateReview() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        savedReview.setContent("Updated review content");
        
        Review updatedReview = reviewRepository.save(savedReview);
        
        assertThat(updatedReview.getContent()).isEqualTo("Updated review content");
    }

    @Test
    void testDeleteReview() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        Long reviewId = savedReview.getId();
        
        reviewRepository.delete(savedReview);
        entityManager.flush();
        
        Optional<Review> deletedReview = reviewRepository.findById(reviewId);
        assertThat(deletedReview).isEmpty();
    }

    @Test
    void testFindAllReviews() {
        Review review1 = Review.builder()
                .content("Review 1")
                .user(testUser)
                .book(testBook)
                .build();
        
        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .authorEntity(testBook.getAuthorEntity())
                .build();
        book2 = entityManager.persistAndFlush(book2);
        
        Review review2 = Review.builder()
                .content("Review 2")
                .user(testUser)
                .book(book2)
                .build();
        
        entityManager.persistAndFlush(review1);
        entityManager.persistAndFlush(review2);
        
        long count = reviewRepository.count();
        
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testReviewCreatedAt() {
        Review savedReview = entityManager.persistAndFlush(testReview);
        
        assertThat(savedReview.getCreatedAt()).isNotNull();
    }
}
