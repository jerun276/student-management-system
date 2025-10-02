package com.student_management_system.teacher.controller;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.service.TimetableService;
import com.student_management_system.common.service.TimeSlotService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {
    private final TeacherService teacherService;
    private final AcademicYearService academicYearService;
    private final TimetableService timetableService;
    private final TimeSlotService timeSlotService;
    private final UserRepository userRepository;

    public TeacherDashboardController(TeacherService teacherService, 
                                    AcademicYearService academicYearService,
                                    TimetableService timetableService,
                                    TimeSlotService timeSlotService,
                                    UserRepository userRepository) {
        this.teacherService = teacherService;
        this.academicYearService = academicYearService;
        this.timetableService = timetableService;
        this.timeSlotService = timeSlotService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        List<Assignment> submittedAssignments = teacherService.getAssignmentsToGrade();
        model.addAttribute("submittedAssignments", submittedAssignments);
        
        // Get current academic year
        Optional<AcademicYear> currentAcademicYear = academicYearService.getCurrentAcademicYear();
        model.addAttribute("currentAcademicYear", currentAcademicYear.orElse(null));
        
        return "teacher/dashboard";
    }

    @GetMapping("/assignments/{id}/grade")
    public String showGradeAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = teacherService.getAssignmentToGradeById(id);
        model.addAttribute("assignment", assignment);
        return "teacher/assignment-grade";
    }

    @PostMapping("/assignments/{id}/grade")
    public String processGradeAssignment(
            @PathVariable Long id,
            @RequestParam String grade,
            @RequestParam String feedback) {
        teacherService.gradeAssignment(id, grade, feedback);
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/budget/request")
    public String submitBudgetRequest(@RequestParam String title,
                                      @RequestParam String description,
                                      @RequestParam BigDecimal amount,
                                      RedirectAttributes redirectAttributes) {
        try {
            teacherService.submitBudgetRequest(title, description, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Budget request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit request.");
        }
        return "redirect:/teacher/dashboard";
    }

    /**
     * Show teacher's personal timetable
     */
    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get teacher's timetable
        Map<DayOfWeek, List<TimetableEntry>> timetable = timetableService.getTeacherTimetable(teacher);
        
        // Get time slots for display
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();
        
        // Get teacher's free slots for today
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        List<TimetableService.FreeSlot> freeSlots = timetableService.getTeacherFreeSlots(teacher, today);

        model.addAttribute("teacher", teacher);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("freeSlots", freeSlots);
        model.addAttribute("weekDays", new DayOfWeek[]{
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        });

        return "teacher/timetable";
    }

    /**
     * Show current period information
     */
    @GetMapping("/current-period")
    public String showCurrentPeriod(Model model) {
        // Get current time slot
        Optional<TimeSlot> currentSlot = timeSlotService.getCurrentTimeSlot();
        Optional<TimeSlot> nextSlot = timeSlotService.getNextTimeSlot();

        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        model.addAttribute("currentSlot", currentSlot.orElse(null));
        model.addAttribute("nextSlot", nextSlot.orElse(null));
        model.addAttribute("teacher", teacher);

        // If there's a current slot, get the teacher's class for this period
        if (currentSlot.isPresent()) {
            DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
            List<TimetableEntry> currentEntries = timetableService.getTimetableEntriesForSlot(today, currentSlot.get());
            
            // Find teacher's entry for this period
            Optional<TimetableEntry> teacherEntry = currentEntries.stream()
                .filter(entry -> entry.getTeacher().getId().equals(teacher.getId()))
                .findFirst();
            
            model.addAttribute("currentEntry", teacherEntry.orElse(null));
        }

        return "teacher/current-period";
    }
}