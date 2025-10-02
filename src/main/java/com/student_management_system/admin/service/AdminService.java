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
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void createUser(CreateUserDto createUserDto) {
        if (userRepository.findByUsername(createUserDto.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setFirstName(createUserDto.getFirstName());
        user.setLastName(createUserDto.getLastName());
        user.setEmail(createUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setRole(createUserDto.getRole());
        user.setNic(createUserDto.getNic());
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
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setAddress(userDto.getAddress());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setNic(userDto.getNic());
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
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setDateOfBirth(user.getDateOfBirth());
        userDto.setAddress(user.getAddress());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setNic(user.getNic());
        userDto.setRole(user.getRole());
        userDto.setEnabled(user.isEnabled());
        return userDto;
    }
    
    // Validation helper methods
    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    public boolean isNicExists(String nic) {
        return userRepository.findByNic(nic).isPresent();
    }
    
    public boolean isUsernameExistsForOtherUser(String username, Long userId) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        return existingUser.isPresent() && !existingUser.get().getId().equals(userId);
    }
    
    public boolean isEmailExistsForOtherUser(String email, Long userId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        return existingUser.isPresent() && !existingUser.get().getId().equals(userId);
    }
    
    public boolean isNicExistsForOtherUser(String nic, Long userId) {
        Optional<User> existingUser = userRepository.findByNic(nic);
        return existingUser.isPresent() && !existingUser.get().getId().equals(userId);
    }
}