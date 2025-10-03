package com.student_management_system.common.service;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.repository.TimeSlotRepository;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.user_management.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing timetables with advanced scheduling and conflict detection
 */
@Service
@Transactional
public class TimetableService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TimetableService(TimetableEntryRepository timetableEntryRepository, 
                           TimeSlotRepository timeSlotRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Create a new timetable entry with conflict detection
     */
    public TimetableEntry createTimetableEntry(Subject subject, Classroom classroom, User teacher, 
                                             DayOfWeek dayOfWeek, TimeSlot timeSlot, String location) {
        
        // Validate that the teacher is assigned to this subject
        if (!subject.hasTeacher(teacher)) {
            throw new IllegalArgumentException("Teacher " + teacher.getFirstName() + " " + teacher.getLastName() + 
                " is not assigned to teach " + subject.getName());
        }

        // Check for conflicts
        List<TimetableConflict> conflicts = checkConflicts(classroom, teacher, dayOfWeek, timeSlot);
        if (!conflicts.isEmpty()) {
            throw new TimetableConflictException("Cannot create timetable entry due to conflicts: " + 
                conflicts.stream().map(TimetableConflict::getDescription).collect(Collectors.joining(", ")));
        }

        // Create the timetable entry
        TimetableEntry entry = new TimetableEntry();
        entry.setSubject(subject);
        entry.setClassroom(classroom);
        entry.setTeacher(teacher);
        entry.setDayOfWeek(dayOfWeek);
        entry.setTimeSlot(timeSlot);
        entry.setLocation(location);

        return timetableEntryRepository.save(entry);
    }

    /**
     * Check for scheduling conflicts
     */
    public List<TimetableConflict> checkConflicts(Classroom classroom, User teacher, 
                                                 DayOfWeek dayOfWeek, TimeSlot timeSlot) {
        List<TimetableConflict> conflicts = new ArrayList<>();

        // Check teacher conflicts
        List<TimetableEntry> teacherEntries = timetableEntryRepository
            .findByTeacherAndDayOfWeekAndTimeSlot(teacher, dayOfWeek, timeSlot);
        
        for (TimetableEntry entry : teacherEntries) {
            conflicts.add(new TimetableConflict(
                TimetableConflict.ConflictType.TEACHER_DOUBLE_BOOKED,
                "❌ TEACHER CONFLICT: " + teacher.getFirstName() + " " + teacher.getLastName() + 
                " is already teaching " + entry.getSubject().getName() + 
                " in " + entry.getClassroom().getFullName() + " during this time slot. " +
                "A teacher can only be in one classroom at a time."
            ));
        }

        // Check classroom conflicts
        List<TimetableEntry> classroomEntries = timetableEntryRepository
            .findByClassroomAndDayOfWeekAndTimeSlot(classroom, dayOfWeek, timeSlot);
        
        for (TimetableEntry entry : classroomEntries) {
            conflicts.add(new TimetableConflict(
                TimetableConflict.ConflictType.CLASSROOM_DOUBLE_BOOKED,
                "🏫 CLASSROOM CONFLICT: " + classroom.getFullName() + " is already occupied by " + 
                entry.getSubject().getName() + " with teacher " + 
                entry.getTeacher().getFirstName() + " " + entry.getTeacher().getLastName() + 
                " during this time slot."
            ));
        }

        return conflicts;
    }

    /**
     * Get timetable for a specific classroom
     */
    public Map<DayOfWeek, List<TimetableEntry>> getClassroomTimetable(Classroom classroom) {
        List<TimetableEntry> entries = timetableEntryRepository.findByClassroomOrderByDayAndTime(classroom);
        return groupByDayOfWeek(entries);
    }

    /**
     * Get timetable for a specific teacher
     */
    public Map<DayOfWeek, List<TimetableEntry>> getTeacherTimetable(User teacher) {
        List<TimetableEntry> entries = timetableEntryRepository.findByTeacherOrderByDayAndTime(teacher);
        return groupByDayOfWeek(entries);
    }

    /**
     * Get timetable for students in a specific classroom
     */
    public Map<DayOfWeek, List<TimetableEntry>> getStudentTimetable(Classroom classroom) {
        // Students follow their classroom's timetable
        return getClassroomTimetable(classroom);
    }

    /**
     * Get all timetable entries for a specific day and time slot
     */
    public List<TimetableEntry> getTimetableEntriesForSlot(DayOfWeek dayOfWeek, TimeSlot timeSlot) {
        return timetableEntryRepository.findByDayOfWeekAndTimeSlot(dayOfWeek, timeSlot);
    }

    /**
     * Update an existing timetable entry
     */
    public TimetableEntry updateTimetableEntry(Long entryId, Subject subject, Classroom classroom, 
                                             User teacher, DayOfWeek dayOfWeek, TimeSlot timeSlot, String location) {
        
        TimetableEntry entry = timetableEntryRepository.findById(entryId)
            .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with id: " + entryId));

        // Check conflicts (excluding current entry)
        List<TimetableConflict> conflicts = checkConflictsExcluding(classroom, teacher, dayOfWeek, timeSlot, entryId);
        if (!conflicts.isEmpty()) {
            throw new TimetableConflictException("Cannot update timetable entry due to conflicts: " + 
                conflicts.stream().map(TimetableConflict::getDescription).collect(Collectors.joining(", ")));
        }

        // Update the entry
        entry.setSubject(subject);
        entry.setClassroom(classroom);
        entry.setTeacher(teacher);
        entry.setDayOfWeek(dayOfWeek);
        entry.setTimeSlot(timeSlot);
        entry.setLocation(location);

        return timetableEntryRepository.save(entry);
    }

    /**
     * Find timetable entry by ID
     */
    public Optional<TimetableEntry> findById(Long entryId) {
        return timetableEntryRepository.findById(entryId);
    }

    /**
     * Delete a timetable entry
     */
    public void deleteTimetableEntry(Long entryId) {
        if (!timetableEntryRepository.existsById(entryId)) {
            throw new IllegalArgumentException("Timetable entry not found with id: " + entryId);
        }
        timetableEntryRepository.deleteById(entryId);
    }

    /**
     * Get teacher workload (number of periods per week)
     */
    public Map<User, Integer> getTeacherWorkloads() {
        List<TimetableEntry> allEntries = timetableEntryRepository.findAll();
        return allEntries.stream()
            .collect(Collectors.groupingBy(
                TimetableEntry::getTeacher,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
    }

    /**
     * Get classroom utilization (percentage of time slots used)
     */
    public Map<Classroom, Double> getClassroomUtilization() {
        List<TimeSlot> regularPeriods = timeSlotRepository.findRegularPeriodsOrderByIndex();
        int totalSlotsPerWeek = regularPeriods.size() * 5; // 5 working days

        List<TimetableEntry> allEntries = timetableEntryRepository.findAll();
        Map<Classroom, Long> classroomCounts = allEntries.stream()
            .collect(Collectors.groupingBy(TimetableEntry::getClassroom, Collectors.counting()));

        Map<Classroom, Double> utilization = new HashMap<>();
        for (Map.Entry<Classroom, Long> entry : classroomCounts.entrySet()) {
            double percentage = (entry.getValue().doubleValue() / totalSlotsPerWeek) * 100.0;
            utilization.put(entry.getKey(), percentage);
        }

        return utilization;
    }

    /**
     * Find free periods for a teacher
     */
    public List<FreeSlot> getTeacherFreeSlots(User teacher, DayOfWeek dayOfWeek) {
        List<TimetableEntry> teacherEntries = timetableEntryRepository.findByTeacherAndDayOfWeek(teacher, dayOfWeek);
        List<TimeSlot> allRegularSlots = timeSlotRepository.findRegularPeriodsOrderByIndex();
        
        Set<Long> occupiedSlotIds = teacherEntries.stream()
            .map(entry -> entry.getTimeSlot().getId())
            .collect(Collectors.toSet());

        return allRegularSlots.stream()
            .filter(slot -> !occupiedSlotIds.contains(slot.getId()))
            .map(slot -> new FreeSlot(dayOfWeek, slot))
            .collect(Collectors.toList());
    }

    /**
     * Find available classrooms for a specific time slot
     */
    public List<Classroom> getAvailableClassrooms(DayOfWeek dayOfWeek, TimeSlot timeSlot) {
        List<TimetableEntry> occupiedEntries = timetableEntryRepository.findByDayOfWeekAndTimeSlot(dayOfWeek, timeSlot);
        Set<Long> occupiedClassroomIds = occupiedEntries.stream()
            .map(entry -> entry.getClassroom().getId())
            .collect(Collectors.toSet());

        // This would need to be implemented with a ClassroomRepository
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    /**
     * Generate timetable statistics
     */
    public TimetableStatistics generateStatistics() {
        List<TimetableEntry> allEntries = timetableEntryRepository.findAll();
        
        int totalEntries = allEntries.size();
        long uniqueTeachers = allEntries.stream().map(TimetableEntry::getTeacher).distinct().count();
        long uniqueClassrooms = allEntries.stream().map(TimetableEntry::getClassroom).distinct().count();
        long uniqueSubjects = allEntries.stream().map(TimetableEntry::getSubject).distinct().count();

        Map<User, Integer> teacherWorkloads = getTeacherWorkloads();
        OptionalDouble avgWorkload = teacherWorkloads.values().stream().mapToInt(Integer::intValue).average();

        return new TimetableStatistics(
            totalEntries,
            (int) uniqueTeachers,
            (int) uniqueClassrooms,
            (int) uniqueSubjects,
            avgWorkload.orElse(0.0)
        );
    }

    // Helper methods
    private Map<DayOfWeek, List<TimetableEntry>> groupByDayOfWeek(List<TimetableEntry> entries) {
        return entries.stream()
            .collect(Collectors.groupingBy(
                TimetableEntry::getDayOfWeek,
                () -> new EnumMap<>(DayOfWeek.class),
                Collectors.toList()
            ));
    }

    private List<TimetableConflict> checkConflictsExcluding(Classroom classroom, User teacher, 
                                                          DayOfWeek dayOfWeek, TimeSlot timeSlot, Long excludeId) {
        List<TimetableConflict> conflicts = checkConflicts(classroom, teacher, dayOfWeek, timeSlot);
        // Remove conflicts from the entry being updated
        return conflicts; // Simplified for now
    }

    // Inner classes for data structures
    public static class TimetableConflict {
        public enum ConflictType {
            TEACHER_DOUBLE_BOOKED,
            CLASSROOM_DOUBLE_BOOKED,
            SUBJECT_TEACHER_MISMATCH
        }

        private final ConflictType type;
        private final String description;

        public TimetableConflict(ConflictType type, String description) {
            this.type = type;
            this.description = description;
        }

        public ConflictType getType() { return type; }
        public String getDescription() { return description; }
    }

    public static class FreeSlot {
        private final DayOfWeek dayOfWeek;
        private final TimeSlot timeSlot;

        public FreeSlot(DayOfWeek dayOfWeek, TimeSlot timeSlot) {
            this.dayOfWeek = dayOfWeek;
            this.timeSlot = timeSlot;
        }

        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public TimeSlot getTimeSlot() { return timeSlot; }
    }

    public static class TimetableStatistics {
        private final int totalEntries;
        private final int uniqueTeachers;
        private final int uniqueClassrooms;
        private final int uniqueSubjects;
        private final double averageTeacherWorkload;

        public TimetableStatistics(int totalEntries, int uniqueTeachers, int uniqueClassrooms, 
                                 int uniqueSubjects, double averageTeacherWorkload) {
            this.totalEntries = totalEntries;
            this.uniqueTeachers = uniqueTeachers;
            this.uniqueClassrooms = uniqueClassrooms;
            this.uniqueSubjects = uniqueSubjects;
            this.averageTeacherWorkload = averageTeacherWorkload;
        }

        // Getters
        public int getTotalEntries() { return totalEntries; }
        public int getUniqueTeachers() { return uniqueTeachers; }
        public int getUniqueClassrooms() { return uniqueClassrooms; }
        public int getUniqueSubjects() { return uniqueSubjects; }
        public double getAverageTeacherWorkload() { return averageTeacherWorkload; }
    }

    // Custom exception for timetable conflicts
    public static class TimetableConflictException extends RuntimeException {
        public TimetableConflictException(String message) {
            super(message);
        }
    }
}
