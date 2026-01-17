package com.booklovers.api.controller;

import com.booklovers.dto.RatingDto;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.rating.RatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(RatingController.class)
class RatingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RatingService ratingService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrUpdateRating_Success() throws Exception {
        RatingDto ratingDto = RatingDto.builder()
                .value(5)
                .build();
        
        RatingDto createdRating = RatingDto.builder()
                .id(1L)
                .value(5)
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .bookTitle("Test Book")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(ratingService.createOrUpdateRating(eq(1L), any(RatingDto.class))).thenReturn(createdRating);
        
        mockMvc.perform(post("/api/ratings/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.bookId").value(1L));
        
        verify(ratingService).createOrUpdateRating(eq(1L), any(RatingDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrUpdateRating_ValidationError() throws Exception {
        RatingDto invalidRating = RatingDto.builder()
                .value(10)
                .build();
        
        mockMvc.perform(post("/api/ratings/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRating)))
                .andExpect(status().isBadRequest());
        
        verify(ratingService, never()).createOrUpdateRating(anyLong(), any(RatingDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testDeleteRating_Success() throws Exception {
        doNothing().when(ratingService).deleteRating(1L);
        
        mockMvc.perform(delete("/api/ratings/books/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(ratingService).deleteRating(1L);
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testDeleteRating_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Rating not found for book id: 1"))
                .when(ratingService).deleteRating(1L);
        
        mockMvc.perform(delete("/api/ratings/books/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(ratingService).deleteRating(1L);
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetMyRating_Success() throws Exception {
        RatingDto rating = RatingDto.builder()
                .id(1L)
                .value(5)
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(ratingService.getRatingByBookId(1L)).thenReturn(Optional.of(rating));
        
        mockMvc.perform(get("/api/ratings/books/1/my-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.value").value(5));
        
        verify(ratingService).getRatingByBookId(1L);
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetMyRating_NotFound() throws Exception {
        when(ratingService.getRatingByBookId(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/ratings/books/1/my-rating"))
                .andExpect(status().isNotFound());
        
        verify(ratingService).getRatingByBookId(1L);
    }
    
    @Test
    @WithMockUser
    void testGetRatingsByBookId_Success() throws Exception {
        RatingDto rating1 = RatingDto.builder().id(1L).value(5).bookId(1L).build();
        RatingDto rating2 = RatingDto.builder().id(2L).value(4).bookId(1L).build();
        List<RatingDto> ratings = Arrays.asList(rating1, rating2);
        
        when(ratingService.getRatingsByBookId(1L)).thenReturn(ratings);
        
        mockMvc.perform(get("/api/ratings/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
        
        verify(ratingService).getRatingsByBookId(1L);
    }
    
    @Test
    @WithMockUser
    void testGetRatingsByUserId_Success() throws Exception {
        RatingDto rating1 = RatingDto.builder().id(1L).value(5).userId(1L).build();
        RatingDto rating2 = RatingDto.builder().id(2L).value(4).userId(1L).build();
        List<RatingDto> ratings = Arrays.asList(rating1, rating2);
        
        when(ratingService.getRatingsByUserId(1L)).thenReturn(ratings);
        
        mockMvc.perform(get("/api/ratings/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(ratingService).getRatingsByUserId(1L);
    }
}
