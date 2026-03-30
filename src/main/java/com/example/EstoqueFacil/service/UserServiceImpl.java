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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO create(UserRequestDTO userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada"));

        user.setRoles(Set.of(role));
        user.setActive(true);

        User savedUser = userRepository.save(user);
        
        // ✅ Retorna DTO, não Entity!
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        // ✅ Retorna DTO, não Entity!
        return userMapper.toResponseDTO(user);
    }
}