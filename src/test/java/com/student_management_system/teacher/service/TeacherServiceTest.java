package com.student_management_system.teacher.service;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.teacher.repository.AttendanceRecordRepository;
import com.student_management_system.teacher.repository.StudyMaterialRepository;
import com.student_management_system.common.service.FileStorageService;
import com.student_management_system.common.repository.BudgetRequestRepository;
import com.student_management_system.common.repository.CourseRepository;
import com.student_management_system.common.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherService
 * Tests basic functionality with all required dependencies mocked
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    
    @Mock
    private SubjectRepository subjectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Mock
    private StudyMaterialRepository studyMaterialRepository;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @Mock
    private BudgetRequestRepository budgetRequestRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    void getAssignmentsToGrade_WithSubmittedAssignments_ShouldReturnSubmittedAssignments() {
        // ARRANGE
        Assignment assignment1 = new Assignment();
        assignment1.setId(1L);
        assignment1.setStatus(AssignmentStatus.SUBMITTED);
        
        Assignment assignment2 = new Assignment();
        assignment2.setId(2L);
        assignment2.setStatus(AssignmentStatus.SUBMITTED);

        List<Assignment> submittedAssignments = Arrays.asList(assignment1, assignment2);

        when(assignmentRepository.findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED))
            .thenReturn(submittedAssignments);

        // ACT
        List<Assignment> result = teacherService.getAssignmentsToGrade();

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(assignment1, assignment2);
        assertThat(result).allMatch(a -> a.getStatus() == AssignmentStatus.SUBMITTED);
        
        verify(assignmentRepository).findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED);
    }

    @Test
    void getAssignmentsToGrade_NoSubmittedAssignments_ShouldReturnEmptyList() {
        // ARRANGE
        when(assignmentRepository.findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED))
            .thenReturn(Arrays.asList());

        // ACT
        List<Assignment> result = teacherService.getAssignmentsToGrade();

        // ASSERT
        assertThat(result).isEmpty();
        verify(assignmentRepository).findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED);
    }
}
