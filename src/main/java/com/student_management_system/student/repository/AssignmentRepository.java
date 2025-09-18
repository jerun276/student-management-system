package com.student_management_system.student.repository;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Find all assignments for a user, ordered by the due date
    List<Assignment> findByUserOrderByDueDateAsc(User user);

    List<Assignment> findByStatusOrderBySubmissionDateDesc(AssignmentStatus status);

    List<Assignment> findByStatus(AssignmentStatus status);
    
    // Find assignments created by a specific teacher
    List<Assignment> findByTeacherOrderByDueDateDesc(User teacher);
    
    // Find assignments by ID and status
    List<Assignment> findByIdAndStatus(Long id, AssignmentStatus status);
    
    // Find assignments by teacher and status
    List<Assignment> findByTeacherAndStatus(User teacher, AssignmentStatus status);
    
    // Search methods for SearchService
    List<Assignment> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    
    // Find top assignments for suggestions
    List<Assignment> findTop5ByTitleContainingIgnoreCaseOrderByTitle(String title);
}