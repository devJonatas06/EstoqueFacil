package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;
import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.UserMapper;
import com.example.EstoqueFacil.repository.RoleRepository;
import com.example.EstoqueFacil.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role employeeRole;
    private Role adminRole;
    private UserRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        employeeRole = new Role();
        employeeRole.setId(1L);
        employeeRole.setName("ROLE_EMPLOYEE");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        user = new User();
        user.setId(1L);
        user.setEmail("user@teste.com");
        user.setName("Usuário Teste");
        user.setPassword("encodedPassword");
        user.setActive(true);
        user.setRoles(Set.of(employeeRole));

        requestDTO = new UserRequestDTO();
        requestDTO.setEmail("user@teste.com");
        requestDTO.setName("Usuário Teste");
        requestDTO.setPassword("Password@123");
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(1L)
                .email("user@teste.com")
                .name("Usuário Teste")
                .active(true)
                .roles(Set.of("ROLE_EMPLOYEE"))
                .build();

        when(userRepository.existsByEmail("user@teste.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_EMPLOYEE")).thenReturn(Optional.of(employeeRole));
        when(userMapper.toEntity(requestDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@teste.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void shouldThrowExceptionWhenDuplicateEmail() {
        when(userRepository.existsByEmail("user@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    @Test
    @DisplayName("Deve encontrar usuário por ID")
    void shouldFindUserById() {
        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(1L)
                .email("user@teste.com")
                .name("Usuário Teste")
                .active(true)
                .roles(Set.of("ROLE_EMPLOYEE"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado por ID")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve desativar usuário")
    void shouldDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivate(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Deve alterar role do usuário")
    void shouldChangeUserRole() {
        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(1L)
                .email("user@teste.com")
                .name("Usuário Teste")
                .active(true)
                .roles(Set.of("ROLE_ADMIN"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.changeRole(1L, "ROLE_ADMIN");

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(user);
    }
}