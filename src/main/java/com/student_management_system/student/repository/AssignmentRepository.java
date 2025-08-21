package com.student_management_system.student.repository;

import com.student_management_system.student.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // TODO: Add custom query methods if needed
}