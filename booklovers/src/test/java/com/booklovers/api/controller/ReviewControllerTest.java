package com.booklovers.api.controller;

import com.booklovers.dto.ReviewDto;
import com.booklovers.exception.ForbiddenException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.review.ReviewService;
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

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ReviewService reviewService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(username = "testuser")
    void testCreateReview_Success() throws Exception {
        ReviewDto reviewDto = ReviewDto.builder()
                .content("Great book!")
                .ratingValue(5)
                .build();
        
        ReviewDto createdReview = ReviewDto.builder()
                .id(1L)
                .content("Great book!")
                .ratingValue(5)
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .bookTitle("Test Book")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(reviewService.createReview(eq(1L), any(ReviewDto.class))).thenReturn(createdReview);
        
        mockMvc.perform(post("/api/reviews/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Great book!"))
                .andExpect(jsonPath("$.ratingValue").value(5))
                .andExpect(jsonPath("$.bookId").value(1L));
        
        verify(reviewService).createReview(eq(1L), any(ReviewDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testCreateReview_ValidationError() throws Exception {
        ReviewDto invalidReview = ReviewDto.builder()
                .content("")
                .ratingValue(10)
                .build();
        
        mockMvc.perform(post("/api/reviews/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
        
        verify(reviewService, never()).createReview(anyLong(), any(ReviewDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testUpdateReview_Success() throws Exception {
        ReviewDto updateDto = ReviewDto.builder()
                .content("Updated review content")
                .ratingValue(4)
                .build();
        
        ReviewDto updatedReview = ReviewDto.builder()
                .id(1L)
                .content("Updated review content")
                .ratingValue(4)
                .userId(1L)
                .username("testuser")
                .bookId(1L)
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(reviewService.updateReview(eq(1L), any(ReviewDto.class))).thenReturn(updatedReview);
        
        mockMvc.perform(put("/api/reviews/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Updated review content"))
                .andExpect(jsonPath("$.ratingValue").value(4));
        
        verify(reviewService).updateReview(eq(1L), any(ReviewDto.class));
    }
    
    @Test
    @WithMockUser(username = "otheruser")
    void testUpdateReview_Forbidden() throws Exception {
        ReviewDto updateDto = ReviewDto.builder()
                .content("Updated review")
                .ratingValue(4)
                .build();
        
        when(reviewService.updateReview(eq(1L), any(ReviewDto.class)))
                .thenThrow(new ForbiddenException("You don't have permission to update this review"));
        
        mockMvc.perform(put("/api/reviews/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
        
        verify(reviewService).updateReview(eq(1L), any(ReviewDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testDeleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);
        
        mockMvc.perform(delete("/api/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(reviewService).deleteReview(1L);
    }
    
    @Test
    @WithMockUser(username = "otheruser")
    void testDeleteReview_Forbidden() throws Exception {
        doThrow(new ForbiddenException("You don't have permission to delete this review"))
                .when(reviewService).deleteReview(1L);
        
        mockMvc.perform(delete("/api/reviews/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(reviewService).deleteReview(1L);
    }
    
    @Test
    @WithMockUser
    void testGetReviewById_Success() throws Exception {
        ReviewDto review = ReviewDto.builder()
                .id(1L)
                .content("Great book!")
                .ratingValue(5)
                .userId(1L)
                .bookId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(review));
        
        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Great book!"));
        
        verify(reviewService).getReviewById(1L);
    }
    
    @Test
    @WithMockUser
    void testGetReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isNotFound());
        
        verify(reviewService).getReviewById(1L);
    }
    
    @Test
    @WithMockUser
    void testGetReviewsByBookId_Success() throws Exception {
        ReviewDto review1 = ReviewDto.builder().id(1L).content("Review 1").bookId(1L).build();
        ReviewDto review2 = ReviewDto.builder().id(2L).content("Review 2").bookId(1L).build();
        List<ReviewDto> reviews = Arrays.asList(review1, review2);
        
        when(reviewService.getReviewsByBookId(1L)).thenReturn(reviews);
        
        mockMvc.perform(get("/api/reviews/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
        
        verify(reviewService).getReviewsByBookId(1L);
    }
    
    @Test
    @WithMockUser
    void testGetReviewsByUserId_Success() throws Exception {
        ReviewDto review1 = ReviewDto.builder().id(1L).content("Review 1").userId(1L).build();
        ReviewDto review2 = ReviewDto.builder().id(2L).content("Review 2").userId(1L).build();
        List<ReviewDto> reviews = Arrays.asList(review1, review2);
        
        when(reviewService.getReviewsByUserId(1L)).thenReturn(reviews);
        
        mockMvc.perform(get("/api/reviews/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(reviewService).getReviewsByUserId(1L);
    }
}
