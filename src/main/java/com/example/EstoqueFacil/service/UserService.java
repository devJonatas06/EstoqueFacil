package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO create(UserRequestDTO requestDTO);

    UserResponseDTO findById(Long id);

    UserResponseDTO findByEmail(String email);

    List<UserResponseDTO> findAll();

    void deactivate(Long id);

    UserResponseDTO changeRole(Long id, String role);
}