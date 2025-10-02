package com.student_management_system.common.repository;

import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.model.TimeSlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    /**
     * Find all active time slots ordered by orderIndex
     */
    List<TimeSlot> findByIsActiveTrueOrderByOrderIndex();
    
    /**
     * Find time slots by type
     */
    List<TimeSlot> findByTypeAndIsActiveTrueOrderByOrderIndex(TimeSlotType type);
    
    /**
     * Find regular teaching periods only
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.type = 'REGULAR' AND ts.isActive = true ORDER BY ts.orderIndex")
    List<TimeSlot> findRegularPeriodsOrderByIndex();
    
    /**
     * Find time slot by name
     */
    Optional<TimeSlot> findByNameAndIsActiveTrue(String name);
    
    /**
     * Find time slots that conflict with a given time range
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isActive = true AND " +
           "NOT (ts.endTime <= :startTime OR ts.startTime >= :endTime)")
    List<TimeSlot> findConflictingTimeSlots(@Param("startTime") LocalTime startTime, 
                                          @Param("endTime") LocalTime endTime);
    
    /**
     * Find the next time slot after a given time
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isActive = true AND ts.startTime > :currentTime " +
           "ORDER BY ts.startTime ASC")
    Optional<TimeSlot> findNextTimeSlot(@Param("currentTime") LocalTime currentTime);
    
    /**
     * Find the current time slot for a given time
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isActive = true AND " +
           ":currentTime >= ts.startTime AND :currentTime < ts.endTime")
    Optional<TimeSlot> findCurrentTimeSlot(@Param("currentTime") LocalTime currentTime);
    
    /**
     * Get all break time slots
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isActive = true AND " +
           "(ts.type = 'BREAK' OR ts.type = 'LUNCH') ORDER BY ts.orderIndex")
    List<TimeSlot> findBreakTimeSlots();
    
    /**
     * Count active time slots
     */
    long countByIsActiveTrue();
    
    /**
     * Find time slots within a time range
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isActive = true AND " +
           "ts.startTime >= :startTime AND ts.endTime <= :endTime ORDER BY ts.orderIndex")
    List<TimeSlot> findTimeSlotsInRange(@Param("startTime") LocalTime startTime, 
                                      @Param("endTime") LocalTime endTime);
}
