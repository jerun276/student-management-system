package com.student_management_system.common.repository;

import com.student_management_system.common.model.*;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ClassroomRepository
 * Tests basic repository functionality with H2 database
 */
@DataJpaTest
class ClassroomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClassroomRepository classroomRepository;

    private AcademicYear testAcademicYear;
    private GradeLevel testGradeLevel;

    @BeforeEach
    void setUp() {
        // Create and persist academic year
        testAcademicYear = new AcademicYear();
        testAcademicYear.setName("2024-2025");
        testAcademicYear.setStartDate(LocalDate.of(2024, 9, 1));
        testAcademicYear.setEndDate(LocalDate.of(2025, 8, 31));
        testAcademicYear.setActive(true);
        testAcademicYear = entityManager.persistAndFlush(testAcademicYear);

        // Create and persist grade level
        testGradeLevel = new GradeLevel();
        testGradeLevel.setName("Grade 10");
        testGradeLevel.setLevel(10);
        testGradeLevel = entityManager.persistAndFlush(testGradeLevel);
    }

    @Test
    void save_NewClassroom_ShouldPersistClassroom() {
        // ARRANGE
        Classroom classroom = createTestClassroom("A", Medium.ENGLISH);

        // ACT
        Classroom saved = classroomRepository.save(classroom);

        // ASSERT
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("A");
        assertThat(saved.getMedium()).isEqualTo(Medium.ENGLISH);
        
        // Verify it's actually in the database
        Classroom found = entityManager.find(Classroom.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("A");
    }

    @Test
    void findByAcademicYear_ExistingYear_ShouldReturnClassrooms() {
        // ARRANGE
        Classroom classroom1 = createTestClassroom("A", Medium.ENGLISH);
        Classroom classroom2 = createTestClassroom("B", Medium.TAMIL);
        
        entityManager.persistAndFlush(classroom1);
        entityManager.persistAndFlush(classroom2);

        // ACT
        List<Classroom> result = classroomRepository.findByAcademicYear(testAcademicYear);

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Classroom::getName).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void findByGradeLevel_ExistingGrade_ShouldReturnClassrooms() {
        // ARRANGE
        Classroom classroom1 = createTestClassroom("A", Medium.ENGLISH);
        Classroom classroom2 = createTestClassroom("B", Medium.ENGLISH);
        
        entityManager.persistAndFlush(classroom1);
        entityManager.persistAndFlush(classroom2);

        // ACT
        List<Classroom> result = classroomRepository.findByGradeLevel(testGradeLevel);

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getGradeLevel().equals(testGradeLevel));
    }

    @Test
    void findByMedium_EnglishMedium_ShouldReturnEnglishClassrooms() {
        // ARRANGE
        Classroom englishClass = createTestClassroom("A", Medium.ENGLISH);
        Classroom tamilClass = createTestClassroom("B", Medium.TAMIL);
        
        entityManager.persistAndFlush(englishClass);
        entityManager.persistAndFlush(tamilClass);

        // ACT
        List<Classroom> result = classroomRepository.findByMedium(Medium.ENGLISH);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMedium()).isEqualTo(Medium.ENGLISH);
        assertThat(result.get(0).getName()).isEqualTo("A");
    }

    @Test
    void findByClassTeacher_ValidTeacher_ShouldReturnClassroom() {
        // ARRANGE
        User teacher = createTestTeacher();
        teacher = entityManager.persistAndFlush(teacher);
        
        Classroom classroom = createTestClassroom("A", Medium.ENGLISH);
        classroom.setClassTeacher(teacher);
        entityManager.persistAndFlush(classroom);

        // ACT
        Optional<Classroom> result = classroomRepository.findByClassTeacher(teacher);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getClassTeacher()).isEqualTo(teacher);
    }

    @Test
    void findAll_MultipleClassrooms_ShouldReturnAllClassrooms() {
        // ARRANGE
        Classroom classroom1 = createTestClassroom("A", Medium.ENGLISH);
        Classroom classroom2 = createTestClassroom("B", Medium.TAMIL);
        Classroom classroom3 = createTestClassroom("C", Medium.SINHALA);
        
        entityManager.persistAndFlush(classroom1);
        entityManager.persistAndFlush(classroom2);
        entityManager.persistAndFlush(classroom3);

        // ACT
        List<Classroom> result = classroomRepository.findAll();

        // ASSERT
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Classroom::getName)
            .containsExactlyInAnyOrder("A", "B", "C");
        assertThat(result).extracting(Classroom::getMedium)
            .containsExactlyInAnyOrder(Medium.ENGLISH, Medium.TAMIL, Medium.SINHALA);
    }

    // Helper method to create test classrooms
    private Classroom createTestClassroom(String name, Medium medium) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setGradeLevel(testGradeLevel);
        classroom.setAcademicYear(testAcademicYear);
        classroom.setMedium(medium);
        classroom.setMaxStudents(35);
        return classroom;
    }
    
    // Helper method to create test teacher
    private User createTestTeacher() {
        User teacher = new User();
        teacher.setUsername("test_teacher");
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@school.com");
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setDateOfBirth(LocalDate.of(1985, 5, 15));
        teacher.setAddress("123 Teacher St");
        teacher.setPhoneNumber("555-0123");
        teacher.setNic("851234567V");
        teacher.setPassword("encoded_password");
        teacher.setEnabled(true);
        return teacher;
    }
}
