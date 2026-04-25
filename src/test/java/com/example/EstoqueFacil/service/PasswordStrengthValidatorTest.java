package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.security.PasswordStrengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordStrengthValidatorTest {

    private PasswordStrengthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordStrengthValidator();
    }

    @Test
    @DisplayName("Deve aceitar senha forte")
    void shouldAcceptStrongPassword() {
        assertThatCode(() -> validator.validate("SenhaForte@123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar senha muito curta")
    void shouldRejectShortPassword() {
        assertThatThrownBy(() -> validator.validate("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("senha");
    }

    @Test
    @DisplayName("Deve rejeitar senha sem números")
    void shouldRejectPasswordWithoutNumbers() {
        assertThatThrownBy(() -> validator.validate("SenhaForte@"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar senha sem letras maiúsculas")
    void shouldRejectPasswordWithoutUppercase() {
        assertThatThrownBy(() -> validator.validate("senha@123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar senha sem caracteres especiais")
    void shouldRejectPasswordWithoutSpecialChars() {
        assertThatThrownBy(() -> validator.validate("SenhaForte123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar senha comum (blacklist)")
    void shouldRejectCommonPassword() {
        assertThatThrownBy(() -> validator.validate("12345678"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}