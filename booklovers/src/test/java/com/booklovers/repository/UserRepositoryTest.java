package com.booklovers.repository;

import com.booklovers.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .isBlocked(false)
                .build();
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(testUser);
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindUserById() {
        User savedUser = entityManager.persistAndFlush(testUser);
        
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testFindByUsername() {
        entityManager.persistAndFlush(testUser);
        
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByEmail() {
        entityManager.persistAndFlush(testUser);
        
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testExistsByUsername() {
        entityManager.persistAndFlush(testUser);
        
        boolean exists = userRepository.existsByUsername("testuser");
        boolean notExists = userRepository.existsByUsername("nonexistent");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testExistsByEmail() {
        entityManager.persistAndFlush(testUser);
        
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testUpdateUser() {
        User savedUser = entityManager.persistAndFlush(testUser);
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        
        User updatedUser = userRepository.save(savedUser);
        
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
    }

    @Test
    void testDeleteUser() {
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();
        
        userRepository.delete(savedUser);
        entityManager.flush();
        
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testFindAllUsers() {
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("pass")
                .role(User.Role.USER)
                .build();
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("pass")
                .role(User.Role.USER)
                .build();
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        
        long count = userRepository.count();
        
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        
        assertThat(foundUser).isEmpty();
    }
}
