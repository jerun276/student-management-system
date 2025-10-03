package com.student_management_system.admin.controller;

import com.student_management_system.common.model.GradeLevel;
import com.student_management_system.common.repository.GradeLevelRepository;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubjectController
 * Tests all CRUD operations and business logic for subject management
 */
@ExtendWith(MockitoExtension.class)
public class SubjectControllerTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GradeLevelRepository gradeLevelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SubjectController subjectController;

    private MockMvc mockMvc;

    private Subject testSubject;
    private GradeLevel testGradeLevel;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subjectController).build();

        // Create test data
        testGradeLevel = new GradeLevel();
        testGradeLevel.setId(1L);
        testGradeLevel.setName("Grade 10");
        testGradeLevel.setLevel(10);

        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("Mathematics");
        testSubject.setSubjectCode("MATH10");
        testSubject.setDescription("Advanced Mathematics for Grade 10");
        testSubject.setGradeLevel(testGradeLevel);
        testSubject.setActive(true);

        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@school.com");
        testTeacher.setRole(Role.ROLE_TEACHER);
    }

    @Test
    void testListSubjects_WithoutFilter() {
        // Arrange
        List<Subject> subjects = Arrays.asList(testSubject);
        List<GradeLevel> gradeLevels = Arrays.asList(testGradeLevel);

        when(subjectRepository.findByIsActiveTrue()).thenReturn(subjects);
        when(gradeLevelRepository.findAllByOrderByLevelAsc()).thenReturn(gradeLevels);

        // Act
        String viewName = subjectController.listSubjects(null, model);

        // Assert
        assertEquals("admin/subjects/list", viewName);
        verify(model).addAttribute("subjects", subjects);
        verify(model).addAttribute("gradeLevels", gradeLevels);
        verify(model, never()).addAttribute(eq("selectedGradeLevelId"), any());
    }

    @Test
    void testListSubjects_WithGradeLevelFilter() {
        // Arrange
        Long gradeLevelId = 1L;
        List<Subject> subjects = Arrays.asList(testSubject);
        List<GradeLevel> gradeLevels = Arrays.asList(testGradeLevel);

        when(subjectRepository.findByGradeLevelIdAndIsActiveTrue(gradeLevelId)).thenReturn(subjects);
        when(gradeLevelRepository.findAllByOrderByLevelAsc()).thenReturn(gradeLevels);

        // Act
        String viewName = subjectController.listSubjects(gradeLevelId, model);

        // Assert
        assertEquals("admin/subjects/list", viewName);
        verify(model).addAttribute("subjects", subjects);
        verify(model).addAttribute("gradeLevels", gradeLevels);
        verify(model).addAttribute("selectedGradeLevelId", gradeLevelId);
    }

    @Test
    void testShowCreateForm() {
        // Arrange
        List<GradeLevel> gradeLevels = Arrays.asList(testGradeLevel);
        List<User> teachers = Arrays.asList(testTeacher);

        when(gradeLevelRepository.findAllByOrderByLevelAsc()).thenReturn(gradeLevels);
        when(userRepository.findByRole(Role.ROLE_TEACHER)).thenReturn(teachers);

        // Act
        String viewName = subjectController.showCreateForm(model);

        // Assert
        assertEquals("admin/subjects/create", viewName);
        verify(model).addAttribute(eq("subject"), any(Subject.class));
        verify(model).addAttribute("gradeLevels", gradeLevels);
        verify(model).addAttribute("teachers", teachers);
    }

    @Test
    void testCreateSubject_Success() {
        // Arrange
        Long gradeLevelId = 1L;
        List<Long> teacherIds = Arrays.asList(1L);

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode(testSubject.getSubjectCode())).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, teacherIds, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository, times(2)).save(any(Subject.class)); // Called twice: initial save + teacher assignment save
        assertTrue(testSubject.isActive());
        assertEquals(testGradeLevel, testSubject.getGradeLevel());
    }

    @Test
    void testCreateSubject_InvalidGradeLevel() {
        // Arrange
        Long gradeLevelId = 999L;

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects/create", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Invalid grade level selected."));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void testCreateSubject_DuplicateSubjectCode() {
        // Arrange
        Long gradeLevelId = 1L;
        Subject existingSubject = new Subject();
        existingSubject.setSubjectCode("MATH10");

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode(testSubject.getSubjectCode())).thenReturn(Optional.of(existingSubject));

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects/create", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject code already exists. Please use a different code."));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void testShowEditForm_SubjectExists() {
        // Arrange
        Long subjectId = 1L;
        List<GradeLevel> gradeLevels = Arrays.asList(testGradeLevel);
        List<User> teachers = Arrays.asList(testTeacher);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(gradeLevelRepository.findAllByOrderByLevelAsc()).thenReturn(gradeLevels);
        when(userRepository.findByRole(Role.ROLE_TEACHER)).thenReturn(teachers);

        // Act
        String viewName = subjectController.showEditForm(subjectId, model, redirectAttributes);

        // Assert
        assertEquals("admin/subjects/edit", viewName);
        verify(model).addAttribute("subject", testSubject);
        verify(model).addAttribute("gradeLevels", gradeLevels);
        verify(model).addAttribute("teachers", teachers);
    }

    @Test
    void testShowEditForm_SubjectNotFound() {
        // Arrange
        Long subjectId = 999L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.showEditForm(subjectId, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject not found."));
    }

    @Test
    void testUpdateSubject_Success() {
        // Arrange
        Long subjectId = 1L;
        Long gradeLevelId = 1L;
        Subject updatedSubject = new Subject();
        updatedSubject.setName("Advanced Mathematics");
        updatedSubject.setSubjectCode("MATH10");
        updatedSubject.setDescription("Updated description");

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode("MATH10")).thenReturn(Optional.of(testSubject)); // Same subject
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        // Act
        String result = subjectController.updateSubject(subjectId, updatedSubject, gradeLevelId, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository).save(testSubject);
    }

    @Test
    void testUpdateSubject_SubjectNotFound() {
        // Arrange
        Long subjectId = 999L;
        Subject updatedSubject = new Subject();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.updateSubject(subjectId, updatedSubject, 1L, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject not found."));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void testViewSubject_SubjectExists() {
        // Arrange
        Long subjectId = 1L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        // Act
        String viewName = subjectController.viewSubject(subjectId, model, redirectAttributes);

        // Assert
        assertEquals("admin/subjects/view", viewName);
        verify(model).addAttribute("subject", testSubject);
    }

    @Test
    void testViewSubject_SubjectNotFound() {
        // Arrange
        Long subjectId = 999L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.viewSubject(subjectId, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject not found."));
    }

    @Test
    void testDeactivateSubject_Success() {
        // Arrange
        Long subjectId = 1L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        // Act
        String result = subjectController.deactivateSubject(subjectId, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository).save(testSubject);
        assertFalse(testSubject.isActive());
    }

    @Test
    void testDeactivateSubject_SubjectNotFound() {
        // Arrange
        Long subjectId = 999L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.deactivateSubject(subjectId, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject not found."));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void testActivateSubject_Success() {
        // Arrange
        Long subjectId = 1L;
        testSubject.setActive(false); // Start with inactive subject

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        // Act
        String result = subjectController.activateSubject(subjectId, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository).save(testSubject);
        assertTrue(testSubject.isActive());
    }

    @Test
    void testActivateSubject_SubjectNotFound() {
        // Arrange
        Long subjectId = 999L;

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.activateSubject(subjectId, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Subject not found."));
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void testCreateSubject_WithTeacherAssignment() {
        // Arrange
        Long gradeLevelId = 1L;
        List<Long> teacherIds = Arrays.asList(1L);

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode(testSubject.getSubjectCode())).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, teacherIds, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository, times(2)).save(any(Subject.class)); // Once for subject, once for teacher assignment
        assertTrue(testSubject.getTeachers().contains(testTeacher));
    }

    @Test
    void testCreateSubject_WithInvalidTeacher() {
        // Arrange
        Long gradeLevelId = 1L;
        List<Long> teacherIds = Arrays.asList(999L); // Non-existent teacher

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode(testSubject.getSubjectCode())).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, teacherIds, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        verify(subjectRepository, times(2)).save(any(Subject.class)); // Called twice: initial save + teacher assignment attempt
        assertTrue(testSubject.getTeachers().isEmpty());
    }

    @Test
    void testCreateSubject_ExceptionHandling() {
        // Arrange
        Long gradeLevelId = 1L;

        when(gradeLevelRepository.findById(gradeLevelId)).thenReturn(Optional.of(testGradeLevel));
        when(subjectRepository.findBySubjectCode(testSubject.getSubjectCode())).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        String result = subjectController.createSubject(testSubject, gradeLevelId, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/subjects/create", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Error creating subject"));
    }
}
