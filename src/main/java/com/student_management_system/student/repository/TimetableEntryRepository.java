package com.student_management_system.student.repository;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    
    // LEGACY: Keep for backward compatibility - updated to use 'teacher' instead of 'user'
    List<TimetableEntry> findByTeacherOrderByDayOfWeekAscStartTimeAsc(User teacher);

    // NEW: Advanced queries for TimeSlot-based timetable management
    
    /**
     * Find timetable entries by teacher and day/time slot
     */
    List<TimetableEntry> findByTeacherAndDayOfWeekAndTimeSlot(User teacher, DayOfWeek dayOfWeek, TimeSlot timeSlot);
    
    /**
     * Find timetable entries by classroom and day/time slot
     */
    List<TimetableEntry> findByClassroomAndDayOfWeekAndTimeSlot(Classroom classroom, DayOfWeek dayOfWeek, TimeSlot timeSlot);
    
    /**
     * Find all timetable entries for a classroom ordered by day and time
     */
    @Query("SELECT te FROM TimetableEntry te WHERE te.classroom = :classroom " +
           "ORDER BY te.dayOfWeek ASC, te.timeSlot.orderIndex ASC")
    List<TimetableEntry> findByClassroomOrderByDayAndTime(@Param("classroom") Classroom classroom);
    
    /**
     * Find all timetable entries for a teacher ordered by day and time
     */
    @Query("SELECT te FROM TimetableEntry te WHERE te.teacher = :teacher " +
           "ORDER BY te.dayOfWeek ASC, te.timeSlot.orderIndex ASC")
    List<TimetableEntry> findByTeacherOrderByDayAndTime(@Param("teacher") User teacher);
    
    /**
     * Find timetable entries by day and time slot ordered by classroom
     */
    @Query("SELECT te FROM TimetableEntry te WHERE te.dayOfWeek = :dayOfWeek AND te.timeSlot = :timeSlot " +
           "ORDER BY te.classroom.gradeLevel.level ASC, te.classroom.name ASC")
    List<TimetableEntry> findByDayOfWeekAndTimeSlotOrderByClassroom(
        @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("timeSlot") TimeSlot timeSlot);
    
    /**
     * Find timetable entries by teacher and day
     */
    List<TimetableEntry> findByTeacherAndDayOfWeek(User teacher, DayOfWeek dayOfWeek);
    
    /**
     * Find timetable entries by day and time slot
     */
    List<TimetableEntry> findByDayOfWeekAndTimeSlot(DayOfWeek dayOfWeek, TimeSlot timeSlot);
    
    /**
     * Find all entries for a specific teacher
     */
    List<TimetableEntry> findByTeacher(User teacher);
    
    /**
     * Find all entries for a specific classroom
     */
    List<TimetableEntry> findByClassroom(Classroom classroom);
    
    /**
     * Find entries by subject
     */
    @Query("SELECT te FROM TimetableEntry te WHERE te.subject.id = :subjectId")
    List<TimetableEntry> findBySubjectId(@Param("subjectId") Long subjectId);
    
    /**
     * Find timetable entries for student's enrolled classrooms
     */
    @Query("SELECT te FROM TimetableEntry te " +
           "JOIN Enrollment e ON e.classroom = te.classroom " +
           "WHERE e.student = :student AND e.isActive = true " +
           "ORDER BY te.dayOfWeek ASC, te.timeSlot.orderIndex ASC")
    List<TimetableEntry> findByStudentOrderByDayAndTime(@Param("student") User student);
    
    /**
     * Check if teacher has any conflicts at specific time
     */
    @Query("SELECT COUNT(te) > 0 FROM TimetableEntry te WHERE te.teacher = :teacher " +
           "AND te.dayOfWeek = :dayOfWeek AND te.timeSlot = :timeSlot")
    boolean hasTeacherConflict(@Param("teacher") User teacher, 
                              @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                              @Param("timeSlot") TimeSlot timeSlot);
    
    /**
     * Check if classroom has any conflicts at specific time
     */
    @Query("SELECT COUNT(te) > 0 FROM TimetableEntry te WHERE te.classroom = :classroom " +
           "AND te.dayOfWeek = :dayOfWeek AND te.timeSlot = :timeSlot")
    boolean hasClassroomConflict(@Param("classroom") Classroom classroom, 
                                @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                @Param("timeSlot") TimeSlot timeSlot);
}