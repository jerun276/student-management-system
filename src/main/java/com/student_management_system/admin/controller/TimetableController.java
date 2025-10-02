package com.student_management_system.admin.controller;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.repository.ClassroomRepository;
import com.student_management_system.common.service.TimeSlotService;
import com.student_management_system.common.service.TimetableService;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * Controller for timetable management interfaces
 */
@Controller
@RequestMapping("/admin/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final TimeSlotService timeSlotService;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public TimetableController(TimetableService timetableService,
                             TimeSlotService timeSlotService,
                             ClassroomRepository classroomRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository) {
        this.timetableService = timetableService;
        this.timeSlotService = timeSlotService;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    /**
     * Show timetable management dashboard
     */
    @GetMapping
    public String showTimetableDashboard(Model model) {
        // Get statistics
        TimetableService.TimetableStatistics stats = timetableService.generateStatistics();
        model.addAttribute("statistics", stats);

        // Get all time slots
        List<TimeSlot> timeSlots = timeSlotService.getAllActiveTimeSlots();
        model.addAttribute("timeSlots", timeSlots);

        // Get teacher workloads
        Map<User, Integer> workloads = timetableService.getTeacherWorkloads();
        model.addAttribute("teacherWorkloads", workloads);

        // Get classroom utilization
        Map<Classroom, Double> utilization = timetableService.getClassroomUtilization();
        model.addAttribute("classroomUtilization", utilization);

        return "admin/timetable/dashboard";
    }

    /**
     * Show master timetable view
     */
    @GetMapping("/master")
    public String showMasterTimetable(Model model) {
        List<TimeSlot> regularPeriods = timeSlotService.getRegularPeriods();
        model.addAttribute("timeSlots", regularPeriods);

        // Get all timetable entries grouped by day and time
        DayOfWeek[] weekDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                               DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        model.addAttribute("weekDays", weekDays);

        return "admin/timetable/master";
    }

    /**
     * Get timetable entries for a specific day and time slot (AJAX)
     */
    @GetMapping("/entries")
    @ResponseBody
    public List<TimetableEntry> getTimetableEntries(@RequestParam DayOfWeek day, 
                                                   @RequestParam Long timeSlotId) {
        TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
        
        return timetableService.getTimetableEntriesForSlot(day, timeSlot);
    }

    /**
     * Show form to create new timetable entry
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findByIsActiveTrue());
        model.addAttribute("teachers", userRepository.findByRole(com.student_management_system.user_management.model.Role.ROLE_TEACHER));
        model.addAttribute("timeSlots", timeSlotService.getRegularPeriods());
        model.addAttribute("weekDays", DayOfWeek.values());

        return "admin/timetable/create";
    }

    /**
     * Create new timetable entry
     */
    @PostMapping("/create")
    public String createTimetableEntry(@RequestParam Long subjectId,
                                     @RequestParam Long classroomId,
                                     @RequestParam Long teacherId,
                                     @RequestParam DayOfWeek dayOfWeek,
                                     @RequestParam Long timeSlotId,
                                     @RequestParam(required = false) String location,
                                     RedirectAttributes redirectAttributes) {
        try {
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
            
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

            timetableService.createTimetableEntry(subject, classroom, teacher, dayOfWeek, timeSlot, location);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Timetable entry created successfully!");
            
        } catch (TimetableService.TimetableConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Conflict detected: " + e.getMessage());
            return "redirect:/admin/timetable/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating timetable entry: " + e.getMessage());
            return "redirect:/admin/timetable/create";
        }

        return "redirect:/admin/timetable";
    }

    /**
     * Show classroom timetable
     */
    @GetMapping("/classroom/{id}")
    public String showClassroomTimetable(@PathVariable Long id, Model model) {
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
        
        Map<DayOfWeek, List<TimetableEntry>> timetable = timetableService.getClassroomTimetable(classroom);
        
        model.addAttribute("classroom", classroom);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timeSlots", timeSlotService.getRegularPeriods());
        model.addAttribute("weekDays", new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY});

        return "admin/timetable/classroom";
    }

    /**
     * Show teacher timetable
     */
    @GetMapping("/teacher/{id}")
    public String showTeacherTimetable(@PathVariable Long id, Model model) {
        User teacher = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        
        Map<DayOfWeek, List<TimetableEntry>> timetable = timetableService.getTeacherTimetable(teacher);
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timeSlots", timeSlotService.getRegularPeriods());
        model.addAttribute("weekDays", new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY});

        return "admin/timetable/teacher";
    }

    /**
     * Show form to edit timetable entry
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        TimetableEntry entry = timetableService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found"));
        
        model.addAttribute("entry", entry);
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findByIsActiveTrue());
        model.addAttribute("teachers", userRepository.findByRole(com.student_management_system.user_management.model.Role.ROLE_TEACHER));
        model.addAttribute("timeSlots", timeSlotService.getRegularPeriods());
        model.addAttribute("weekDays", DayOfWeek.values());

        return "admin/timetable/edit";
    }

    /**
     * Update timetable entry
     */
    @PostMapping("/edit/{id}")
    public String updateTimetableEntry(@PathVariable Long id,
                                     @RequestParam Long subjectId,
                                     @RequestParam Long classroomId,
                                     @RequestParam Long teacherId,
                                     @RequestParam DayOfWeek dayOfWeek,
                                     @RequestParam Long timeSlotId,
                                     @RequestParam(required = false) String location,
                                     RedirectAttributes redirectAttributes) {
        try {
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
            
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

            timetableService.updateTimetableEntry(id, subject, classroom, teacher, dayOfWeek, timeSlot, location);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Timetable entry updated successfully!");
            
        } catch (TimetableService.TimetableConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Conflict detected: " + e.getMessage());
            return "redirect:/admin/timetable/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating timetable entry: " + e.getMessage());
            return "redirect:/admin/timetable/edit/" + id;
        }

        return "redirect:/admin/timetable";
    }

    /**
     * Delete timetable entry
     */
    @PostMapping("/delete/{id}")
    public String deleteTimetableEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            timetableService.deleteTimetableEntry(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Timetable entry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting timetable entry: " + e.getMessage());
        }

        return "redirect:/admin/timetable";
    }

    /**
     * Check for conflicts (AJAX endpoint)
     */
    @PostMapping("/check-conflicts")
    @ResponseBody
    public List<TimetableService.TimetableConflict> checkConflicts(@RequestParam Long classroomId,
                                                                  @RequestParam Long teacherId,
                                                                  @RequestParam DayOfWeek dayOfWeek,
                                                                  @RequestParam Long timeSlotId) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
        
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        
        TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

        return timetableService.checkConflicts(classroom, teacher, dayOfWeek, timeSlot);
    }
}
