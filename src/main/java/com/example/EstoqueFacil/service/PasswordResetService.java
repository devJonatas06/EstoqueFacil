package com.example.EstoqueFacil.service;


import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.UserRepository;
import com.example.EstoqueFacil.security.PasswordStrengthValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordStrengthValidator passwordStrengthValidator;

    private static final ConcurrentHashMap<String, ResetTokenData> resetTokens = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> resetAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int TOKEN_EXPIRATION_MINUTES = 5;


    public ResponseEntity<String> requestReset(String email) {

        log.info("PasswordReset | Request attempt | email={}", email);

        if (isBlocked(email)) {
            log.warn("PasswordReset | Request blocked | email={} | attempts={}",
                    email, getAttempts(email));
            return badRequest("Too many reset attempts. Try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            incrementAttempts(email);
            log.warn("PasswordReset | Request failed | email={} | reason=USER_NOT_FOUND", email);
            return badRequest("Invalid email");
        }

        incrementAttempts(email);
        String token = generateToken(email);

        log.info("PasswordReset | Token generated | email={}", email);

        System.out.println(
                "Password reset link: http://localhost:8080/auth/reset-password?token=" + token
        );

        return ResponseEntity.ok("Password reset link sent.");
    }

    public ResponseEntity<String> resetPassword(String token, String newPassword) {

        ResetTokenData tokenData = validateToken(token);
        if (tokenData == null) {
            return badRequest("Invalid or expired token");
        }

        if (!isPasswordStrong(newPassword, tokenData.email())) {
            return badRequest("Password does not meet security requirements.");
        }

        updateUserPassword(tokenData.email(), newPassword);

        clearResetData(token, tokenData.email());

        log.info("PasswordReset | Password updated | email={}", tokenData.email());

        return ResponseEntity.ok("Password updated successfully.");
    }


    private boolean isBlocked(String email) {
        return getAttempts(email) >= MAX_ATTEMPTS;
    }

    private int getAttempts(String email) {
        return resetAttempts.getOrDefault(email, 0);
    }

    private void incrementAttempts(String email) {
        resetAttempts.merge(email, 1, Integer::sum);
    }

    private String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);

        resetTokens.put(token, new ResetTokenData(email, expireAt));
        return token;
    }

    private ResetTokenData validateToken(String token) {
        ResetTokenData data = resetTokens.get(token);

        if (data == null) {
            log.warn("PasswordReset | Reset failed | token={} | reason=INVALID_TOKEN", token);
            return null;
        }

        if (data.expireAt().isBefore(LocalDateTime.now())) {
            log.warn("PasswordReset | Reset failed | token={} | reason=TOKEN_EXPIRED", token);
            resetTokens.remove(token);
            return null;
        }

        return data;
    }

    private boolean isPasswordStrong(String password, String email) {
        try {
            passwordStrengthValidator.validate(password);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("PasswordReset | Reset failed | email={} | reason=WEAK_PASSWORD", email);
            return false;
        }
    }

    private void updateUserPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found during reset"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void clearResetData(String token, String email) {
        resetTokens.remove(token);
        resetAttempts.remove(email);
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.badRequest().body(message);
    }



    private record ResetTokenData(String email, LocalDateTime expireAt) {}
}
