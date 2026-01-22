package com.booklovers.service.user;

import com.booklovers.dto.UserDto;
import com.booklovers.entity.Book;
import com.booklovers.entity.Review;
import com.booklovers.entity.User;
import com.booklovers.entity.UserBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .role(User.Role.USER)
                .isBlocked(false)
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .role("USER")
                .isBlocked(false)
                .createdAt(LocalDateTime.of(2020, 1, 1, 12, 0))
                .build();
    }

    @Test
    void testToDto_Success() {
        UserDto result = userMapper.toDto(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBio()).isEqualTo("Test bio");
        assertThat(result.getAvatarUrl()).isEqualTo("http://example.com/avatar.jpg");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getIsBlocked()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0));
    }

    @Test
    void testToDto_WithUserBooks() {
        Book book1 = Book.builder().id(1L).build();
        Book book2 = Book.builder().id(2L).build();
        Book book3 = Book.builder().id(1L).build(); // Duplikat ID

        UserBook userBook1 = UserBook.builder().id(1L).book(book1).build();
        UserBook userBook2 = UserBook.builder().id(2L).book(book2).build();
        UserBook userBook3 = UserBook.builder().id(3L).book(book3).build(); // Duplikat book ID
        UserBook userBook4 = UserBook.builder().id(4L).book(null).build(); // Null book

        user.setUserBooks(Arrays.asList(userBook1, userBook2, userBook3, userBook4));

        UserDto result = userMapper.toDto(user);

        assertThat(result.getBooksCount()).isEqualTo(2); // Tylko unikalne book IDs (1L i 2L), null book jest pomijany
    }

    @Test
    void testToDto_WithReviews() {
        Review review1 = Review.builder().id(1L).build();
        Review review2 = Review.builder().id(2L).build();

        user.setReviews(Arrays.asList(review1, review2));

        UserDto result = userMapper.toDto(user);

        assertThat(result.getReviewsCount()).isEqualTo(2);
    }

    @Test
    void testToDto_WithNullUserBooksAndReviews() {
        user.setUserBooks(null);
        user.setReviews(null);

        UserDto result = userMapper.toDto(user);

        assertThat(result.getBooksCount()).isEqualTo(0);
        assertThat(result.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void testToDto_WithEmptyUserBooksAndReviews() {
        user.setUserBooks(Collections.emptyList());
        user.setReviews(Collections.emptyList());

        UserDto result = userMapper.toDto(user);

        assertThat(result.getBooksCount()).isEqualTo(0);
        assertThat(result.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void testToDto_WithNullRole() {
        user.setRole(null);

        UserDto result = userMapper.toDto(user);

        assertThat(result.getRole()).isNull();
    }

    @Test
    void testToDto_WithAdminRole() {
        user.setRole(User.Role.ADMIN);

        UserDto result = userMapper.toDto(user);

        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void testToDto_WithBlockedUser() {
        user.setIsBlocked(true);

        UserDto result = userMapper.toDto(user);

        assertThat(result.getIsBlocked()).isTrue();
    }

    @Test
    void testToDto_NullUser() {
        UserDto result = userMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithoutOptionalFields() {
        User userMinimal = User.builder()
                .id(2L)
                .username("minimaluser")
                .email("minimal@example.com")
                .password("password")
                .role(User.Role.USER)
                .isBlocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        UserDto result = userMapper.toDto(userMinimal);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("minimaluser");
        assertThat(result.getEmail()).isEqualTo("minimal@example.com");
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getBio()).isNull();
        assertThat(result.getAvatarUrl()).isNull();
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getIsBlocked()).isFalse();
    }

    @Test
    void testToEntity_Success() {
        User result = userMapper.toEntity(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBio()).isEqualTo("Test bio");
        assertThat(result.getAvatarUrl()).isEqualTo("http://example.com/avatar.jpg");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void testToEntity_WithAdminRole() {
        userDto.setRole("ADMIN");

        User result = userMapper.toEntity(userDto);

        assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void testToEntity_WithNullRole() {
        userDto.setRole(null);

        User result = userMapper.toEntity(userDto);

        assertThat(result.getRole()).isEqualTo(User.Role.USER); // Domyślna wartość
    }

    @Test
    void testToEntity_WithInvalidRole() {
        userDto.setRole("INVALID");

        // Powinno rzucić IllegalArgumentException przy valueOf
        assertThrows(IllegalArgumentException.class, () -> {
            userMapper.toEntity(userDto);
        });
    }

    @Test
    void testToEntity_NullDto() {
        User result = userMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithoutOptionalFields() {
        UserDto dtoMinimal = UserDto.builder()
                .id(2L)
                .username("minimaluser")
                .email("minimal@example.com")
                .role("USER")
                .build();

        User result = userMapper.toEntity(dtoMinimal);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("minimaluser");
        assertThat(result.getEmail()).isEqualTo("minimal@example.com");
        assertThat(result.getPassword()).isNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getBio()).isNull();
        assertThat(result.getAvatarUrl()).isNull();
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }
}
