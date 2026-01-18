package com.booklovers.web.controller;

import com.booklovers.dto.RegisterRequest;
import com.booklovers.dto.UserDto;
import com.booklovers.exception.ConflictException;
import com.booklovers.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private RegisterRequest registerRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        userDto = UserDto.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@example.com")
                .build();
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void testRegister_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "password123")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_ValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "")
                        .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any(RegisterRequest.class));
    }


    @Test
    void testRegister_IllegalArgumentException() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "password123")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register?error"));
    }
}
