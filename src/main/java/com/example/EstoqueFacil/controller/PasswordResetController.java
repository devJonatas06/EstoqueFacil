package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        log.info("Reset de senha - Solicitação para email: {}", email);
        ResponseEntity<String> response = passwordResetService.requestReset(email);
        log.info("Reset de senha - Solicitação processada para: {}", email);
        return response;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        log.info("Reset de senha - Tentativa de redefinição com token");
        ResponseEntity<String> response = passwordResetService.resetPassword(token, newPassword);
        log.info("Reset de senha - Processo finalizado");
        return response;
    }
}