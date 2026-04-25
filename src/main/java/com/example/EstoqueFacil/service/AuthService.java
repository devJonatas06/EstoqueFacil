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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;  // ✅ ADICIONAR
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordStrengthValidator passwordStrengthValidator;
    private final AuditServiceImpl auditService;

    public ResponseDto login(LoginRequestDto body) {
        log.info("Auth | Login attempt | email={}", body.email());

        if (loginAttemptService.isBlocked(body.email())) {
            throw new SecurityException("Too many login attempts. Try again later.");
        }

        User user = repository.findByEmail(body.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(body.password(), user.getPassword())) {
            loginAttemptService.loginFailed(body.email());
            auditService.recordAction(body.email(), "LOGIN_FAILED");
            throw new SecurityException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(body.email());
        auditService.recordAction(user.getEmail(), "LOGIN_SUCCESS");

        String token = tokenService.generateToken(user);
        return new ResponseDto(user.getName(), token);
    }

    public ResponseDto register(RegisterRequestDto body) {
        passwordStrengthValidator.validate(body.password());

        if (repository.findByEmail(body.email()).isPresent()) {
            throw new BusinessException("This email already exists, try another");
        }

        // ✅ BUSCAR A ROLE EMPLOYEE
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Role ROLE_EMPLOYEE não encontrada"));

        User newUser = new User();
        newUser.setEmail(body.email());
        newUser.setName(body.name());
        newUser.setPassword(passwordEncoder.encode(body.password()));
        newUser.setActive(true);
        newUser.setRoles(Set.of(employeeRole));  // ✅ ADICIONAR ROLE

        repository.save(newUser);
        auditService.recordAction(newUser.getEmail(), "REGISTER_NEW_USER");

        String token = tokenService.generateToken(newUser);
        log.info("Usuário registrado com sucesso - Email: {}, Role: EMPLOYEE", body.email());

        return new ResponseDto(newUser.getName(), token);
    }
}