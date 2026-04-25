// src/test/java/com/example/EstoqueFacil/service/CustomDetailsServiceTest.java
package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CustomDetailsService customDetailsService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");

        user = new User();
        user.setId(1L);
        user.setEmail("admin@teste.com");
        user.setPassword("encodedPassword");
        user.setActive(true);
        user.setRoles(Set.of(role));
    }

    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByEmailWithRoles("admin@teste.com")).thenReturn(Optional.of(user));

        var userDetails = customDetailsService.loadUserByUsername("admin@teste.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@teste.com");
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmailWithRoles("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customDetailsService.loadUserByUsername("naoexiste@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }
}