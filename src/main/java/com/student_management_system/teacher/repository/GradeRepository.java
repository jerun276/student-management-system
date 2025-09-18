package com.student_management_system.teacher.repository;

import com.student_management_system.teacher.model.Grade;
import com.student_management_system.user_management.model.User;
import com.student_management_system.student.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    // Find grades by teacher
    List<Grade> findByTeacherOrderByGradedDateDesc(User teacher);
    
    // Find grades by student
    List<Grade> findByStudentOrderByGradedDateDesc(User student);
    
    // Find grades by subject
    List<Grade> findBySubjectOrderByGradedDateDesc(Subject subject);
    
    // Find grades by teacher and subject
    List<Grade> findByTeacherAndSubjectOrderByGradedDateDesc(User teacher, Subject subject);
    
    // Find grades by teacher with pagination
    Page<Grade> findByTeacher(User teacher, Pageable pageable);
    
    // Find grades by teacher and subject with pagination
    Page<Grade> findByTeacherAndSubject(User teacher, Subject subject, Pageable pageable);
    
    // Find grades by teacher and semester
    Page<Grade> findByTeacherAndSemester(User teacher, String semester, Pageable pageable);
    
    // Find grades by teacher, subject and semester
    Page<Grade> findByTeacherAndSubjectAndSemester(User teacher, Subject subject, String semester, Pageable pageable);
    
    // Find published grades by student
    List<Grade> findByStudentAndIsPublishedTrueOrderByGradedDateDesc(User student);
    
    // Search grades by comments
    List<Grade> findByCommentsContainingIgnoreCase(String searchTerm);
    
    // Find grades by academic year
    List<Grade> findByAcademicYearOrderByGradedDateDesc(String academicYear);
    
    // Custom query to get average grade by subject
    @Query("SELECT AVG(g.numericGrade) FROM Grade g WHERE g.subject = :subject AND g.numericGrade IS NOT NULL")
    Double getAverageGradeBySubject(@Param("subject") Subject subject);
    
    // Custom query to get grade statistics for a student
    @Query("SELECT COUNT(g), AVG(g.numericGrade), MIN(g.numericGrade), MAX(g.numericGrade) FROM Grade g WHERE g.student = :student AND g.numericGrade IS NOT NULL")
    Object[] getGradeStatisticsByStudent(@Param("student") User student);
}
