package com.student_management_system.staff.repository;

import com.student_management_system.staff.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    // We can add custom queries later, e.g., find by status
}