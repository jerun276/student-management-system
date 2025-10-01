package com.student_management_system.user_management.repository;

import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserRepository
 * Uses @DataJpaTest for testing JPA repositories with in-memory database
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ExistingUser_ShouldReturnUser() {
        // ARRANGE - Create and save a test user
        User user = createTestUser("test_student", Role.ROLE_STUDENT);
        entityManager.persistAndFlush(user);

        // ACT
        Optional<User> found = userRepository.findByUsername("test_student");

        // ASSERT
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("test_student");
        assertThat(found.get().getRole()).isEqualTo(Role.ROLE_STUDENT);
    }

    @Test
    void findByUsername_NonExistentUser_ShouldReturnEmpty() {
        // ACT
        Optional<User> found = userRepository.findByUsername("non_existent");

        // ASSERT
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsernameWithChildren_ExistingUser_ShouldReturnUserWithChildren() {
        // ARRANGE
        User user = createTestUser("test_user", Role.ROLE_TEACHER);
        entityManager.persistAndFlush(user);

        // ACT
        Optional<User> found = userRepository.findByUsernameWithChildren("test_user");

        // ASSERT
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("test_user");
    }

    @Test
    void findByRole_ExistingRole_ShouldReturnUsersWithRole() {
        // ARRANGE - Create users with different roles
        User student1 = createTestUser("student1", Role.ROLE_STUDENT);
        User student2 = createTestUser("student2", Role.ROLE_STUDENT);
        User teacher = createTestUser("teacher1", Role.ROLE_TEACHER);
        
        entityManager.persistAndFlush(student1);
        entityManager.persistAndFlush(student2);
        entityManager.persistAndFlush(teacher);

        // ACT
        List<User> students = userRepository.findByRole(Role.ROLE_STUDENT);
        List<User> teachers = userRepository.findByRole(Role.ROLE_TEACHER);

        // ASSERT
        assertThat(students).hasSize(2);
        assertThat(teachers).hasSize(1);
        assertThat(students).extracting(User::getRole).containsOnly(Role.ROLE_STUDENT);
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        // ARRANGE
        User user = createTestUser("existing_user", Role.ROLE_ADMIN);
        User savedUser = entityManager.persistAndFlush(user);

        // ACT
        Optional<User> found = userRepository.findById(savedUser.getId());

        // ASSERT
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("existing_user");
        assertThat(found.get().getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void findById_NonExistentUser_ShouldReturnEmpty() {
        // ACT
        Optional<User> found = userRepository.findById(999L);

        // ASSERT
        assertThat(found).isEmpty();
    }

    @Test
    void save_NewUser_ShouldPersistUser() {
        // ARRANGE
        User newUser = createTestUser("new_user", Role.ROLE_STAFF);

        // ACT
        User saved = userRepository.save(newUser);

        // ASSERT
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("new_user");
        
        // Verify it's actually in the database
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
    }

    // Helper method to create test users
    private User createTestUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(username + "@school.com");
        user.setRole(role);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setAddress("123 Test St");
        user.setPhoneNumber("555-0123");
        user.setNic("901234567V");
        user.setPassword("encoded_password");
        user.setEnabled(true);
        return user;
    }
}
