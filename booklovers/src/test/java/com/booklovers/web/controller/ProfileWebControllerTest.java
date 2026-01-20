package com.booklovers.web.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.export.ExportService;
import com.booklovers.service.file.FileStorageService;
import com.booklovers.service.import_.ImportService;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ExportService exportService;

    @MockBean
    private ImportService importService;

    @MockBean
    private StatsService statsService;

    @MockBean
    private FileStorageService fileStorageService;

    private UserDto userDto;
    private UserStatsDto userStatsDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        userStatsDto = UserStatsDto.builder()
                .userId(1L)
                .username("testuser")
                .booksRead(10)
                .reviewsWritten(5)
                .ratingsGiven(8)
                .booksReadThisYear(5)
                .readingChallengeGoal(12)
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testProfilePage_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(statsService.getUserStats(1L)).thenReturn(userStatsDto);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userDto"))
                .andExpect(model().attributeExists("userStats"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testProfilePage_Exception() throws Exception {
        when(userService.getCurrentUser()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name")
                        .param("bio", "New bio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_Exception() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doThrow(new RuntimeException("Update error")).when(userService).updateUser(any(UserDto.class));

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testExportJson_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(exportService.exportUserDataAsJson(1L)).thenReturn("{\"user\":{}}");

        mockMvc.perform(get("/profile/export/json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testExportJson_Exception() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(exportService.exportUserDataAsJson(1L)).thenThrow(new RuntimeException("Export error"));

        mockMvc.perform(get("/profile/export/json"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testExportCsv_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(exportService.exportUserDataAsCsv(1L)).thenReturn("User Data Export\n");

        mockMvc.perform(get("/profile/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testExportCsv_Exception() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        when(exportService.exportUserDataAsCsv(1L)).thenThrow(new RuntimeException("Export error"));

        mockMvc.perform(get("/profile/export/csv"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testImport_JsonFormat() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doNothing().when(importService).importUserDataFromJson(anyLong(), anyString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.json", "application/json", "{\"user\":{}}".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(importService).importUserDataFromJson(1L, "{\"user\":{}}");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testImport_CsvFormat() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doNothing().when(importService).importUserDataFromCsv(anyLong(), anyString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", "User Data Export\n".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(importService).importUserDataFromCsv(1L, "User Data Export\n");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testImport_EmptyFile() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.json", "application/json", new byte[0]);

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));

        verify(importService, never()).importUserDataFromJson(anyLong(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testImport_UnsupportedFormat() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xml", "application/xml", "<data></data>".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));

        verify(importService, never()).importUserDataFromJson(anyLong(), anyString());
        verify(importService, never()).importUserDataFromCsv(anyLong(), anyString());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testImport_NoFileExtension() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "data", "application/octet-stream", "some content".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));

        verify(importService, never()).importUserDataFromJson(anyLong(), anyString());
        verify(importService, never()).importUserDataFromCsv(anyLong(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testImport_Exception() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userDto);
        doThrow(new RuntimeException("Import error")).when(importService).importUserDataFromJson(anyLong(), anyString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.json", "application/json", "{}".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));
    }
}
