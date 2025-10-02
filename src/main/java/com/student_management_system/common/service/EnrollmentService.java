package com.student_management_system.common.service;

import com.student_management_system.common.model.*;
import com.student_management_system.common.repository.*;
import com.student_management_system.user_management.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private AcademicYearService academicYearService;
    
    /**
     * Enroll a student in a classroom for a specific academic year
     */
    public Enrollment enrollStudent(User student, Classroom classroom, AcademicYear academicYear) {
        // Check if student is already actively enrolled in this academic year
        Optional<Enrollment> activeEnrollment = enrollmentRepository
            .findActiveEnrollmentByStudentAndAcademicYear(student, academicYear);
        
        if (activeEnrollment.isPresent()) {
            throw new IllegalStateException("Student is already enrolled in a classroom for this academic year");
        }
        
        // Check for any existing enrollment (including withdrawn) for this student and academic year
        Optional<Enrollment> existingEnrollment = enrollmentRepository
            .findByStudentAndAcademicYear(student, academicYear);
        
        // Check classroom capacity
        Long currentEnrollmentCount = classroomRepository.getCurrentEnrollmentCount(classroom);
        if (currentEnrollmentCount >= classroom.getMaxStudents()) {
            throw new IllegalStateException("Classroom has reached maximum capacity");
        }
        
        Enrollment enrollment;
        if (existingEnrollment.isPresent()) {
            // Reactivate existing enrollment record
            enrollment = existingEnrollment.get();
            enrollment.setClassroom(classroom);
            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setActive(true);
            enrollment.setWithdrawalDate(null);
            enrollment.setRemarks("Re-enrolled after withdrawal");
        } else {
            // Create new enrollment record
            enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassroom(classroom);
            enrollment.setAcademicYear(academicYear);
            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setActive(true);
        }
        
        return enrollmentRepository.save(enrollment);
    }
    
    /**
     * Enroll a student in a classroom for the current academic year
     */
    public Enrollment enrollStudentInCurrentYear(User student, Classroom classroom) {
        Optional<AcademicYear> currentYear = academicYearService.getCurrentAcademicYear();
        if (currentYear.isEmpty()) {
            throw new IllegalStateException("No active academic year found");
        }
        
        return enrollStudent(student, classroom, currentYear.get());
    }
    
    /**
     * Transfer a student to a different classroom
     */
    public Enrollment transferStudent(User student, Classroom newClassroom, AcademicYear academicYear) {
        // Find current enrollment
        Optional<Enrollment> currentEnrollmentOpt = enrollmentRepository
            .findActiveEnrollmentByStudentAndAcademicYear(student, academicYear);
        
        if (currentEnrollmentOpt.isEmpty()) {
            throw new IllegalStateException("Student is not currently enrolled in this academic year");
        }
        
        // Check new classroom capacity
        Long currentEnrollmentCount = classroomRepository.getCurrentEnrollmentCount(newClassroom);
        if (currentEnrollmentCount >= newClassroom.getMaxStudents()) {
            throw new IllegalStateException("New classroom has reached maximum capacity");
        }
        
        // Deactivate current enrollment
        Enrollment currentEnrollment = currentEnrollmentOpt.get();
        currentEnrollment.setActive(false);
        currentEnrollment.setWithdrawalDate(LocalDate.now());
        enrollmentRepository.save(currentEnrollment);
        
        // Create new enrollment
        return enrollStudent(student, newClassroom, academicYear);
    }
    
    /**
     * Withdraw a student from their current classroom
     */
    public void withdrawStudent(User student, AcademicYear academicYear, String remarks) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository
            .findActiveEnrollmentByStudentAndAcademicYear(student, academicYear);
        
        if (enrollmentOpt.isEmpty()) {
            throw new IllegalStateException("Student is not currently enrolled in this academic year");
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        enrollment.setActive(false);
        enrollment.setWithdrawalDate(LocalDate.now());
        enrollment.setRemarks(remarks);
        
        enrollmentRepository.save(enrollment);
    }
    
    /**
     * Get current enrollment for a student
     */
    public Optional<Enrollment> getCurrentEnrollment(User student) {
        Optional<AcademicYear> currentYear = academicYearService.getCurrentAcademicYear();
        if (currentYear.isEmpty()) {
            return Optional.empty();
        }
        
        return enrollmentRepository.findActiveEnrollmentByStudentAndAcademicYear(student, currentYear.get());
    }
    
    /**
     * Get all students enrolled in a classroom
     */
    public List<User> getStudentsInClassroom(Classroom classroom) {
        return enrollmentRepository.findStudentsByClassroom(classroom);
    }
    
    /**
     * Get all active enrollments for a classroom
     */
    public List<Enrollment> getActiveEnrollments(Classroom classroom) {
        return enrollmentRepository.findActiveEnrollmentsByClassroom(classroom);
    }
    
    /**
     * Get enrollment history for a student
     */
    public List<Enrollment> getEnrollmentHistory(User student) {
        return enrollmentRepository.findByStudent(student);
    }
    
    /**
     * Check if a student is enrolled in a specific classroom
     */
    public boolean isStudentEnrolledInClassroom(User student, Classroom classroom) {
        return enrollmentRepository.isStudentEnrolledInClassroom(student, classroom);
    }
    
    /**
     * Get current enrollment count for a classroom
     */
    public Long getCurrentEnrollmentCount(Classroom classroom) {
        return enrollmentRepository.countActiveEnrollmentsByClassroom(classroom);
    }
    
    /**
     * Get available spots in a classroom
     */
    public Integer getAvailableSpots(Classroom classroom) {
        Long currentCount = getCurrentEnrollmentCount(classroom);
        return classroom.getMaxStudents() - currentCount.intValue();
    }
    
    /**
     * Check if a student is enrolled in any classroom for a specific academic year
     */
    public boolean isStudentEnrolledInAcademicYear(User student, AcademicYear academicYear) {
        Optional<Enrollment> enrollment = enrollmentRepository
            .findActiveEnrollmentByStudentAndAcademicYear(student, academicYear);
        return enrollment.isPresent();
    }
    
    /**
     * Enroll a student in a classroom (uses classroom's academic year)
     */
    public Enrollment enrollStudent(User student, Classroom classroom) {
        return enrollStudent(student, classroom, classroom.getAcademicYear());
    }
}
