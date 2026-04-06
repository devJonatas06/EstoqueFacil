package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.auth.LoginRequestDto;
import com.example.EstoqueFacil.dto.auth.RegisterRequestDto;
import com.example.EstoqueFacil.dto.auth.ResponseDto;
import com.example.EstoqueFacil.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
        log.info("Tentativa de login - Email: {}", body.email());
        ResponseDto response = authService.login(body);
        log.info("Login realizado com sucesso - Email: {}", body.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@RequestBody RegisterRequestDto body) {
        log.info("Tentativa de registro - Email: {}", body.email());
        ResponseDto response = authService.register(body);
        log.info("Registro realizado com sucesso - Email: {}", body.email());
        return ResponseEntity.ok(response);
    }
}