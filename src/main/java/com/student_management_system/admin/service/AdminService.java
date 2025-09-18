package com.student_management_system.admin.service;

import com.student_management_system.admin.dto.CreateUserDto;
import com.student_management_system.admin.dto.UserDto;
import com.student_management_system.admin.dto.UserManagementDto;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REQUIRED METHODS FOR AdminUserController:

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String searchTerm, Role roleFilter, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, roleFilter, pageable);
    }

    public Page<User> getUsersByRole(Role roleFilter, Pageable pageable) {
        return userRepository.findByRole(roleFilter, pageable);
    }

    public List<User> getPotentialParents() {
        return userRepository.findByRole(Role.ROLE_PARENT);
    }

    @Transactional
    public User createUser(UserManagementDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        user.setEnabled(userDto.isEnabled());
        
        if (userDto.getParentId() != null) {
            User parent = userRepository.findById(userDto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));
            user.setParent(parent);
        }
        
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User updateUser(Long id, UserManagementDto userDto) {
        User user = getUserById(id);
        
        // Check username uniqueness
        if (!user.getUsername().equals(userDto.getUsername())) {
            if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
                throw new IllegalStateException("Username already exists");
            }
        }
        
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setEnabled(userDto.isEnabled());
        
        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        // Update parent relationship
        if (userDto.getParentId() != null) {
            User parent = userRepository.findById(userDto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));
            user.setParent(parent);
        } else {
            user.setParent(null);
        }
        
        return userRepository.save(user);
    }

    public UserManagementDto convertToDto(User user) {
        UserManagementDto dto = new UserManagementDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        
        if (user.getParent() != null) {
            dto.setParentId(user.getParent().getId());
            dto.setParentName(user.getParent().getUsername());
        }
        
        return dto;
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void changeUserRole(Long id, Role newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        // Check if user has children (for parent users)
        if (user.getChildren() != null && !user.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete user with dependent children");
        }
        userRepository.delete(user);
    }

    @Transactional
    public String resetUserPassword(Long id) {
        User user = getUserById(id);
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}