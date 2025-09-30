package com.student_management_system.common.repository;

import com.student_management_system.common.model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    
    /**
     * Find the currently active academic year
     */
    Optional<AcademicYear> findByIsActiveTrue();
    
    /**
     * Find academic year by name
     */
    Optional<AcademicYear> findByName(String name);
    
    /**
     * Check if an academic year with the given name exists
     */
    boolean existsByName(String name);
    
    /**
     * Get all academic years ordered by start date descending (most recent first)
     */
    @Query("SELECT ay FROM AcademicYear ay ORDER BY ay.startDate DESC")
    java.util.List<AcademicYear> findAllOrderByStartDateDesc();
}
