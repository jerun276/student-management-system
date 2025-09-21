package com.student_management_system.user_management.service;

import com.student_management_system.user_management.dto.EditProfileDto;
import com.student_management_system.user_management.dto.UserProfileDto;
import com.student_management_system.user_management.dto.PasswordChangeDto;
import com.student_management_system.user_management.dto.UserRegistrationDto;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());

        return dto;
    }

    public EditProfileDto getEditProfileDto(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EditProfileDto dto = new EditProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());

        return dto;
    }

    public void updateUserProfile(String username, EditProfileDto editProfileDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(editProfileDto.getFirstName());
        user.setLastName(editProfileDto.getLastName());
        user.setEmail(editProfileDto.getEmail());
        user.setDateOfBirth(editProfileDto.getDateOfBirth());
        user.setAddress(editProfileDto.getAddress());
        user.setPhoneNumber(editProfileDto.getPhoneNumber());

        userRepository.save(user);
    }

    public void updateUserPassword(String username, PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Check if the current password is correct
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Incorrect current password");
        }

        // 2. Check if the new passwords match
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmNewPassword())) {
            throw new IllegalStateException("New passwords do not match");
        }

        // 3. Encode and set the new password
        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));

        userRepository.save(user);
    }

    public void registerNewStudent(UserRegistrationDto registrationDto) {
        // Check if username already exists
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists.");
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setFirstName(registrationDto.getFirstName());
        newUser.setLastName(registrationDto.getLastName());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setDateOfBirth(registrationDto.getDateOfBirth());
        newUser.setAddress(registrationDto.getAddress());
        newUser.setPhoneNumber(registrationDto.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setRole(Role.ROLE_STUDENT);
        newUser.setEnabled(true);

        userRepository.save(newUser);
    }
}