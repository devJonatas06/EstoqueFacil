package com.example.EstoqueFacil.config;

import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.RoleRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {
        // Criar roles se não existirem
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_EMPLOYEE")));

        log.info("✅ Roles verificadas/criadas");

        // Pegar credenciais do .env
        String adminEmail = env.getRequiredProperty("ADMIN_EMAIL");
        String adminPassword = env.getRequiredProperty("ADMIN_PASSWORD");
        String adminName = env.getRequiredProperty("ADMIN_NAME");

        // Buscar ou criar admin
        User admin = userRepository.findByEmail(adminEmail).orElse(null);

        if (admin == null) {
            // Criar novo admin
            admin = new User();
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setActive(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            log.info("✅ Admin criado! Email: {}", adminEmail);
        } else {
            // Admin já existe, verificar se tem a role correta
            if (admin.getRoles() == null || !admin.getRoles().contains(adminRole)) {
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
                log.info("✅ Role ROLE_ADMIN associada ao admin existente! Email: {}", adminEmail);
            } else {
                log.info("✅ Admin já existe e já tem a role ROLE_ADMIN. Email: {}", adminEmail);
            }
        }
    }
}