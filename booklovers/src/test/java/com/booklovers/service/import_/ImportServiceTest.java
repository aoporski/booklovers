package com.booklovers.service.import_;

import com.booklovers.dto.*;
import com.booklovers.entity.Book;
import com.booklovers.entity.User;
import com.booklovers.exception.BadRequestException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.BookRepository;
import com.booklovers.repository.UserRepository;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookService bookService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private RatingService ratingService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private ImportServiceImp importService;

    private User user;
    private Book book;
    private UserDataExportDto exportData;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        UserBookDto userBook = UserBookDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .shelfName("Test Shelf")
                .build();

        exportData = UserDataExportDto.builder()
                .user(UserDto.builder().id(1L).username("testuser").build())
                .userBooks(Arrays.asList(userBook))
                .reviews(Collections.emptyList())
                .ratings(Collections.emptyList())
                .build();
    }

    @Test
    void testImportUserDataFromJson_InvalidJson() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(objectMapper.copy()).thenReturn(mockMapper);
        when(mockMapper.registerModule(any(JavaTimeModule.class))).thenReturn(mockMapper);
        when(mockMapper.disable(any(SerializationFeature.class))).thenReturn(mockMapper);
        when(mockMapper.readValue(anyString(), eq(UserDataExportDto.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        assertThatThrownBy(() -> importService.importUserDataFromJson(1L, "invalid json"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid JSON format");
    }



    @Test
    void testImportUserDataFromCsv_EmptyData() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        importService.importUserDataFromCsv(1L, csvData);

        verify(userRepository).findById(1L);
    }

    @Test
    void testImportUserDataFromCsv_WithUserData() {
        String csvData = "User Data Export\n" +
                "Username,testuser\n" +
                "Email,test@test.com\n" +
                "First Name,Test\n" +
                "Last Name,User\n" +
                "Bio,Test bio\n";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        importService.importUserDataFromCsv(1L, csvData);

        verify(userRepository).findById(1L);
    }
}
