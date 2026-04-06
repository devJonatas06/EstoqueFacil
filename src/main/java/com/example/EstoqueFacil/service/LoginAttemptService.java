package com.example.EstoqueFacil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        attemptsCache.remove(email);
        log.info(
                "Security | Login attempts reset | email={}",
                email
        );
    }

    public void loginFailed(String email) {
        int attempts = attemptsCache.merge(email, 1, Integer::sum);

        if (attempts >= MAX_ATTEMPTS) {
            log.warn(
                    "Security | Account temporarily blocked | email={} | attempts={}",
                    email,
                    attempts
            );
        } else {
            log.debug(
                    "Security | Login failed attempt | email={} | attempt={}/{}",
                    email,
                    attempts,
                    MAX_ATTEMPTS
            );
        }
    }

    public boolean isBlocked(String email) {
        int attempts = attemptsCache.getOrDefault(email, 0);
        boolean blocked = attempts >= MAX_ATTEMPTS;

        if (blocked) {
            log.debug(
                    "Security | Account is blocked | email={} | attempts={}",
                    email,
                    attempts
            );
        }

        return blocked;
    }
}