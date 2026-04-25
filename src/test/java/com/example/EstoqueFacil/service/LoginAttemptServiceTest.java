package com.example.EstoqueFacil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("Deve permitir login após 5 tentativas falhas? Não, deve bloquear")
    void shouldBlockAfter5FailedAttempts() {
        String email = "teste@email.com";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(email);
        }

        assertThat(loginAttemptService.isBlocked(email)).isTrue();
    }

    @Test
    @DisplayName("Deve resetar tentativas após login bem sucedido")
    void shouldResetAttemptsAfterSuccessfulLogin() {
        String email = "teste@email.com";

        for (int i = 0; i < 3; i++) {
            loginAttemptService.loginFailed(email);
        }

        loginAttemptService.loginSucceeded(email);

        assertThat(loginAttemptService.isBlocked(email)).isFalse();
    }

    @Test
    @DisplayName("Não deve bloquear antes de 5 tentativas")
    void shouldNotBlockBefore5Attempts() {
        String email = "teste@email.com";

        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed(email);
        }

        assertThat(loginAttemptService.isBlocked(email)).isFalse();
    }

    @Test
    @DisplayName("Email sem tentativas não deve estar bloqueado")
    void shouldNotBlockEmailWithNoAttempts() {
        assertThat(loginAttemptService.isBlocked("novo@email.com")).isFalse();
    }
}