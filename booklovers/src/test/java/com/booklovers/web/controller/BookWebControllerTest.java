package com.booklovers.web.controller;

import com.booklovers.dto.*;
import com.booklovers.entity.User;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private StatsService statsService;

    private BookDto bookDto;
    private UserDto userDto;
    private ReviewDto reviewDto;
    private BookStatsDto bookStatsDto;

    @BeforeEach
    void setUp() {
        bookDto = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .role("USER")
                .build();

        reviewDto = ReviewDto.builder()
                .id(1L)
                .content("Great book!")
                .userId(1L)
                .build();

        bookStatsDto = BookStatsDto.builder()
                .readersCount(10)
                .averageRating(4.5)
                .ratingsCount(8)
                .build();
    }

    @Test
    @WithMockUser
    void testBooksPage_WithoutSearch() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(bookDto));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("search", (Object) null));

        verify(bookService).getAllBooks();
        verify(bookService, never()).searchBooks(anyString());
    }

    @Test
    @WithMockUser
    void testBooksPage_WithSearch() throws Exception {
        when(bookService.searchBooks("test")).thenReturn(Arrays.asList(bookDto));

        mockMvc.perform(get("/books").param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("search", "test"));

        verify(bookService).searchBooks("test");
        verify(bookService, never()).getAllBooks();
    }

    @Test
    @WithMockUser
    void testBookDetails_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        when(reviewService.getReviewsByBookId(1L)).thenReturn(Arrays.asList(reviewDto));
        when(ratingService.getRatingsByBookId(1L)).thenReturn(Collections.emptyList());
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Przeczytane"));
        when(statsService.getBookStats(1L)).thenReturn(bookStatsDto);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("reviewDto"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("userShelves"))
                .andExpect(model().attributeExists("bookStats"));

        verify(bookService).getBookById(1L);
        verify(reviewService).getReviewsByBookId(1L);
    }

    @Test
    @WithMockUser
    void testBookDetails_NotFound() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isNotFound());

        verify(bookService).getBookById(1L);
    }

    @Test
    @WithMockUser
    void testMyBooksPage_WithoutShelf() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Przeczytane"));
        when(bookService.getDefaultShelves()).thenReturn(Arrays.asList("Przeczytane", "Chcę przeczytać"));
        when(bookService.getUserBooks(1L)).thenReturn(Arrays.asList(bookDto));

        mockMvc.perform(get("/my-books"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("totalBooksCount"))
                .andExpect(model().attributeExists("shelves"))
                .andExpect(model().attributeExists("defaultShelves"));

        verify(bookService).getUserBooks(1L);
    }

    @Test
    @WithMockUser
    void testMyBooksPage_WithShelf() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Przeczytane"));
        when(bookService.getDefaultShelves()).thenReturn(Arrays.asList("Przeczytane", "Chcę przeczytać"));
        when(bookService.getUserBooks(1L)).thenReturn(Arrays.asList(bookDto));
        when(bookService.getUserBooksByShelf(1L, "Przeczytane")).thenReturn(Arrays.asList(bookDto));

        mockMvc.perform(get("/my-books").param("shelf", "Przeczytane"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-books"))
                .andExpect(model().attribute("currentShelf", "Przeczytane"));

        verify(bookService).getUserBooksByShelf(1L, "Przeczytane");
    }

    @Test
    @WithMockUser
    void testShelfBooksPage() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserBooksByShelf(1L, "Przeczytane")).thenReturn(Arrays.asList(bookDto));
        when(bookService.getUserBooks(1L)).thenReturn(Arrays.asList(bookDto));
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Przeczytane"));
        when(bookService.getDefaultShelves()).thenReturn(Arrays.asList("Przeczytane"));

        mockMvc.perform(get("/my-books/shelves/Przeczytane"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-books"))
                .andExpect(model().attribute("currentShelf", "Przeczytane"));

        verify(bookService).getUserBooksByShelf(1L, "Przeczytane");
    }

    @Test
    @WithMockUser
    void testCreateShelf_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doNothing().when(bookService).createShelf(1L, "New Shelf");

        mockMvc.perform(post("/my-books/shelves/create")
                        .param("shelfName", "New Shelf")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-books"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).createShelf(1L, "New Shelf");
    }

    @Test
    @WithMockUser
    void testCreateShelf_EmptyName() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);

        mockMvc.perform(post("/my-books/shelves/create")
                        .param("shelfName", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-books"))
                .andExpect(flash().attributeExists("error"));

        verify(bookService, never()).createShelf(anyLong(), anyString());
    }

    @Test
    @WithMockUser
    void testCreateShelf_BadRequestException() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doThrow(new com.booklovers.exception.BadRequestException("Cannot create default shelf"))
                .when(bookService).createShelf(1L, "Przeczytane");

        mockMvc.perform(post("/my-books/shelves/create")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-books"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void testDeleteShelf_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doNothing().when(bookService).deleteShelf(1L, "Test Shelf");

        mockMvc.perform(post("/my-books/shelves/Test+Shelf/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-books"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).deleteShelf(1L, "Test Shelf");
    }

    @Test
    @WithMockUser
    void testMoveBookToShelf_Success() throws Exception {
        doNothing().when(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");

        mockMvc.perform(post("/my-books/1/move")
                        .param("fromShelf", "From Shelf")
                        .param("toShelf", "To Shelf")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/my-books?shelf=*"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");
    }

    @Test
    @WithMockUser
    void testMoveBookToShelf_ConflictException() throws Exception {
        doThrow(new com.booklovers.exception.ConflictException("Book already exists"))
                .when(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");

        mockMvc.perform(post("/my-books/1/move")
                        .param("fromShelf", "From Shelf")
                        .param("toShelf", "To Shelf")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void testAddBookForm() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().attributeExists("bookDto"))
                .andExpect(model().attributeExists("authors"));

        verify(authorService).getAllAuthors();
    }

    @Test
    @WithMockUser
    void testAddBook_Success() throws Exception {
        when(bookService.createBook(any(BookDto.class))).thenReturn(bookDto);

        mockMvc.perform(post("/books/add")
                        .param("title", "New Book")
                        .param("author", "New Author")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).createBook(any(BookDto.class));
    }

    @Test
    @WithMockUser
    void testAddBook_ValidationError() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/books/add")
                        .param("title", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().attributeExists("authors"));

        verify(bookService, never()).createBook(any(BookDto.class));
    }

    @Test
    @WithMockUser
    void testAddBookToLibrary_Success() throws Exception {
        com.booklovers.dto.UserBookDto userBookDto = com.booklovers.dto.UserBookDto.builder()
                .id(1L)
                .bookId(1L)
                .shelfName("Przeczytane")
                .build();

        when(bookService.addBookToUserLibrary(1L, "Przeczytane")).thenReturn(userBookDto);

        mockMvc.perform(post("/books/1/add-to-library")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).addBookToUserLibrary(1L, "Przeczytane");
    }

    @Test
    @WithMockUser
    void testAddBookToLibrary_DefaultShelf() throws Exception {
        com.booklovers.dto.UserBookDto userBookDto = com.booklovers.dto.UserBookDto.builder()
                .id(1L)
                .bookId(1L)
                .shelfName("Moja biblioteczka")
                .build();

        when(bookService.addBookToUserLibrary(1L, "Moja biblioteczka")).thenReturn(userBookDto);

        mockMvc.perform(post("/books/1/add-to-library")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).addBookToUserLibrary(1L, "Moja biblioteczka");
    }

    @Test
    @WithMockUser
    void testAddBookToLibrary_ConflictException() throws Exception {
        doThrow(new com.booklovers.exception.ConflictException("Book already exists"))
                .when(bookService).addBookToUserLibrary(1L, "Przeczytane");

        mockMvc.perform(post("/books/1/add-to-library")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void testRemoveBookFromLibrary_Success() throws Exception {
        doNothing().when(bookService).removeBookFromUserLibrary(1L, "Przeczytane");

        mockMvc.perform(post("/books/1/remove-from-library")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-books?shelf=Przeczytane"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).removeBookFromUserLibrary(1L, "Przeczytane");
    }

    @Test
    @WithMockUser
    void testAddReview_Success() throws Exception {
        when(reviewService.createReview(eq(1L), any(ReviewDto.class))).thenReturn(reviewDto);
        doNothing().when(reviewService).createRatingAfterReview(eq(1L), eq(5));

        mockMvc.perform(post("/books/1/reviews")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Great book!")
                        .param("ratingValue", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService, atLeastOnce()).createReview(eq(1L), any(ReviewDto.class));
    }

    @Test
    @WithMockUser
    void testAddReview_ValidationError() throws Exception {
        mockMvc.perform(post("/books/1/reviews")
                        .param("content", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(reviewService, never()).createReview(anyLong(), any(ReviewDto.class));
    }

    @Test
    @WithMockUser
    void testAddReview_ConflictException() throws Exception {
        doThrow(new com.booklovers.exception.ConflictException("Review already exists"))
                .when(reviewService).createReview(eq(1L), any(ReviewDto.class));

        mockMvc.perform(post("/books/1/reviews")
                        .param("content", "Great book!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void testEditReviewForm_Success() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(reviewDto));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        when(userService.getCurrentUser()).thenReturn(userDto);

        mockMvc.perform(get("/books/1/reviews/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-review"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("reviewDto"));

        verify(reviewService).getReviewById(1L);
        verify(bookService).getBookById(1L);
    }

    @Test
    @WithMockUser
    void testEditReviewForm_Forbidden() throws Exception {
        ReviewDto otherReview = ReviewDto.builder()
                .id(1L)
                .userId(2L)
                .build();

        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(otherReview));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        when(userService.getCurrentUser()).thenReturn(userDto);

        mockMvc.perform(get("/books/1/reviews/1/edit"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditReviewForm_Admin() throws Exception {
        ReviewDto otherReview = ReviewDto.builder()
                .id(1L)
                .userId(2L)
                .build();

        UserDto adminUser = UserDto.builder()
                .id(1L)
                .username("admin")
                .role("ADMIN")
                .build();

        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(otherReview));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        when(userService.getCurrentUser()).thenReturn(adminUser);

        mockMvc.perform(get("/books/1/reviews/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-review"));
    }

    @Test
    @WithMockUser
    void testUpdateReview_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(reviewDto));
        when(reviewService.updateReview(eq(1L), any(ReviewDto.class))).thenReturn(reviewDto);
        doNothing().when(reviewService).createRatingAfterReview(eq(1L), eq(5));

        mockMvc.perform(post("/books/1/reviews/1/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Updated review")
                        .param("ratingValue", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService, atLeastOnce()).updateReview(eq(1L), any(ReviewDto.class));
    }

    @Test
    @WithMockUser
    void testUpdateReview_ValidationError() throws Exception {
        mockMvc.perform(post("/books/1/reviews/1/edit")
                        .param("content", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1/reviews/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateReview_AsAdmin() throws Exception {
        UserDto adminUser = UserDto.builder()
                .id(1L)
                .username("admin")
                .role("ADMIN")
                .build();

        ReviewDto existingReview = ReviewDto.builder()
                .id(1L)
                .userId(2L)
                .content("Original review")
                .build();

        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(existingReview));
        when(reviewService.updateReviewAsAdmin(eq(1L), any(ReviewDto.class))).thenReturn(existingReview);

        mockMvc.perform(post("/books/1/reviews/1/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Updated review")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService, atLeastOnce()).updateReviewAsAdmin(eq(1L), any(ReviewDto.class));
    }

    @Test
    @WithMockUser
    void testDeleteReview_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doNothing().when(reviewService).deleteReview(1L);

        mockMvc.perform(post("/books/1/reviews/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService).deleteReview(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteReview_AsAdmin() throws Exception {
        UserDto adminUser = UserDto.builder()
                .id(1L)
                .username("admin")
                .role("ADMIN")
                .build();

        when(userService.getCurrentUser()).thenReturn(adminUser);
        doNothing().when(reviewService).deleteReviewAsAdmin(1L);

        mockMvc.perform(post("/books/1/reviews/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService).deleteReviewAsAdmin(1L);
    }

    @Test
    @WithMockUser
    void testDeleteReview_Forbidden() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doThrow(new com.booklovers.exception.ForbiddenException("You can only delete your own reviews"))
                .when(reviewService).deleteReview(1L);

        mockMvc.perform(post("/books/1/reviews/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));
    }
}
