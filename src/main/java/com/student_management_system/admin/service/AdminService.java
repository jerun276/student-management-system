package com.student_management_system.admin.service;

import com.student_management_system.admin.dto.CreateUserDto;
import com.student_management_system.admin.dto.UserDto;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createUser(CreateUserDto createUserDto) {
        if (userRepository.findByUsername(createUserDto.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setRole(createUserDto.getRole());
        user.setEnabled(true);
        userRepository.save(user);
    }

    public UserDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToUserDto(user);
    }

    @Transactional
    public void updateUser(UserDto userDto) { // Now takes the single DTO
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userDto.getId()));

        Optional<User> existingUserWithSameUsername = userRepository.findByUsername(userDto.getUsername());
        if (existingUserWithSameUsername.isPresent() && !existingUserWithSameUsername.get().getId().equals(user.getId())) {
            throw new IllegalStateException("Username '" + userDto.getUsername() + "' is already taken.");
        }

        user.setUsername(userDto.getUsername()); // <-- ALLOW USERNAME UPDATE
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setEnabled(userDto.isEnabled());

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Encode the new password before saving
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }


    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setEnabled(user.isEnabled());
        return userDto;
    }
}