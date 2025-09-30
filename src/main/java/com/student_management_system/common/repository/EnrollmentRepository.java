package com.student_management_system.common.repository;

import com.student_management_system.common.model.Enrollment;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.model.Classroom;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    /**
     * Find active enrollment for a student in a specific academic year
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student = :student AND e.academicYear = :academicYear AND e.isActive = true")
    Optional<Enrollment> findActiveEnrollmentByStudentAndAcademicYear(@Param("student") User student, @Param("academicYear") AcademicYear academicYear);
    
    /**
     * Find all enrollments for a student
     */
    List<Enrollment> findByStudent(User student);
    
    /**
     * Find all active enrollments for a student
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student = :student AND e.isActive = true")
    List<Enrollment> findActiveEnrollmentsByStudent(@Param("student") User student);
    
    /**
     * Find all enrollments for a classroom
     */
    List<Enrollment> findByClassroom(Classroom classroom);
    
    /**
     * Find all active enrollments for a classroom
     */
    @Query("SELECT e FROM Enrollment e WHERE e.classroom = :classroom AND e.isActive = true")
    List<Enrollment> findActiveEnrollmentsByClassroom(@Param("classroom") Classroom classroom);
    
    /**
     * Find all enrollments for an academic year
     */
    List<Enrollment> findByAcademicYear(AcademicYear academicYear);
    
    /**
     * Find all active enrollments for an academic year
     */
    @Query("SELECT e FROM Enrollment e WHERE e.academicYear = :academicYear AND e.isActive = true")
    List<Enrollment> findActiveEnrollmentsByAcademicYear(@Param("academicYear") AcademicYear academicYear);
    
    /**
     * Get students enrolled in a specific classroom for current academic year
     */
    @Query("SELECT e.student FROM Enrollment e WHERE e.classroom = :classroom AND e.isActive = true")
    List<User> findStudentsByClassroom(@Param("classroom") Classroom classroom);
    
    /**
     * Check if a student is enrolled in a specific classroom for current academic year
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.student = :student AND e.classroom = :classroom AND e.isActive = true")
    boolean isStudentEnrolledInClassroom(@Param("student") User student, @Param("classroom") Classroom classroom);
    
    /**
     * Count active enrollments in a classroom
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.classroom = :classroom AND e.isActive = true")
    Long countActiveEnrollmentsByClassroom(@Param("classroom") Classroom classroom);
}
