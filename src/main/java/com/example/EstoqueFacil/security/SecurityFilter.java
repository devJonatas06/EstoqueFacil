package com.example.EstoqueFacil.security;

import com.example.EstoqueFacil.dto.user.UserPrincipal;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.UserRepository;
import com.example.EstoqueFacil.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Endpoints públicos
        if (path.startsWith("/auth") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = recoverToken(request);

        if (token != null) {
            String email = tokenService.validateToken(token);

            if (email != null) {
                // ✅ USAR findByEmailWithRoles (para carregar as roles)
                User user = userRepository.findByEmailWithRoles(email)
                        .orElseThrow(() -> new RuntimeException("User Not Found"));

                log.info("Usuário autenticado: {} | Roles: {}",
                        email,
                        user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));

                UserPrincipal userPrincipal = UserPrincipal.from(user);

                // ✅ PEGAR AS AUTHORITIES DAS ROLES DO USUÁRIO
                var authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList());

                var authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}