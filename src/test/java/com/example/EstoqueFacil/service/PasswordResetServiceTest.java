package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.UserRepository;
import com.example.EstoqueFacil.security.PasswordStrengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordStrengthValidator passwordStrengthValidator;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@teste.com");
        user.setPassword("oldEncodedPassword");
    }

    @Test
    @DisplayName("Deve solicitar reset de senha para email válido")
    void shouldRequestResetForValidEmail() {
        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = passwordResetService.requestReset("user@teste.com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Password reset link sent");
    }

    @Test
    @DisplayName("Deve retornar erro para email inválido")
    void shouldReturnErrorForInvalidEmail() {
        when(userRepository.findByEmail("invalido@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = passwordResetService.requestReset("invalido@teste.com");

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).contains("Invalid email");
    }

    @Test
    @DisplayName("Deve bloquear após muitas tentativas de reset")
    void shouldBlockAfterManyResetAttempts() {
        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(user));

        for (int i = 0; i < 5; i++) {
            passwordResetService.requestReset("user@teste.com");
        }

        ResponseEntity<String> response = passwordResetService.requestReset("user@teste.com");

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).contains("Too many reset attempts");
    }
}