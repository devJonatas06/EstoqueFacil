package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.user.UserPrincipal;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Security | Loading user details | email={}", email);

        User user = repository.findByEmailWithRoles(email)
                .orElseThrow(() -> {
                    log.warn("Security | UserDetails not found | email={}", email);
                    return new UsernameNotFoundException("User Not Found: " + email);
                });

        log.debug("Security | User details loaded | email={} | userId={} | roles={}",
                email, user.getId(), user.getRoles());
        return UserPrincipal.from(user)
    }
}