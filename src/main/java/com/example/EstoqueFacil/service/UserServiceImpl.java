package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;
import com.example.EstoqueFacil.entity.Role;
import com.example.EstoqueFacil.entity.User;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.UserMapper;
import com.example.EstoqueFacil.repository.RoleRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO create(UserRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            log.warn("Usuário - Tentativa de criar com email duplicado: {}", requestDTO.getEmail());
            throw new BusinessException("Email já cadastrado: " + requestDTO.getEmail());
        }

        User user = userMapper.toEntity(requestDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> {
                    log.error("Usuário - Role ROLE_EMPLOYEE não encontrada no sistema");
                    return new ResourceNotFoundException("Role ROLE_EMPLOYEE não encontrada");
                });

        user.setRoles(Set.of(role));
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("Usuário - Criado com sucesso. ID: {}, Email: {}, Role: {}", saved.getId(), saved.getEmail(), role.getName());
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuário - Não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
                });
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> {
                    log.warn("Usuário - Não encontrado por email: {}", email);
                    return new ResourceNotFoundException("Usuário não encontrado com email: " + email);
                });
        return userMapper.toResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        List<UserResponseDTO> users = userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());

        long adminCount = users.stream().filter(u -> u.getRoles().contains("ROLE_ADMIN")).count();
        long employeeCount = users.stream().filter(u -> u.getRoles().contains("ROLE_EMPLOYEE")).count();

        log.info("Usuário - Listagem concluída. Total: {}, ADMIN: {}, EMPLOYEE: {}", users.size(), adminCount, employeeCount);
        return users;
    }

    @Override
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuário - Tentativa de desativar usuário inexistente. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
                });
        user.setActive(false);
        userRepository.save(user);
        log.info("Usuário - Desativado com sucesso. ID: {}, Email: {}", id, user.getEmail());
    }

    @Override
    public UserResponseDTO changeRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuário - Tentativa de alterar role de usuário inexistente. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
                });

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Usuário - Role não encontrada: {}", roleName);
                    return new ResourceNotFoundException("Role não encontrada: " + roleName);
                });

        String oldRole = user.getRoles().iterator().next().getName();
        user.setRoles(Set.of(newRole));
        User updated = userRepository.save(user);

        log.info("Usuário - Role alterada. ID: {}, Email: {}, Role antiga: {}, Nova role: {}",
                id, user.getEmail(), oldRole, roleName);
        return userMapper.toResponseDTO(updated);
    }
}