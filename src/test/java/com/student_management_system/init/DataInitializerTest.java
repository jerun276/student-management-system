package com.student_management_system.init;

import com.student_management_system.common.model.*;
import com.student_management_system.common.repository.*;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.service.EnrollmentService;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.staff.repository.FeeRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataInitializer
 * This demonstrates how to test initialization logic with mocks
 */
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    // Mock all dependencies
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private TimetableEntryRepository timetableEntryRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private FeeRepository feeRepository;
    @Mock
    private AcademicYearRepository academicYearRepository;
    @Mock
    private GradeLevelRepository gradeLevelRepository;
    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private AcademicYearService academicYearService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private com.student_management_system.common.service.TimeSlotService timeSlotService;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        // Create the DataInitializer with all mocked dependencies
        dataInitializer = new DataInitializer(
                userRepository, passwordEncoder, subjectRepository,
                timetableEntryRepository, assignmentRepository, feeRepository,
                academicYearRepository, gradeLevelRepository, classroomRepository,
                academicYearService, enrollmentService, timeSlotService);
    }

    @Test
    void run_EmptyDatabase_ShouldInitializeUsersAndAcademicStructure() {
        // ARRANGE - Set up the test conditions
        when(userRepository.count()).thenReturn(0L); // Empty database
        when(academicYearRepository.count()).thenReturn(0L); // No academic years
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // ACT - Execute the method under test
        dataInitializer.run();

        // ASSERT - Verify the expected behavior
        // Verify that users were created (admin, teachers, students, etc.)
        verify(userRepository, atLeast(8)).save(any(User.class)); // At least 8 users created

        // Verify password encoding was called
        verify(passwordEncoder, atLeast(8)).encode(anyString());

        // Verify academic structure creation was attempted
        verify(academicYearRepository, atLeast(1)).save(any(AcademicYear.class));
        verify(gradeLevelRepository, atLeast(1)).save(any(GradeLevel.class));
    }

    @Test
    void run_DatabaseAlreadyPopulated_ShouldSkipInitialization() {
        // ARRANGE - Database already has data
        when(userRepository.count()).thenReturn(10L); // Users exist

        // ACT
        dataInitializer.run();

        // ASSERT - Verify no initialization happened
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(academicYearRepository, never()).save(any(AcademicYear.class));
    }

    @Test
    void run_UsersExistButNoAcademicStructure_ShouldOnlyCreateAcademicStructure() {
        // ARRANGE
        when(userRepository.count()).thenReturn(10L); // Users exist
        when(academicYearRepository.count()).thenReturn(0L); // No academic structure

        // ACT
        dataInitializer.run();

        // ASSERT
        verify(userRepository, never()).save(any(User.class)); // No users created
        verify(academicYearRepository, atLeast(1)).save(any(AcademicYear.class)); // Academic structure created
    }
}
