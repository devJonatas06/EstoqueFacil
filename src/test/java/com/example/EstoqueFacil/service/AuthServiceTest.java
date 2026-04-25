package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.auth.LoginRequestDto;
import com.example.EstoqueFacil.dto.auth.RegisterRequestDto;
import com.example.EstoqueFacil.dto.auth.ResponseDto;
import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.repository.RoleRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import com.example.EstoqueFacil.security.PasswordStrengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private PasswordStrengthValidator passwordStrengthValidator;
    @Mock private AuditServiceImpl auditService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role employeeRole;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        employeeRole = new Role();
        employeeRole.setId(1L);
        employeeRole.setName("ROLE_EMPLOYEE");

        user = new User();
        user.setId(1L);
        user.setEmail("user@teste.com");
        user.setName("Usuário Teste");
        user.setPassword("encodedPassword");
        user.setActive(true);
        user.setRoles(Set.of(employeeRole));

        loginRequest = new LoginRequestDto("user@teste.com", "Password@123");
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(loginAttemptService.isBlocked("user@teste.com")).thenReturn(false);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        ResponseDto response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.name()).isEqualTo("Usuário Teste");
        verify(loginAttemptService).loginSucceeded("user@teste.com");
        verify(auditService).recordAction("user@teste.com", "LOGIN_SUCCESS");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não existe")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta")
    void shouldThrowExceptionWhenWrongPassword() {
        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(false);
        when(loginAttemptService.isBlocked("user@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid credentials");

        verify(loginAttemptService).loginFailed("user@teste.com");
        verify(auditService).recordAction("user@teste.com", "LOGIN_FAILED");
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void shouldRegisterSuccessfully() {
        RegisterRequestDto registerRequest = new RegisterRequestDto("Novo User", "novo@teste.com", "Password@123");

        when(userRepository.findByEmail("novo@teste.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_EMPLOYEE")).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-token");
        doNothing().when(passwordStrengthValidator).validate(anyString());

        ResponseDto response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordStrengthValidator).validate("Password@123");
        verify(auditService).recordAction("novo@teste.com", "REGISTER_NEW_USER");
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar com email duplicado")
    void shouldThrowExceptionWhenDuplicateEmail() {
        RegisterRequestDto registerRequest = new RegisterRequestDto("Novo User", "user@teste.com", "Password@123");

        when(userRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email already exists");
    }
}