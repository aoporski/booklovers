package com.booklovers.api.controller;

import com.booklovers.dto.BookDto;
import com.booklovers.dto.UserBookDto;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.service.book.BookService;
import com.booklovers.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@WebMvcTest(BookController.class)
class BookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BookService bookService;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private BookDto bookDto;
    private UserDto userDto;
    private UserBookDto userBookDto;
    
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
                .build();
        
        userBookDto = UserBookDto.builder()
                .id(1L)
                .bookId(1L)
                .shelfName("Przeczytane")
                .build();
    }
    
    @Test
    @WithMockUser
    void testGetAllBooks() throws Exception {
        BookDto book1 = BookDto.builder().id(1L).title("Book 1").author("Author 1").build();
        BookDto book2 = BookDto.builder().id(2L).title("Book 2").author("Author 2").build();
        
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));
        
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(bookService).getAllBooks();
    }
    
    @Test
    @WithMockUser
    void testGetBookById() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(bookDto));
        
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
        
        verify(bookService).getBookById(1L);
    }
    
    @Test
    @WithMockUser
    void testGetBookById_NotFound() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isNotFound());
        
        verify(bookService).getBookById(1L);
    }
    
    @Test
    @WithMockUser
    void testCreateBook_EndpointDoesNotExist() throws Exception {
        BookDto inputDto = BookDto.builder()
                .title("New Book")
                .author("New Author")
                .build();
        
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(csrf()))
                .andExpect(status().is5xxServerError()); // Spring zwraca 500 dla nieistniejących endpointów
        
        verify(bookService, never()).createBook(any(BookDto.class));
    }
    
    @Test
    @WithMockUser
    void testUpdateBook_EndpointDoesNotExist() throws Exception {
        // Endpoint PUT /api/books/{id} nie istnieje - Spring może zwrócić 404, 405 lub 500
        // Najważniejsze jest to, że serwis nie został wywołany - endpoint nie działa poprawnie
        BookDto updateDto = BookDto.builder()
                .title("Updated Book")
                .author("Updated Author")
                .build();
        
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().is5xxServerError()); // Spring zwraca 500 dla nieistniejących endpointów
        
        verify(bookService, never()).updateBook(anyLong(), any(BookDto.class));
    }
    
    @Test
    @WithMockUser
    void testDeleteBook_EndpointDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/books/1")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
        
        verify(bookService, never()).deleteBook(anyLong());
    }
    
    @Test
    @WithMockUser
    void testSearchBooks_Success() throws Exception {
        when(bookService.searchBooks("test")).thenReturn(Arrays.asList(bookDto));
        
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        
        verify(bookService).searchBooks("test");
    }
    
    @Test
    @WithMockUser
    void testSearchBooks_Empty() throws Exception {
        when(bookService.searchBooks("nonexistent")).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/books/search")
                        .param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @WithMockUser
    void testGetMyBooks_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserBooks(1L)).thenReturn(Arrays.asList(bookDto));
        
        mockMvc.perform(get("/api/books/my-books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
        
        verify(userService).getCurrentUser();
        verify(bookService).getUserBooks(1L);
    }
    
    @Test
    @WithMockUser
    void testAddBookToLibrary_Success() throws Exception {
        when(bookService.addBookToUserLibrary(1L, "Przeczytane")).thenReturn(userBookDto);
        
        mockMvc.perform(post("/api/books/1/add-to-library")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.shelfName").value("Przeczytane"));
        
        verify(bookService).addBookToUserLibrary(1L, "Przeczytane");
    }
    
    @Test
    @WithMockUser
    void testAddBookToLibrary_DefaultShelf() throws Exception {
        when(bookService.addBookToUserLibrary(1L, "Moja biblioteczka")).thenReturn(userBookDto);
        
        mockMvc.perform(post("/api/books/1/add-to-library")
                        .with(csrf()))
                .andExpect(status().isOk());
        
        verify(bookService).addBookToUserLibrary(1L, "Moja biblioteczka");
    }
    
    @Test
    @WithMockUser
    void testRemoveBookFromLibrary_Success() throws Exception {
        doNothing().when(bookService).removeBookFromUserLibrary(1L, "Przeczytane");
        
        mockMvc.perform(delete("/api/books/1/remove-from-library")
                        .param("shelfName", "Przeczytane")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(bookService).removeBookFromUserLibrary(1L, "Przeczytane");
    }
    
    @Test
    @WithMockUser
    void testRemoveBookFromLibrary_DefaultShelf() throws Exception {
        doNothing().when(bookService).removeBookFromUserLibrary(1L, "Moja biblioteczka");
        
        mockMvc.perform(delete("/api/books/1/remove-from-library")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(bookService).removeBookFromUserLibrary(1L, "Moja biblioteczka");
    }
    
    @Test
    @WithMockUser
    void testGetMyShelves_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserShelves(1L)).thenReturn(Arrays.asList("Przeczytane", "Chcę przeczytać"));
        
        mockMvc.perform(get("/api/books/my-books/shelves"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(userService).getCurrentUser();
        verify(bookService).getUserShelves(1L);
    }
    
    @Test
    @WithMockUser
    void testGetMyBooksByShelf_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(bookService.getUserBooksByShelf(1L, "Przeczytane")).thenReturn(Arrays.asList(bookDto));
        
        mockMvc.perform(get("/api/books/my-books/shelves/Przeczytane"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
        
        verify(userService).getCurrentUser();
        verify(bookService).getUserBooksByShelf(1L, "Przeczytane");
    }
    
    @Test
    @WithMockUser
    void testMoveBookToShelf_Success() throws Exception {
        doNothing().when(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");
        
        mockMvc.perform(put("/api/books/1/move-to-shelf")
                        .param("fromShelf", "From Shelf")
                        .param("toShelf", "To Shelf")
                        .with(csrf()))
                .andExpect(status().isOk());
        
        verify(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");
    }
    
    @Test
    @WithMockUser
    void testMoveBookToShelf_Exception() throws Exception {
        doThrow(new RuntimeException("Error")).when(bookService).moveBookToShelf(1L, "From Shelf", "To Shelf");
        
        mockMvc.perform(put("/api/books/1/move-to-shelf")
                        .param("fromShelf", "From Shelf")
                        .param("toShelf", "To Shelf")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
