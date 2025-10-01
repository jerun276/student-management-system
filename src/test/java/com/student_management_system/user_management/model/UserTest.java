package com.student_management_system.user_management.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

/**
 * Unit tests for User entity
 * This demonstrates basic entity testing
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Create a fresh user for each test
        user = new User();
    }

    @Test
    void createUser_ValidData_ShouldSetAllProperties() {
        // ARRANGE & ACT
        user.setUsername("test_user");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@school.com");
        user.setRole(Role.ROLE_STUDENT);
        user.setDateOfBirth(LocalDate.of(2005, 5, 15));
        user.setAddress("123 Test Street");
        user.setPhoneNumber("555-0123");
        user.setNic("051234567V");
        user.setEnabled(true);

        // ASSERT
        assertThat(user.getUsername()).isEqualTo("test_user");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@school.com");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_STUDENT);
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(2005, 5, 15));
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void createUser_DefaultValues_ShouldHaveCorrectDefaults() {
        // ACT - Create user with minimal data
        user.setUsername("test_user");
        user.setRole(Role.ROLE_STUDENT);

        // ASSERT - Check default values
        assertThat(user.isEnabled()).isTrue(); // Default should be true
        assertThat(user.getId()).isNull(); // Not persisted yet
    }

    @Test
    void setParentChildRelationship_ValidUsers_ShouldEstablishRelationship() {
        // ARRANGE
        User parent = new User();
        parent.setUsername("parent_user");
        parent.setRole(Role.ROLE_PARENT);

        User child = new User();
        child.setUsername("child_user");
        child.setRole(Role.ROLE_STUDENT);

        // ACT
        child.setParent(parent);

        // ASSERT
        assertThat(child.getParent()).isEqualTo(parent);
    }

    @Test
    void createTeacherUser_ValidData_ShouldCreateTeacherCorrectly() {
        // ACT - Create a teacher user with all required fields
        User teacher = new User();
        teacher.setUsername("teacher_user");
        teacher.setFirstName("Jane");
        teacher.setLastName("Smith");
        teacher.setEmail("jane.smith@school.com");
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setDateOfBirth(LocalDate.of(1985, 3, 20));
        teacher.setAddress("456 Teacher Ave");
        teacher.setPhoneNumber("555-0456");
        teacher.setNic("851234567V");
        teacher.setEnabled(true);

        // ASSERT
        assertThat(teacher.getUsername()).isEqualTo("teacher_user");
        assertThat(teacher.getFirstName()).isEqualTo("Jane");
        assertThat(teacher.getLastName()).isEqualTo("Smith");
        assertThat(teacher.getRole()).isEqualTo(Role.ROLE_TEACHER);
        assertThat(teacher.getEmail()).isEqualTo("jane.smith@school.com");
        assertThat(teacher.isEnabled()).isTrue();
    }
}
