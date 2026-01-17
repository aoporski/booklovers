package com.booklovers.api.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {
    
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
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllBooks_Success() throws Exception {
        BookDto book1 = BookDto.builder().id(1L).title("Book 1").author("Author 1").build();
        BookDto book2 = BookDto.builder().id(2L).title("Book 2").author("Author 2").build();
        List<BookDto> books = Arrays.asList(book1, book2);
        
        when(bookService.getAllBooks()).thenReturn(books);
        
        mockMvc.perform(get("/api/admin/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(bookService).getAllBooks();
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateBook_Success() throws Exception {
        BookDto bookDto = BookDto.builder()
                .title("New Book")
                .author("New Author")
                .isbn("1234567890")
                .build();
        
        BookDto createdBook = BookDto.builder()
                .id(1L)
                .title("New Book")
                .author("New Author")
                .isbn("1234567890")
                .build();
        
        when(bookService.createBook(any(BookDto.class))).thenReturn(createdBook);
        
        mockMvc.perform(post("/api/admin/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Book"));
        
        verify(bookService).createBook(any(BookDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateBook_Success() throws Exception {
        BookDto bookDto = BookDto.builder()
                .title("Updated Book")
                .author("Updated Author")
                .build();
        
        BookDto updatedBook = BookDto.builder()
                .id(1L)
                .title("Updated Book")
                .author("Updated Author")
                .build();
        
        when(bookService.updateBook(eq(1L), any(BookDto.class))).thenReturn(updatedBook);
        
        mockMvc.perform(put("/api/admin/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Book"));
        
        verify(bookService).updateBook(eq(1L), any(BookDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(1L);
        
        mockMvc.perform(delete("/api/admin/books/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(bookService).deleteBook(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllUsers_Success() throws Exception {
        UserDto user1 = UserDto.builder().id(1L).username("user1").build();
        UserDto user2 = UserDto.builder().id(2L).username("user2").build();
        List<UserDto> users = Arrays.asList(user1, user2);
        
        when(userService.getAllUsers()).thenReturn(users);
        
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(userService).getAllUsers();
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testBlockUser_Success() throws Exception {
        UserDto blockedUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(true)
                .build();
        
        when(userService.blockUser(1L)).thenReturn(blockedUser);
        
        mockMvc.perform(put("/api/admin/users/1/block")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isBlocked").value(true));
        
        verify(userService).blockUser(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUnblockUser_Success() throws Exception {
        UserDto unblockedUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(false)
                .build();
        
        when(userService.unblockUser(1L)).thenReturn(unblockedUser);
        
        mockMvc.perform(put("/api/admin/users/1/unblock")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isBlocked").value(false));
        
        verify(userService).unblockUser(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllAuthors_Success() throws Exception {
        AuthorDto author1 = AuthorDto.builder().id(1L).firstName("John").lastName("Doe").build();
        AuthorDto author2 = AuthorDto.builder().id(2L).firstName("Jane").lastName("Smith").build();
        List<AuthorDto> authors = Arrays.asList(author1, author2);
        
        when(authorService.getAllAuthors()).thenReturn(authors);
        
        mockMvc.perform(get("/api/admin/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(authorService).getAllAuthors();
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAuthorById_Success() throws Exception {
        AuthorDto author = AuthorDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .build();
        
        when(authorService.getAuthorById(1L)).thenReturn(Optional.of(author));
        
        mockMvc.perform(get("/api/admin/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
        
        verify(authorService).getAuthorById(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAuthorById_NotFound() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/admin/authors/1"))
                .andExpect(status().isNotFound());
        
        verify(authorService).getAuthorById(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateAuthor_Success() throws Exception {
        AuthorDto authorDto = AuthorDto.builder()
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .build();
        
        AuthorDto createdAuthor = AuthorDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .biography("Test biography")
                .build();
        
        when(authorService.createAuthor(any(AuthorDto.class))).thenReturn(createdAuthor);
        
        mockMvc.perform(post("/api/admin/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
        
        verify(authorService).createAuthor(any(AuthorDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateAuthor_Success() throws Exception {
        AuthorDto authorDto = AuthorDto.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();
        
        AuthorDto updatedAuthor = AuthorDto.builder()
                .id(1L)
                .firstName("Updated")
                .lastName("Name")
                .build();
        
        when(authorService.updateAuthor(eq(1L), any(AuthorDto.class))).thenReturn(updatedAuthor);
        
        mockMvc.perform(put("/api/admin/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Updated"));
        
        verify(authorService).updateAuthor(eq(1L), any(AuthorDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteAuthor_Success() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);
        
        mockMvc.perform(delete("/api/admin/authors/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(authorService).deleteAuthor(1L);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteReviewAsAdmin_Success() throws Exception {
        doNothing().when(reviewService).deleteReviewAsAdmin(1L);
        
        mockMvc.perform(delete("/api/admin/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(reviewService).deleteReviewAsAdmin(1L);
    }
    
}
