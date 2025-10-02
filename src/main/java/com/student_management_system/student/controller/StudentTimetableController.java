package com.student_management_system.student.controller;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.repository.EnrollmentRepository;
import com.student_management_system.common.service.TimetableService;
import com.student_management_system.common.service.TimeSlotService;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for student timetable views
 */
@Controller
@RequestMapping("/student")
public class StudentTimetableController {

    private final TimetableService timetableService;
    private final TimeSlotService timeSlotService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public StudentTimetableController(TimetableService timetableService,
                                    TimeSlotService timeSlotService,
                                    UserRepository userRepository,
                                    EnrollmentRepository enrollmentRepository) {
        this.timetableService = timetableService;
        this.timeSlotService = timeSlotService;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Show student's timetable
     */
    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        // Get current student
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User student = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current student not found"));

        // Find student's classroom through enrollment
        List<Classroom> studentClassrooms = enrollmentRepository.findClassroomsByStudent(student);
        
        if (studentClassrooms.isEmpty()) {
            model.addAttribute("errorMessage", "You are not enrolled in any classroom.");
            return "student/timetable";
        }

        // Get the first (current) classroom - students typically have one active enrollment
        Classroom classroom = studentClassrooms.get(0);
        
        // Get classroom timetable (students follow their classroom's schedule)
        Map<DayOfWeek, List<TimetableEntry>> timetable = timetableService.getClassroomTimetable(classroom);
        
        // Get time slots for display
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();

        model.addAttribute("student", student);
        model.addAttribute("classroom", classroom);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("weekDays", new DayOfWeek[]{
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        });

        return "student/timetable";
    }

    /**
     * Show current period information for student
     */
    @GetMapping("/current-period")
    public String showCurrentPeriod(Model model) {
        // Get current time slot
        Optional<TimeSlot> currentSlot = timeSlotService.getCurrentTimeSlot();
        Optional<TimeSlot> nextSlot = timeSlotService.getNextTimeSlot();

        // Get current student
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User student = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current student not found"));

        // Find student's classroom
        List<Classroom> studentClassrooms = enrollmentRepository.findClassroomsByStudent(student);
        
        model.addAttribute("currentSlot", currentSlot.orElse(null));
        model.addAttribute("nextSlot", nextSlot.orElse(null));
        model.addAttribute("student", student);

        if (!studentClassrooms.isEmpty() && currentSlot.isPresent()) {
            Classroom classroom = studentClassrooms.get(0);
            DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
            
            // Get current period's timetable entry for the student's classroom
            List<TimetableEntry> currentEntries = timetableService.getTimetableEntriesForSlot(today, currentSlot.get());
            
            // Find classroom's entry for this period
            Optional<TimetableEntry> classroomEntry = currentEntries.stream()
                .filter(entry -> entry.getClassroom().getId().equals(classroom.getId()))
                .findFirst();
            
            model.addAttribute("classroom", classroom);
            model.addAttribute("currentEntry", classroomEntry.orElse(null));
        }

        return "student/current-period";
    }

    /**
     * Show today's schedule for student
     */
    @GetMapping("/today")
    public String showTodaySchedule(Model model) {
        // Get current student
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User student = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current student not found"));

        // Find student's classroom
        List<Classroom> studentClassrooms = enrollmentRepository.findClassroomsByStudent(student);
        
        if (studentClassrooms.isEmpty()) {
            model.addAttribute("errorMessage", "You are not enrolled in any classroom.");
            return "student/today";
        }

        Classroom classroom = studentClassrooms.get(0);
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        
        // Get today's timetable for the classroom
        Map<DayOfWeek, List<TimetableEntry>> fullTimetable = timetableService.getClassroomTimetable(classroom);
        List<TimetableEntry> todayEntries = fullTimetable.get(today);
        
        // Get time slots
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();
        
        // Get current and next periods
        Optional<TimeSlot> currentSlot = timeSlotService.getCurrentTimeSlot();
        Optional<TimeSlot> nextSlot = timeSlotService.getNextTimeSlot();

        model.addAttribute("student", student);
        model.addAttribute("classroom", classroom);
        model.addAttribute("todayEntries", todayEntries);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("currentSlot", currentSlot.orElse(null));
        model.addAttribute("nextSlot", nextSlot.orElse(null));
        model.addAttribute("today", today);

        return "student/today";
    }
}
