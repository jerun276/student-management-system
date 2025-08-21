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
}