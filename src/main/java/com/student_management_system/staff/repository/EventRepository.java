package com.student_management_system.staff.repository;

import com.student_management_system.staff.model.Event;
import com.student_management_system.staff.model.EventStatus;
import com.student_management_system.staff.model.EventType;
import com.student_management_system.user_management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find events by status
    List<Event> findByStatusOrderByEventDateTimeDesc(EventStatus status);
    
    // Find events by type
    List<Event> findByEventTypeOrderByEventDateTimeDesc(EventType eventType);
    
    // Find events by creator
    List<Event> findByCreatedByOrderByEventDateTimeDesc(User createdBy);
    
    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.eventDateTime > :currentDateTime ORDER BY e.eventDateTime ASC")
    List<Event> findUpcomingEvents(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    // Find events by date range
    @Query("SELECT e FROM Event e WHERE e.eventDateTime BETWEEN :startDate AND :endDate ORDER BY e.eventDateTime ASC")
    List<Event> findEventsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Search methods for SearchService
    List<Event> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    // Find top events for suggestions
    List<Event> findTop5ByNameContainingIgnoreCaseOrderByName(String name);
    
    // Find public events
    List<Event> findByIsPublicTrueOrderByEventDateTimeDesc();
    
    // Find events with pagination
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    
    // Find events by creator with pagination
    Page<Event> findByCreatedBy(User createdBy, Pageable pageable);
}