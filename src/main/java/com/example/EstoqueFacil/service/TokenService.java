package com.example.EstoqueFacil.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.EstoqueFacil.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) { // Corrigido: generateToken (antes genareteToken)
        try {
            log.debug(
                    "JWT | Generating token | subject={} | expiresIn=2h",
                    user.getEmail()
            );

            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("login-auth-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);

            log.debug(
                    "JWT | Token generated successfully | subject={}",
                    user.getEmail()
            );

            return token;
        } catch (JWTCreationException exception) {
            log.error(
                    "JWT | Token generation failed | subject={}",
                    user.getEmail(),
                    exception
            );
            throw new RuntimeException("Error while authenticating");
        }
    }

    public String validateToken(String token) { // Corrigido: validateToken (antes validadeToken)
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

            log.debug(
                    "JWT | Token validated | subject={}",
                    subject
            );

            return subject;
        } catch (JWTVerificationException exception) {
            log.warn(
                    "JWT | Invalid token received | error={}",
                    exception.getMessage()
            );
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}