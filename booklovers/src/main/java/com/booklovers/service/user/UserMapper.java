package com.booklovers.service.user;

import com.booklovers.dto.UserDto;
import com.booklovers.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isBlocked(user.getIsBlocked())
                .createdAt(user.getCreatedAt())
                .booksCount(user.getUserBooks() != null ? 
                    (int) user.getUserBooks().stream()
                        .filter(ub -> ub.getBook() != null)
                        .map(ub -> ub.getBook().getId())
                        .distinct()
                        .count() : 0)
                .reviewsCount(user.getReviews() != null ? user.getReviews().size() : 0)
                .build();
    }
    
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .bio(dto.getBio())
                .avatarUrl(dto.getAvatarUrl())
                .role(dto.getRole() != null ? dto.getRole() : User.Role.USER)
                .build();
    }
}
