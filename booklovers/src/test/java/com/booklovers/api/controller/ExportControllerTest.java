package com.booklovers.api.controller;

import com.booklovers.dto.UserDataExportDto;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.service.export.ExportService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
class ExportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ExportService exportService;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser(username = "testuser")
    void testExportCurrentUserData_Success() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        UserDataExportDto exportData = UserDataExportDto.builder()
                .user(currentUser)
                .books(new ArrayList<>())
                .reviews(new ArrayList<>())
                .ratings(new ArrayList<>())
                .shelves(List.of("Przeczytane", "Chcę przeczytać"))
                .userBooks(new ArrayList<>())
                .build();
        
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(exportService.exportUserData(1L)).thenReturn(exportData);
        
        mockMvc.perform(get("/api/export/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.shelves.length()").value(2));
        
        verify(userService).getCurrentUser();
        verify(exportService).exportUserData(1L);
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testExportCurrentUserDataAsJson_Success() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        String jsonData = "{\"user\":{\"id\":1,\"username\":\"testuser\"},\"books\":[],\"reviews\":[]}";
        
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(exportService.exportUserDataAsJson(1L)).thenReturn(jsonData);
        
        mockMvc.perform(get("/api/export/user/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("user-data.json")));
        
        verify(userService).getCurrentUser();
        verify(exportService).exportUserDataAsJson(1L);
    }
    
    @Test
    void testExportCurrentUserData_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/export/user"))
                .andExpect(status().isUnauthorized());
        
        verify(userService, never()).getCurrentUser();
        verify(exportService, never()).exportUserData(anyLong());
    }
}
