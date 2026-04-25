package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "testSecretKeyForUnitTestsOnly12345678901234567890");
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void shouldGenerateValidToken() {
        User user = new User();
        user.setEmail("teste@email.com");

        String token = tokenService.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Deve validar token e retornar subject")
    void shouldValidateTokenAndReturnSubject() {
        User user = new User();
        user.setEmail("teste@email.com");

        String token = tokenService.generateToken(user);
        String subject = tokenService.validateToken(token);

        assertThat(subject).isEqualTo("teste@email.com");
    }

    @Test
    @DisplayName("Deve retornar null para token inválido")
    void shouldReturnNullForInvalidToken() {
        String subject = tokenService.validateToken("token-invalido");

        assertThat(subject).isNull();
    }
}