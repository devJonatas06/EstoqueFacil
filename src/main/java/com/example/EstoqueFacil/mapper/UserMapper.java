package com.example.EstoqueFacil.mapper;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;
import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Será criptografada depois no service
        return user;
    }

    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .active(user.isActive())
                .roles(user.getRoles() != null ? 
                       user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) : 
                       null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}