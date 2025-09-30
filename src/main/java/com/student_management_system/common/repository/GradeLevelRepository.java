package com.student_management_system.common.repository;

import com.student_management_system.common.model.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeLevelRepository extends JpaRepository<GradeLevel, Long> {
    
    /**
     * Find grade level by name
     */
    Optional<GradeLevel> findByName(String name);
    
    /**
     * Find grade level by level number
     */
    Optional<GradeLevel> findByLevel(Integer level);
    
    /**
     * Check if a grade level with the given name exists
     */
    boolean existsByName(String name);
    
    /**
     * Get all grade levels ordered by level
     */
    @Query("SELECT gl FROM GradeLevel gl ORDER BY gl.level ASC")
    List<GradeLevel> findAllOrderByLevel();
    
    /**
     * Find grade levels within a range
     */
    @Query("SELECT gl FROM GradeLevel gl WHERE gl.level BETWEEN :minLevel AND :maxLevel ORDER BY gl.level")
    List<GradeLevel> findByLevelBetween(Integer minLevel, Integer maxLevel);
}
