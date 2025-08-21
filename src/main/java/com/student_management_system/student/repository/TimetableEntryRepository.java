package com.student_management_system.student.repository;

import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    // This is a custom method to find all timetable entries for a specific user,
    // ordered by day and start time. Spring Data JPA will implement it for us!
    List<TimetableEntry> findByUserOrderByDayOfWeekAscStartTimeAsc(User user);
}