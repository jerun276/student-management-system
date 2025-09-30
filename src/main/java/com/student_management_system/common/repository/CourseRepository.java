package com.student_management_system.common.repository;

import com.student_management_system.common.model.Course;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.model.Classroom;
import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Find courses by classroom
     */
    List<Course> findByClassroom(Classroom classroom);
    
    /**
     * Find active courses by classroom
     */
    @Query("SELECT c FROM Course c WHERE c.classroom = :classroom AND c.isActive = true")
    List<Course> findActiveByClassroom(@Param("classroom") Classroom classroom);
    
    /**
     * Find courses by teacher
     */
    List<Course> findByTeacher(User teacher);
    
    /**
     * Find active courses by teacher
     */
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher AND c.isActive = true")
    List<Course> findActiveByTeacher(@Param("teacher") User teacher);
    
    /**
     * Find courses by subject
     */
    List<Course> findBySubject(Subject subject);
    
    /**
     * Find courses by academic year
     */
    List<Course> findByAcademicYear(AcademicYear academicYear);
    
    /**
     * Find active courses by academic year
     */
    @Query("SELECT c FROM Course c WHERE c.academicYear = :academicYear AND c.isActive = true")
    List<Course> findActiveByAcademicYear(@Param("academicYear") AcademicYear academicYear);
    
    /**
     * Find course by subject, classroom, and academic year (should be unique)
     */
    Optional<Course> findBySubjectAndClassroomAndAcademicYear(Subject subject, Classroom classroom, AcademicYear academicYear);
    
    /**
     * Find courses for a specific student through their enrollment
     */
    @Query("SELECT c FROM Course c JOIN Enrollment e ON c.classroom = e.classroom " +
           "WHERE e.student = :student AND e.isActive = true AND c.isActive = true")
    List<Course> findCoursesByStudent(@Param("student") User student);
    
    /**
     * Find courses for a specific student in a specific academic year
     */
    @Query("SELECT c FROM Course c JOIN Enrollment e ON c.classroom = e.classroom " +
           "WHERE e.student = :student AND c.academicYear = :academicYear AND e.isActive = true AND c.isActive = true")
    List<Course> findCoursesByStudentAndAcademicYear(@Param("student") User student, @Param("academicYear") AcademicYear academicYear);
    
    /**
     * Check if a teacher is assigned to teach a specific subject to a specific classroom
     */
    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.teacher = :teacher AND c.subject = :subject AND c.classroom = :classroom AND c.isActive = true")
    boolean isTeacherAssignedToSubjectInClassroom(@Param("teacher") User teacher, @Param("subject") Subject subject, @Param("classroom") Classroom classroom);
}
