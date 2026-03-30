package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;

public interface UserService {

    UserResponseDTO create(UserRequestDTO userRequest);

    UserResponseDTO findByEmail(String email);
}