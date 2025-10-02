package com.student_management_system.user_management.repository;

import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByNic(String nic);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.children WHERE u.username = :username")
    Optional<User> findByUsernameWithChildren(@Param("username") String username);

    long countByRole(Role role);
}