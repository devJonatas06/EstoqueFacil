package com.example.EstoqueFacil.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank String name,
        @Email String email,
        @Size(min = 8, message = "Password must have at least 8 characters") String password
) {}