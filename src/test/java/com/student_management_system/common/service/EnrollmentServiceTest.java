package com.student_management_system.common.service;

import com.student_management_system.common.model.*;
import com.student_management_system.common.repository.EnrollmentRepository;
import com.student_management_system.common.repository.ClassroomRepository;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnrollmentService
 * Tests core enrollment functionality with proper method signatures
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    
    @Mock
    private ClassroomRepository classroomRepository;
    
    @Mock
    private AcademicYearService academicYearService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User testStudent;
    private Classroom testClassroom;
    private AcademicYear testAcademicYear;

    @BeforeEach
    void setUp() {
        // Create test student
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("test_student");
        testStudent.setRole(Role.ROLE_STUDENT);

        // Create test academic year
        testAcademicYear = new AcademicYear();
        testAcademicYear.setId(1L);
        testAcademicYear.setName("2024-2025");
        testAcademicYear.setActive(true);

        // Create test classroom
        testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setName("A");
        testClassroom.setMaxStudents(35);
    }

    @Test
    void enrollStudent_ValidData_ShouldCreateActiveEnrollment() {
        // ARRANGE
        when(enrollmentRepository.findActiveEnrollmentByStudentAndAcademicYear(testStudent, testAcademicYear))
            .thenReturn(Optional.empty());
        when(classroomRepository.getCurrentEnrollmentCount(testClassroom)).thenReturn(20L);
        
        Enrollment expectedEnrollment = new Enrollment();
        expectedEnrollment.setStudent(testStudent);
        expectedEnrollment.setClassroom(testClassroom);
        expectedEnrollment.setAcademicYear(testAcademicYear);
        expectedEnrollment.setActive(true);
        
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(expectedEnrollment);

        // ACT
        Enrollment result = enrollmentService.enrollStudent(testStudent, testClassroom, testAcademicYear);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(testStudent);
        assertThat(result.getClassroom()).isEqualTo(testClassroom);
        assertThat(result.isActive()).isTrue();
        
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_StudentAlreadyEnrolled_ShouldThrowException() {
        // ARRANGE
        Enrollment existingEnrollment = new Enrollment();
        when(enrollmentRepository.findActiveEnrollmentByStudentAndAcademicYear(testStudent, testAcademicYear))
            .thenReturn(Optional.of(existingEnrollment));

        // ACT & ASSERT
        assertThatThrownBy(() -> 
            enrollmentService.enrollStudent(testStudent, testClassroom, testAcademicYear))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Student is already enrolled");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_ClassroomAtCapacity_ShouldThrowException() {
        // ARRANGE
        when(enrollmentRepository.findActiveEnrollmentByStudentAndAcademicYear(testStudent, testAcademicYear))
            .thenReturn(Optional.empty());
        when(classroomRepository.getCurrentEnrollmentCount(testClassroom)).thenReturn(35L); // At max capacity

        // ACT & ASSERT
        assertThatThrownBy(() -> 
            enrollmentService.enrollStudent(testStudent, testClassroom, testAcademicYear))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Classroom has reached maximum capacity");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void getStudentsInClassroom_ValidClassroom_ShouldReturnStudentList() {
        // ARRANGE
        User student2 = new User();
        student2.setId(2L);
        student2.setUsername("student2");
        
        List<User> expectedStudents = Arrays.asList(testStudent, student2);
        when(enrollmentRepository.findStudentsByClassroom(testClassroom)).thenReturn(expectedStudents);

        // ACT
        List<User> result = enrollmentService.getStudentsInClassroom(testClassroom);

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testStudent, student2);
    }

    @Test
    void getCurrentEnrollmentCount_ValidClassroom_ShouldReturnCount() {
        // ARRANGE
        when(enrollmentRepository.countActiveEnrollmentsByClassroom(testClassroom)).thenReturn(25L);

        // ACT
        Long result = enrollmentService.getCurrentEnrollmentCount(testClassroom);

        // ASSERT
        assertThat(result).isEqualTo(25L);
    }

    @Test
    void getAvailableSpots_ClassroomWithSpace_ShouldReturnCorrectCount() {
        // ARRANGE
        when(enrollmentRepository.countActiveEnrollmentsByClassroom(testClassroom)).thenReturn(25L);

        // ACT
        Integer availableSpots = enrollmentService.getAvailableSpots(testClassroom);

        // ASSERT
        assertThat(availableSpots).isEqualTo(10); // 35 max - 25 enrolled = 10 available
    }
}
