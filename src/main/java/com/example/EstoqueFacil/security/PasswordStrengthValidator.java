package com.example.EstoqueFacil.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PasswordStrengthValidator {
    private final Set<String> weakPasswords = new HashSet<>();

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/password-blacklist.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            this.weakPasswords.addAll(
                    br.lines()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet())
            );
        } catch (Exception e) {
            System.err.println("Error loading blacklist: " + e.getMessage());
        }
    }

    public void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        String lower = password.toLowerCase();
        if (weakPasswords.contains(lower)) {
            throw new IllegalArgumentException("This password is too common. Please choose a more unique password.");
        }

        if (isSimpleVariation(lower)) {
            throw new IllegalArgumentException("This password is a variation of common passwords. Try something more original.");
        }
    }

    private boolean isSimpleVariation(String lowerPass) {
        for (String weak : weakPasswords) {
            if (lowerPass.startsWith(weak) && lowerPass.length() > weak.length()) {
                String suffix = lowerPass.substring(weak.length());
                if (suffix.matches("\\d+") || suffix.matches("^[!@#$%^&*()]+$")) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getStrengthFeedback(String password) {
        if (password == null) return "Enter a password";
        int length = password.length();
        if (length < 8) return "Too short";
        if (length >= 20) return "Excellent - long password";
        if (length >= 12) return "Good - consider making it even longer";
        return "OK - longer passwords are more secure";
    }
}