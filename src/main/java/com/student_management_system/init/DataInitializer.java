package com.student_management_system.init;

import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only run this if the user table is empty to avoid creating duplicate users on
        // every restart
        if (userRepository.count() == 0) {
            System.out.println("No users found in DB, creating sample users...");

            // Create an Admin User
            User admin = new User();
            admin.setUsername("admin");
            // IMPORTANT: Always encode passwords before saving!
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@school.com");
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);

            // Create a Student User
            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setEmail("student@school.com");
            student.setRole(Role.ROLE_STUDENT);
            userRepository.save(student);

            // Create a Teacher User
            User teacher = new User();
            teacher.setUsername("teacher");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setEmail("teacher@school.com");
            teacher.setRole(Role.ROLE_TEACHER);
            userRepository.save(teacher);

            System.out.println("Sample users created successfully!");
        } else {
            System.out.println("Users already exist in the DB, skipping data initialization.");
        }
    }
}