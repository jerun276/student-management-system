package com.student_management_system.common.service;

import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.model.TimeSlotType;
import com.student_management_system.common.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing time slots in the school schedule
 */
@Service
@Transactional
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Get all active time slots ordered by index
     */
    public List<TimeSlot> getAllActiveTimeSlots() {
        return timeSlotRepository.findByIsActiveTrueOrderByOrderIndex();
    }

    /**
     * Get regular teaching periods only
     */
    public List<TimeSlot> getRegularPeriods() {
        return timeSlotRepository.findRegularPeriodsOrderByIndex();
    }

    /**
     * Get break time slots
     */
    public List<TimeSlot> getBreakTimeSlots() {
        return timeSlotRepository.findBreakTimeSlots();
    }

    /**
     * Find time slot by ID
     */
    public Optional<TimeSlot> findById(Long id) {
        return timeSlotRepository.findById(id);
    }

    /**
     * Find time slot by name
     */
    public Optional<TimeSlot> findByName(String name) {
        return timeSlotRepository.findByNameAndIsActiveTrue(name);
    }

    /**
     * Get current time slot based on current time
     */
    public Optional<TimeSlot> getCurrentTimeSlot() {
        LocalTime now = LocalTime.now();
        return timeSlotRepository.findCurrentTimeSlot(now);
    }

    /**
     * Get next time slot after current time
     */
    public Optional<TimeSlot> getNextTimeSlot() {
        LocalTime now = LocalTime.now();
        return timeSlotRepository.findNextTimeSlot(now);
    }

    /**
     * Create a new time slot
     */
    public TimeSlot createTimeSlot(String name, LocalTime startTime, LocalTime endTime, 
                                 TimeSlotType type, Integer orderIndex, String description) {
        
        // Validate no conflicts with existing time slots
        List<TimeSlot> conflicts = timeSlotRepository.findConflictingTimeSlots(startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Time slot conflicts with existing slots: " + 
                conflicts.stream().map(TimeSlot::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setName(name);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setType(type);
        timeSlot.setOrderIndex(orderIndex);
        timeSlot.setDescription(description);
        timeSlot.setActive(true);

        return timeSlotRepository.save(timeSlot);
    }

    /**
     * Update an existing time slot
     */
    public TimeSlot updateTimeSlot(Long id, String name, LocalTime startTime, LocalTime endTime, 
                                 TimeSlotType type, Integer orderIndex, String description) {
        
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + id));

        // Check for conflicts with other time slots (excluding this one)
        List<TimeSlot> conflicts = timeSlotRepository.findConflictingTimeSlots(startTime, endTime);
        conflicts.removeIf(ts -> ts.getId().equals(id)); // Remove self from conflicts
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Time slot conflicts with existing slots: " + 
                conflicts.stream().map(TimeSlot::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        }

        timeSlot.setName(name);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setType(type);
        timeSlot.setOrderIndex(orderIndex);
        timeSlot.setDescription(description);

        return timeSlotRepository.save(timeSlot);
    }

    /**
     * Soft delete a time slot
     */
    public void deleteTimeSlot(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + id));
        
        timeSlot.setActive(false);
        timeSlotRepository.save(timeSlot);
    }

    /**
     * Initialize default time slots for a school
     */
    public void initializeDefaultTimeSlots() {
        if (timeSlotRepository.countByIsActiveTrue() > 0) {
            return; // Already initialized
        }

        // Create standard school periods
        createTimeSlot("Period 1", LocalTime.of(8, 0), LocalTime.of(8, 45), TimeSlotType.REGULAR, 1, "First period of the day");
        createTimeSlot("Period 2", LocalTime.of(8, 45), LocalTime.of(9, 30), TimeSlotType.REGULAR, 2, "Second period");
        createTimeSlot("Break", LocalTime.of(9, 30), LocalTime.of(9, 45), TimeSlotType.BREAK, 3, "Morning break");
        createTimeSlot("Period 3", LocalTime.of(9, 45), LocalTime.of(10, 30), TimeSlotType.REGULAR, 4, "Third period");
        createTimeSlot("Period 4", LocalTime.of(10, 30), LocalTime.of(11, 15), TimeSlotType.REGULAR, 5, "Fourth period");
        createTimeSlot("Period 5", LocalTime.of(11, 15), LocalTime.of(12, 0), TimeSlotType.REGULAR, 6, "Fifth period");
        createTimeSlot("Lunch", LocalTime.of(12, 0), LocalTime.of(12, 45), TimeSlotType.LUNCH, 7, "Lunch break");
        createTimeSlot("Period 6", LocalTime.of(12, 45), LocalTime.of(13, 30), TimeSlotType.REGULAR, 8, "Sixth period");
        createTimeSlot("Period 7", LocalTime.of(13, 30), LocalTime.of(14, 15), TimeSlotType.REGULAR, 9, "Seventh period");
        createTimeSlot("Period 8", LocalTime.of(14, 15), LocalTime.of(15, 0), TimeSlotType.REGULAR, 10, "Eighth period");
    }

    /**
     * Check if a time range conflicts with existing time slots
     */
    public boolean hasTimeConflict(LocalTime startTime, LocalTime endTime, Long excludeId) {
        List<TimeSlot> conflicts = timeSlotRepository.findConflictingTimeSlots(startTime, endTime);
        if (excludeId != null) {
            conflicts.removeIf(ts -> ts.getId().equals(excludeId));
        }
        return !conflicts.isEmpty();
    }

    /**
     * Get time slots by type
     */
    public List<TimeSlot> getTimeSlotsByType(TimeSlotType type) {
        return timeSlotRepository.findByTypeAndIsActiveTrueOrderByOrderIndex(type);
    }
}
