package com.booklovers.api.controller;

import com.booklovers.dto.BookStatsDto;
import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.entity.User;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StatsService statsService;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser
    void testGetGlobalStats_Success() throws Exception {
        Map<String, Long> booksByGenre = new HashMap<>();
        booksByGenre.put("Fiction", 10L);
        booksByGenre.put("Non-Fiction", 5L);
        
        Map<String, Long> topAuthors = new HashMap<>();
        topAuthors.put("Author 1", 5L);
        
        Map<Integer, Long> ratingsDistribution = new HashMap<>();
        ratingsDistribution.put(5, 10L);
        ratingsDistribution.put(4, 5L);
        
        StatsDto stats = StatsDto.builder()
                .totalBooks(100)
                .totalUsers(50)
                .totalReviews(200)
                .totalRatings(300)
                .averageRating(4.5)
                .booksByGenre(booksByGenre)
                .topAuthors(topAuthors)
                .ratingsDistribution(ratingsDistribution)
                .build();
        
        when(statsService.getGlobalStats()).thenReturn(stats);
        
        mockMvc.perform(get("/api/stats/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalBooks").value(100))
                .andExpect(jsonPath("$.totalUsers").value(50))
                .andExpect(jsonPath("$.totalReviews").value(200))
                .andExpect(jsonPath("$.totalRatings").value(300))
                .andExpect(jsonPath("$.averageRating").value(4.5));
        
        verify(statsService).getGlobalStats();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetCurrentUserStats_Success() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        UserStatsDto userStats = UserStatsDto.builder()
                .userId(1L)
                .username("testuser")
                .booksRead(10)
                .reviewsWritten(5)
                .ratingsGiven(8)
                .averageRatingGiven(4.5)
                .shelvesCount(3)
                .booksReadThisYear(5)
                .readingChallengeGoal(12)
                .build();
        
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(statsService.getUserStats(1L)).thenReturn(userStats);
        
        mockMvc.perform(get("/api/stats/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.booksRead").value(10))
                .andExpect(jsonPath("$.reviewsWritten").value(5))
                .andExpect(jsonPath("$.ratingsGiven").value(8))
                .andExpect(jsonPath("$.booksReadThisYear").value(5))
                .andExpect(jsonPath("$.readingChallengeGoal").value(12));
        
        verify(userService).getCurrentUser();
        verify(statsService).getUserStats(1L);
    }
    
    @Test
    @WithMockUser
    void testGetUserStats_Success() throws Exception {
        UserStatsDto userStats = UserStatsDto.builder()
                .userId(1L)
                .username("testuser")
                .booksRead(10)
                .reviewsWritten(5)
                .ratingsGiven(8)
                .averageRatingGiven(4.5)
                .shelvesCount(3)
                .booksReadThisYear(5)
                .readingChallengeGoal(12)
                .build();
        
        when(statsService.getUserStats(1L)).thenReturn(userStats);
        
        mockMvc.perform(get("/api/stats/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.booksRead").value(10));
        
        verify(statsService).getUserStats(1L);
    }
    
    @Test
    @WithMockUser
    void testGetBookStats_Success() throws Exception {
        Map<Integer, Long> ratingsDistribution = new HashMap<>();
        ratingsDistribution.put(5, 10L);
        ratingsDistribution.put(4, 5L);
        ratingsDistribution.put(3, 2L);
        
        BookStatsDto bookStats = BookStatsDto.builder()
                .bookId(1L)
                .bookTitle("Test Book")
                .readersCount(20)
                .averageRating(4.5)
                .ratingsCount(17)
                .ratingsDistribution(ratingsDistribution)
                .build();
        
        when(statsService.getBookStats(1L)).thenReturn(bookStats);
        
        mockMvc.perform(get("/api/stats/book/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookId").value(1L))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.readersCount").value(20))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.ratingsCount").value(17));
        
        verify(statsService).getBookStats(1L);
    }
}
