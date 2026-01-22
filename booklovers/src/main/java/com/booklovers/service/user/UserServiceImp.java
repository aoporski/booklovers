package com.booklovers.service.user;

import com.booklovers.dto.RegisterRequest;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import com.booklovers.exception.ConflictException;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        log.info("Rejestracja nowego użytkownika: username={}, email={}", request.getUsername(), request.getEmail());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Próba rejestracji z istniejącym username: {}", request.getUsername());
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Próba rejestracji z istniejącym email: {}", request.getEmail());
            throw new ConflictException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Użytkownik zarejestrowany pomyślnie: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<UserDto> findByIdDto(Long id) {
        log.debug("Pobieranie użytkownika jako DTO: userId={}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }
    
    @Override
    public UserDto getCurrentUser() {
        log.debug("Pobieranie aktualnego użytkownika");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Aktualny użytkownik: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        return userMapper.toDto(user);
    }
    
    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        log.info("Aktualizacja danych użytkownika");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Aktualizacja użytkownika: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika do aktualizacji: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        
        if (userDto.getFirstName() != null) {
            log.debug("Aktualizacja firstName dla użytkownika: userId={}", user.getId());
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            log.debug("Aktualizacja lastName dla użytkownika: userId={}", user.getId());
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getBio() != null) {
            log.debug("Aktualizacja bio dla użytkownika: userId={}", user.getId());
            user.setBio(userDto.getBio());
        }
        if (userDto.getAvatarUrl() != null) {
            log.debug("Aktualizacja avatarUrl dla użytkownika: userId={}", user.getId());
            user.setAvatarUrl(userDto.getAvatarUrl());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            log.debug("Aktualizacja hasła dla użytkownika: userId={}", user.getId());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Dane użytkownika zaktualizowane pomyślnie: userId={}, username={}", updatedUser.getId(), updatedUser.getUsername());
        return userMapper.toDto(updatedUser);
    }
    
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Usuwanie użytkownika: userId={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Próba usunięcia nieistniejącego użytkownika: userId={}", id);
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("Użytkownik usunięty pomyślnie: userId={}", id);
    }
    
    @Override
    @Transactional
    public void deleteCurrentUser() {
        log.info("Usuwanie konta aktualnego użytkownika");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Usuwanie konta użytkownika: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika do usunięcia: username={}", username);
                    return new ResourceNotFoundException("User", username);
                });
        Long userId = user.getId();
        log.info("Usuwanie konta użytkownika: userId={}, username={}", userId, username);
        userRepository.deleteById(userId);
        log.info("Konto użytkownika usunięte pomyślnie: userId={}, username={}", userId, username);
    }
    
    @Override
    @Transactional
    public UserDto blockUser(Long id) {
        log.info("Blokowanie użytkownika: userId={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika do zablokowania: userId={}", id);
                    return new ResourceNotFoundException("User", id);
                });
        user.setIsBlocked(true);
        User saved = userRepository.save(user);
        log.info("Użytkownik zablokowany pomyślnie: userId={}, username={}", saved.getId(), saved.getUsername());
        return userMapper.toDto(saved);
    }
    
    @Override
    @Transactional
    public UserDto unblockUser(Long id) {
        log.info("Odblokowywanie użytkownika: userId={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono użytkownika do odblokowania: userId={}", id);
                    return new ResourceNotFoundException("User", id);
                });
        user.setIsBlocked(false);
        User saved = userRepository.save(user);
        log.info("Użytkownik odblokowany pomyślnie: userId={}, username={}", saved.getId(), saved.getUsername());
        return userMapper.toDto(saved);
    }
}
