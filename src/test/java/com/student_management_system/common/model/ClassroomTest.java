package com.student_management_system.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Classroom entity
 * Tests the new academic structure's classroom functionality
 */
class ClassroomTest {

    private Classroom classroom;
    private AcademicYear academicYear;
    private GradeLevel gradeLevel;

    @BeforeEach
    void setUp() {
        // Create test academic year
        academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setName("2024-2025");
        academicYear.setStartDate(LocalDate.of(2024, 9, 1));
        academicYear.setEndDate(LocalDate.of(2025, 8, 31));
        academicYear.setActive(true);

        // Create test grade level
        gradeLevel = new GradeLevel();
        gradeLevel.setId(1L);
        gradeLevel.setName("Grade 10");
        gradeLevel.setLevel(10);

        // Create test classroom
        classroom = new Classroom();
    }

    @Test
    void createClassroom_ValidData_ShouldSetAllProperties() {
        // ACT
        classroom.setName("A");
        classroom.setGradeLevel(gradeLevel);
        classroom.setAcademicYear(academicYear);
        classroom.setMedium(Medium.ENGLISH);
        classroom.setMaxStudents(35);

        // ASSERT
        assertThat(classroom.getName()).isEqualTo("A");
        assertThat(classroom.getGradeLevel()).isEqualTo(gradeLevel);
        assertThat(classroom.getAcademicYear()).isEqualTo(academicYear);
        assertThat(classroom.getMedium()).isEqualTo(Medium.ENGLISH);
        assertThat(classroom.getMaxStudents()).isEqualTo(35);
    }

    @Test
    void getFullName_AllPropertiesSet_ShouldReturnFormattedName() {
        // ARRANGE
        classroom.setName("A");
        classroom.setGradeLevel(gradeLevel);
        classroom.setMedium(Medium.ENGLISH);
        classroom.setAcademicYear(academicYear);

        // ACT
        String fullName = classroom.getFullName();

        // ASSERT
        assertThat(fullName).isEqualTo("Grade 10-A (ENGLISH Medium) - 2024-2025");
    }

    @Test
    void getFullName_TamilMedium_ShouldReturnCorrectFormat() {
        // ARRANGE
        classroom.setName("B");
        classroom.setGradeLevel(gradeLevel);
        classroom.setMedium(Medium.TAMIL);
        classroom.setAcademicYear(academicYear);

        // ACT
        String fullName = classroom.getFullName();

        // ASSERT
        assertThat(fullName).isEqualTo("Grade 10-B (TAMIL Medium) - 2024-2025");
    }

    @Test
    void getFullName_SinhalaMedium_ShouldReturnCorrectFormat() {
        // ARRANGE
        classroom.setName("C");
        classroom.setGradeLevel(gradeLevel);
        classroom.setMedium(Medium.SINHALA);
        classroom.setAcademicYear(academicYear);

        // ACT
        String fullName = classroom.getFullName();

        // ASSERT
        assertThat(fullName).isEqualTo("Grade 10-C (SINHALA Medium) - 2024-2025");
    }

    @Test
    void mediumEnum_AllValues_ShouldBeAvailable() {
        // ACT & ASSERT
        assertThat(Medium.ENGLISH).isNotNull();
        assertThat(Medium.TAMIL).isNotNull();
        assertThat(Medium.SINHALA).isNotNull();
        
        // Test enum values
        assertThat(Medium.values()).hasSize(3);
        assertThat(Medium.valueOf("ENGLISH")).isEqualTo(Medium.ENGLISH);
        assertThat(Medium.valueOf("TAMIL")).isEqualTo(Medium.TAMIL);
        assertThat(Medium.valueOf("SINHALA")).isEqualTo(Medium.SINHALA);
    }

    @Test
    void classroom_MaxStudentsValidation_ShouldAcceptValidValues() {
        // ACT
        classroom.setMaxStudents(30);
        classroom.setMaxStudents(40);
        classroom.setMaxStudents(1);

        // ASSERT
        assertThat(classroom.getMaxStudents()).isEqualTo(1);
        
        // Test reasonable classroom sizes
        classroom.setMaxStudents(35);
        assertThat(classroom.getMaxStudents()).isEqualTo(35);
    }

    @Test
    void classroom_Relationships_ShouldMaintainReferences() {
        // ARRANGE
        classroom.setName("A");
        classroom.setGradeLevel(gradeLevel);
        classroom.setAcademicYear(academicYear);
        classroom.setMedium(Medium.ENGLISH);

        // ACT & ASSERT - Test that relationships are maintained
        assertThat(classroom.getGradeLevel().getName()).isEqualTo("Grade 10");
        assertThat(classroom.getGradeLevel().getLevel()).isEqualTo(10);
        assertThat(classroom.getAcademicYear().getName()).isEqualTo("2024-2025");
        assertThat(classroom.getAcademicYear().isActive()).isTrue();
    }
}
