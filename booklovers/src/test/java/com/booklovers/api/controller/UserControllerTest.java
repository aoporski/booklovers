package com.booklovers.api.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.user.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetCurrentUser_Success() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .booksCount(5)
                .reviewsCount(3)
                .build();
        
        when(userService.getCurrentUser()).thenReturn(userDto);
        
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.booksCount").value(5))
                .andExpect(jsonPath("$.reviewsCount").value(3));
        
        verify(userService, times(1)).getCurrentUser();
    }
    
    @Test
    void testGetCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
        
        verify(userService, never()).getCurrentUser();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testUpdateCurrentUser_Success() throws Exception {
        UserDto updateDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .bio("Updated bio")
                .avatarUrl("http://example.com/new-avatar.jpg")
                .build();
        
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .bio("Updated bio")
                .avatarUrl("http://example.com/new-avatar.jpg")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .booksCount(5)
                .reviewsCount(3)
                .build();
        
        when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUser);
        
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://example.com/new-avatar.jpg"));
        
        verify(userService, times(1)).updateUser(any(UserDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testUpdateCurrentUser_ValidationError() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .id(1L)
                .username("ab")
                .email("invalid-email")
                .build();
        
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).updateUser(any(UserDto.class));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetAllUsers_Success() throws Exception {
        UserDto user1 = UserDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .role("USER")
                .booksCount(10)
                .build();
        
        UserDto user2 = UserDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .role("USER")
                .booksCount(5)
                .build();
        
        List<UserDto> users = Arrays.asList(user1, user2);
        
        when(userService.getAllUsers()).thenReturn(users);
        
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("user2"));
        
        verify(userService).getAllUsers();
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(1L);
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 1"))
                .when(userService).deleteUser(1L);
        
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(userService).deleteUser(1L);
    }
    
    @Test
    void testDeleteUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteCurrentUser_Success() throws Exception {
        doNothing().when(userService).deleteCurrentUser();
        
        mockMvc.perform(delete("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteCurrentUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteCurrentUser();
        
        mockMvc.perform(delete("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(userService).deleteCurrentUser();
    }

    @Test
    void testDeleteCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(userService, never()).deleteCurrentUser();
    }
}
