package com.student_management_system.user_management.repository;

import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.children WHERE u.username = :username")
    Optional<User> findByUsernameWithChildren(@Param("username") String username);
    
    // Search methods for SearchService
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    
    // Find top users for suggestions
    List<User> findTop5ByUsernameContainingIgnoreCaseOrderByUsername(String username);
    
    // Pagination methods for AdminService
    Page<User> findByRole(Role role, Pageable pageable);
    
    // Search users with pagination
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%) AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, @Param("role") Role role, Pageable pageable);
}