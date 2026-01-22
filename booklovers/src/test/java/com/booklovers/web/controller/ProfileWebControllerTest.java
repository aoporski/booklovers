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
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    void testUpdateProfile_WithNewAvatarFile() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/old-avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.storeFile(any(), eq("avatars"))).thenReturn("avatars/new-avatar.jpg");
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile", "new-avatar.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/profile")
                        .file(avatarFile)
                        .param("firstName", "Updated")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).storeFile(any(), eq("avatars"));
        verify(fileStorageService).deleteFile(eq("old-avatar.jpg"), eq("avatars"));
        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_WithNewAvatarFile_ExternalUrlOld() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.storeFile(any(), eq("avatars"))).thenReturn("avatars/new-avatar.jpg");
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile", "new-avatar.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/profile")
                        .file(avatarFile)
                        .param("firstName", "Updated")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).storeFile(any(), eq("avatars"));
        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_WithNewAvatarFile_DeleteOldFileError() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/old-avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new RuntimeException("Delete error")).when(fileStorageService).deleteFile(anyString(), anyString());
        when(fileStorageService.storeFile(any(), eq("avatars"))).thenReturn("avatars/new-avatar.jpg");
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile", "new-avatar.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/profile")
                        .file(avatarFile)
                        .param("firstName", "Updated")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).storeFile(any(), eq("avatars"));
        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_WithoutAvatarFile() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/existing-avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService, never()).storeFile(any(), anyString());
        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
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

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_NoAvatar() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl(null)
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_EmptyAvatar() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_ExternalUrl() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://example.com/avatar.jpg"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_LocalFile_Jpeg() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.jpg")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.loadFileAsResource("avatar.jpg", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("avatar.jpg", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_LocalFile_Png() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.png")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.loadFileAsResource("avatar.png", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("avatar.png", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_LocalFile_Gif() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.gif")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.loadFileAsResource("avatar.gif", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/gif"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("avatar.gif", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_InvalidPath() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("invalid-path")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvatar_LoadError() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(fileStorageService.loadFileAsResource("avatar.jpg", "avatars"))
                .thenThrow(new RuntimeException("File not found"));

        mockMvc.perform(get("/profile/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_UserNotFound() throws Exception {
        when(userService.findByIdDto(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_NoAvatar() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl(null)
                .build();

        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_EmptyAvatar() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("")
                .build();

        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_ExternalUrl() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();

        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://example.com/avatar.jpg"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_LocalFile_Jpeg() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/user-avatar.jpg")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("user-avatar.jpg", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("user-avatar.jpg", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_LocalFile_Png() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/user-avatar.png")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("user-avatar.png", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("user-avatar.png", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_LocalFile_Gif() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/user-avatar.gif")
                .build();

        Resource resource = mock(Resource.class);
        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("user-avatar.gif", "avatars")).thenReturn(resource);

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/gif"))
                .andExpect(header().exists("Content-Disposition"));

        verify(fileStorageService).loadFileAsResource("user-avatar.gif", "avatars");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_InvalidPath() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("invalid")
                .build();

        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAvatar_LoadError() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/user-avatar.jpg")
                .build();

        when(userService.findByIdDto(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("user-avatar.jpg", "avatars"))
                .thenThrow(new RuntimeException("File not found"));

        mockMvc.perform(get("/profile/avatar/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteAccount_Success() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl(null)
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(userService).deleteCurrentUser();

        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?accountDeleted=true"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).getCurrentUser();
        verify(userService).deleteCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteAccount_WithLocalAvatar() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(fileStorageService).deleteFile("avatar.jpg", "avatars");
        doNothing().when(userService).deleteCurrentUser();

        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?accountDeleted=true"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).deleteFile("avatar.jpg", "avatars");
        verify(userService).deleteCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteAccount_WithExternalAvatar() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(userService).deleteCurrentUser();

        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?accountDeleted=true"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService, never()).deleteFile(anyString(), anyString());
        verify(userService).deleteCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteAccount_DeleteAvatarError() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .avatarUrl("avatars/avatar.jpg")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new RuntimeException("Delete error")).when(fileStorageService).deleteFile(anyString(), anyString());
        doNothing().when(userService).deleteCurrentUser();

        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?accountDeleted=true"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).deleteCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteAccount_Exception() throws Exception {
        UserDto currentUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new RuntimeException("Delete error")).when(userService).deleteCurrentUser();

        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));

        verify(userService).deleteCurrentUser();
    }
}
