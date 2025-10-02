package com.student_management_system.student.repository;

import com.student_management_system.student.model.Subject;
import com.student_management_system.common.model.GradeLevel;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    /**
     * Find subjects by grade level
     */
    List<Subject> findByGradeLevelAndIsActiveTrue(GradeLevel gradeLevel);
    
    /**
     * Find subjects by grade level ID
     */
    List<Subject> findByGradeLevelIdAndIsActiveTrue(Long gradeLevelId);
    
    /**
     * Find subjects assigned to a specific teacher
     */
    @Query("SELECT s FROM Subject s JOIN s.teachers t WHERE t = :teacher AND s.isActive = true")
    List<Subject> findByTeachersContaining(@Param("teacher") User teacher);
    
    /**
     * Find subject by subject code and grade level (should be unique)
     */
    Optional<Subject> findBySubjectCodeAndGradeLevel(String subjectCode, GradeLevel gradeLevel);
    
    /**
     * Find subject by subject code
     */
    Optional<Subject> findBySubjectCode(String subjectCode);
    
    /**
     * Find all active subjects
     */
    List<Subject> findByIsActiveTrue();
    
    /**
     * Check if a teacher is assigned to a specific subject
     */
    @Query("SELECT COUNT(s) > 0 FROM Subject s JOIN s.teachers t WHERE s = :subject AND t = :teacher")
    boolean isTeacherAssignedToSubject(@Param("subject") Subject subject, @Param("teacher") User teacher);
}