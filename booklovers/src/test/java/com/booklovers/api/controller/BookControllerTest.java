package com.booklovers.api.controller;

import com.booklovers.dto.BookDto;
import com.booklovers.service.book.BookService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BookService bookService;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser
    void testGetAllBooks() throws Exception {
        BookDto book1 = BookDto.builder().id(1L).title("Book 1").author("Author 1").build();
        BookDto book2 = BookDto.builder().id(2L).title("Book 2").author("Author 2").build();
        
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));
        
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    @WithMockUser
    void testGetBookById() throws Exception {
        BookDto book = BookDto.builder().id(1L).title("Book 1").author("Author 1").build();
        
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));
        
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    @WithMockUser
    void testGetBookById_NotFound() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isNotFound());
    }
}
