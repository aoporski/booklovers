package com.booklovers.service.user;

import com.booklovers.dto.RegisterRequest;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImp userService;
    
    private RegisterRequest registerRequest;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
    }
    
    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(com.booklovers.dto.UserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .build());
        
        com.booklovers.dto.UserDto result = userService.register(registerRequest);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testRegister_UsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        assertThrows(com.booklovers.exception.ConflictException.class, () -> {
            userService.register(registerRequest);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(com.booklovers.exception.ConflictException.class, () -> {
            userService.register(registerRequest);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByUsername_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        Optional<User> result = userService.findByUsername("testuser");
        
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        Optional<User> result = userService.findByUsername("testuser");
        
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindById_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Optional<User> result = userService.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        Optional<User> result = userService.findById(1L);
        
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetCurrentUser_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);
        
        UserDto result = userService.getCurrentUser();
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetCurrentUser_NotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        assertThrows(com.booklovers.exception.ResourceNotFoundException.class, () -> {
            userService.getCurrentUser();
        });
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testUpdateUser_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Old")
                .lastName("Name")
                .build();
        
        UserDto updateDto = UserDto.builder()
                .firstName("New")
                .lastName("Name")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();
        
        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("New")
                .lastName("Name")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();
        
        UserDto resultDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .firstName("New")
                .lastName("Name")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(resultDto);
        
        UserDto result = userService.updateUser(updateDto);
        
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("Test bio", result.getBio());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_WithPassword() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("oldEncodedPassword")
                .build();
        
        UserDto updateDto = UserDto.builder()
                .password("newPassword123")
                .build();
        
        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("newEncodedPassword")
                .build();
        
        UserDto resultDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(resultDto);
        
        UserDto result = userService.updateUser(updateDto);
        
        assertNotNull(result);
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_EmptyPassword() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("oldEncodedPassword")
                .build();
        
        UserDto updateDto = UserDto.builder()
                .password("")
                .build();
        
        UserDto resultDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(resultDto);
        
        UserDto result = userService.updateUser(updateDto);
        
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDto updateDto = UserDto.builder()
                .firstName("New")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        assertThrows(com.booklovers.exception.ResourceNotFoundException.class, () -> {
            userService.updateUser(updateDto);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetAllUsers_Success() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .build();
        
        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .build();
        
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .username("user1")
                .build();
        
        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .username("user2")
                .build();
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);
        
        List<UserDto> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_Empty() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        
        List<UserDto> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void testDeleteUser_Success() {
        userService.deleteUser(1L);
        
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testBlockUser_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(false)
                .build();
        
        User blockedUser = User.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(true)
                .build();
        
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(true)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(blockedUser);
        when(userMapper.toDto(blockedUser)).thenReturn(userDto);
        
        UserDto result = userService.blockUser(1L);
        
        assertNotNull(result);
        assertTrue(result.getIsBlocked());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testBlockUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(com.booklovers.exception.ResourceNotFoundException.class, () -> {
            userService.blockUser(1L);
        });
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUnblockUser_Success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(true)
                .build();
        
        User unblockedUser = User.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(false)
                .build();
        
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .isBlocked(false)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(unblockedUser);
        when(userMapper.toDto(unblockedUser)).thenReturn(userDto);
        
        UserDto result = userService.unblockUser(1L);
        
        assertNotNull(result);
        assertFalse(result.getIsBlocked());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUnblockUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(com.booklovers.exception.ResourceNotFoundException.class, () -> {
            userService.unblockUser(1L);
        });
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}
