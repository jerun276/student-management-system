package com.student_management_system.user_management.repository;

import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);
}