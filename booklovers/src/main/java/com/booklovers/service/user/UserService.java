package com.booklovers.service.user;

import com.booklovers.dto.RegisterRequest;
import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto register(RegisterRequest request);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Optional<UserDto> findByIdDto(Long id);
    UserDto getCurrentUser();
    UserDto updateUser(UserDto userDto);
    List<UserDto> getAllUsers();
    void deleteUser(Long id);
    void deleteCurrentUser();
    UserDto blockUser(Long id);
    UserDto unblockUser(Long id);
}
