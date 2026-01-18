package com.booklovers.web.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.ReviewDto;
import com.booklovers.dto.UserDto;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private ReviewService reviewService;

    private BookDto bookDto;
    private UserDto userDto;
    private AuthorDto authorDto;
    private ReviewDto reviewDto;

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
                .email("test@example.com")
                .isBlocked(false)
                .build();

        authorDto = AuthorDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        reviewDto = ReviewDto.builder()
                .id(1L)
                .content("Great book")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminPanel_Success() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(bookDto));
        when(userService.getAllUsers()).thenReturn(Arrays.asList(userDto));
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(authorDto));
        when(reviewService.getAllReviews()).thenReturn(Arrays.asList(reviewDto));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditBookForm_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(authorDto));

        mockMvc.perform(get("/admin/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-book"))
                .andExpect(model().attributeExists("bookDto"))
                .andExpect(model().attributeExists("authors"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditBookForm_NotFound() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/books/1/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBook_Success() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(authorDto));
        when(bookService.updateBook(anyLong(), any(BookDto.class))).thenReturn(bookDto);

        mockMvc.perform(post("/admin/books/1/edit")
                        .with(csrf())
                        .param("title", "Updated Book")
                        .param("author", "Updated Author"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).updateBook(eq(1L), any(BookDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBook_ValidationErrors() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(authorDto));

        mockMvc.perform(post("/admin/books/1/edit")
                        .with(csrf())
                        .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-book"))
                .andExpect(model().attributeExists("authors"));

        verify(bookService, never()).updateBook(anyLong(), any(BookDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(post("/admin/books/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(bookService).deleteBook(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockUser_Success() throws Exception {
        when(userService.blockUser(1L)).thenReturn(userDto);

        mockMvc.perform(post("/admin/users/1/block")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).blockUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnblockUser_Success() throws Exception {
        when(userService.unblockUser(1L)).thenReturn(userDto);

        mockMvc.perform(post("/admin/users/1/unblock")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).unblockUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/users/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddAuthorForm() throws Exception {
        mockMvc.perform(get("/admin/authors/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().attributeExists("authorDto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddAuthor_Success() throws Exception {
        when(authorService.createAuthor(any(AuthorDto.class))).thenReturn(authorDto);

        mockMvc.perform(post("/admin/authors/add")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(authorService).createAuthor(any(AuthorDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddAuthor_ValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/authors/add")
                        .with(csrf())
                        .param("firstName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"));

        verify(authorService, never()).createAuthor(any(AuthorDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditAuthorForm_Success() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(Optional.of(authorDto));

        mockMvc.perform(get("/admin/authors/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-author"))
                .andExpect(model().attributeExists("authorDto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditAuthorForm_NotFound() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/authors/1/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAuthor_Success() throws Exception {
        when(authorService.updateAuthor(anyLong(), any(AuthorDto.class))).thenReturn(authorDto);

        mockMvc.perform(post("/admin/authors/1/edit")
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(authorService).updateAuthor(eq(1L), any(AuthorDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAuthor_ValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/authors/1/edit")
                        .with(csrf())
                        .param("firstName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-author"));

        verify(authorService, never()).updateAuthor(anyLong(), any(AuthorDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteAuthor_Success() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);

        mockMvc.perform(post("/admin/authors/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(authorService).deleteAuthor(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReviewAsAdmin(1L);

        mockMvc.perform(post("/admin/reviews/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService).deleteReviewAsAdmin(1L);
    }
}
