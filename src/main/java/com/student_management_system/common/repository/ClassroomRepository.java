package com.student_management_system.common.repository;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.model.GradeLevel;
import com.student_management_system.common.model.Medium;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    /**
     * Find classrooms by academic year
     */
    List<Classroom> findByAcademicYear(AcademicYear academicYear);
    
    /**
     * Find classrooms by grade level
     */
    List<Classroom> findByGradeLevel(GradeLevel gradeLevel);
    
    /**
     * Find classrooms by grade level and academic year
     */
    List<Classroom> findByGradeLevelAndAcademicYear(GradeLevel gradeLevel, AcademicYear academicYear);
    
    /**
     * Find classrooms by medium
     */
    List<Classroom> findByMedium(Medium medium);
    
    /**
     * Find classroom by class teacher
     */
    Optional<Classroom> findByClassTeacher(User classTeacher);
    
    /**
     * Find classrooms by grade level and medium for a specific academic year
     */
    @Query("SELECT c FROM Classroom c WHERE c.gradeLevel = :gradeLevel AND c.medium = :medium AND c.academicYear = :academicYear")
    List<Classroom> findByGradeLevelAndMediumAndAcademicYear(
        @Param("gradeLevel") GradeLevel gradeLevel, 
        @Param("medium") Medium medium, 
        @Param("academicYear") AcademicYear academicYear
    );
    
    /**
     * Get current enrollment count for a classroom
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.classroom = :classroom AND e.isActive = true")
    Long getCurrentEnrollmentCount(@Param("classroom") Classroom classroom);
    
    /**
     * Find available classrooms (not at max capacity) for a grade level and academic year
     */
    @Query("SELECT c FROM Classroom c WHERE c.gradeLevel = :gradeLevel AND c.academicYear = :academicYear " +
           "AND (SELECT COUNT(e) FROM Enrollment e WHERE e.classroom = c AND e.isActive = true) < c.maxStudents")
    List<Classroom> findAvailableClassrooms(@Param("gradeLevel") GradeLevel gradeLevel, @Param("academicYear") AcademicYear academicYear);
}
