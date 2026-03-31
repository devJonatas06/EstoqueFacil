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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            throw new BusinessException("Email já cadastrado: " + requestDTO.getEmail());
        }

        User user = userMapper.toEntity(requestDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_EMPLOYEE não encontrada"));

        user.setRoles(Set.of(role));
        user.setActive(true);

        User saved = userRepository.save(user);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com email: " + email));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO changeRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada: " + roleName));

        user.setRoles(Set.of(newRole));
        User updated = userRepository.save(user);
        return userMapper.toResponseDTO(updated);
    }
}