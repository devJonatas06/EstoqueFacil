package com.example.EstoqueFacil.dto.user;

import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Schema(description = "Detalhes do usuário para autenticação Spring Security")
public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    @Schema(description = "Objeto User completo")
    public User getUser() {
        return user;
    }

    @Schema(description = "ID do usuário", example = "1")
    public Long getId() {
        return user.getId();
    }

    @Schema(description = "Email do usuário", example = "admin@estoque.com")
    public String getEmail() {
        return user.getEmail();
    }

    @Schema(description = "Nome do usuário", example = "Administrador")
    public String getName() {
        return user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}