package com.student_management_system.admin.controller;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.repository.ClassroomRepository;
import com.student_management_system.common.service.TimeSlotService;
import com.student_management_system.common.service.TimetableService;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

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
    private final TimetableEntryRepository timetableEntryRepository;

    public TimetableController(TimetableService timetableService,
                             TimeSlotService timeSlotService,
                             ClassroomRepository classroomRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository,
                             TimetableEntryRepository timetableEntryRepository) {
        this.timetableService = timetableService;
        this.timeSlotService = timeSlotService;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.timetableEntryRepository = timetableEntryRepository;
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
    public ResponseEntity<?> getTimetableEntries(@RequestParam DayOfWeek day, 
                                               @RequestParam Long timeSlotId) {
        try {
            System.out.println("Requesting timetable entries for day: " + day + ", timeSlotId: " + timeSlotId);
            
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
                .orElse(null);
            
            if (timeSlot == null) {
                System.err.println("Time slot not found with ID: " + timeSlotId);
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            System.out.println("Found timeSlot: " + timeSlot.getName() + " (ID: " + timeSlot.getId() + ")");
            
            List<TimetableEntry> entries = timetableService.getTimetableEntriesForSlot(day, timeSlot);
            System.out.println("Found " + (entries != null ? entries.size() : 0) + " entries for " + day + " " + timeSlot.getName());
            
            // Create a simple response to avoid JSON serialization issues
            List<Map<String, Object>> response = new ArrayList<>();
            if (entries != null) {
                for (TimetableEntry entry : entries) {
                    try {
                        Map<String, Object> entryMap = new HashMap<>();
                        entryMap.put("id", entry.getId());
                        
                        // Subject object
                        Map<String, Object> subject = new HashMap<>();
                        subject.put("name", entry.getSubject() != null ? entry.getSubject().getName() : "Unknown");
                        entryMap.put("subject", subject);
                        
                        // Teacher object
                        Map<String, Object> teacher = new HashMap<>();
                        teacher.put("firstName", entry.getTeacher() != null ? entry.getTeacher().getFirstName() : "Unknown");
                        teacher.put("lastName", entry.getTeacher() != null ? entry.getTeacher().getLastName() : "Teacher");
                        entryMap.put("teacher", teacher);
                        
                        // Classroom object
                        Map<String, Object> classroom = new HashMap<>();
                        if (entry.getClassroom() != null) {
                            try {
                                // Try to get full classroom name with grade info
                                String classroomName = entry.getClassroom().getName(); // e.g., "A"
                                String fullName = "Grade 10-" + classroomName; // Default fallback
                                
                                // Try to access grade level safely
                                if (entry.getClassroom().getGradeLevel() != null) {
                                    String gradeName = entry.getClassroom().getGradeLevel().getName(); // e.g., "Grade 10"
                                    fullName = gradeName + "-" + classroomName; // e.g., "Grade 10-A"
                                }
                                
                                classroom.put("fullName", fullName);
                            } catch (Exception e) {
                                // Fallback if there are lazy loading issues
                                classroom.put("fullName", "Grade 10-" + entry.getClassroom().getName());
                            }
                        } else {
                            classroom.put("fullName", "Unknown Classroom");
                        }
                        entryMap.put("classroom", classroom);
                        
                        entryMap.put("location", entry.getLocation());
                        response.add(entryMap);
                    } catch (Exception entryError) {
                        System.err.println("Error processing entry: " + entryError.getMessage());
                        // Skip this entry and continue
                    }
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching timetable entries for day " + day + " and timeSlot " + timeSlotId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Show form to create new timetable entry
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Subject> subjects = subjectRepository.findByIsActiveTrue();
        List<User> teachers = userRepository.findByRole(com.student_management_system.user_management.model.Role.ROLE_TEACHER);
        
        // Create a map of subject ID to list of teacher IDs who can teach that subject
        // For now, we'll assume all teachers can teach all subjects since we don't have 
        // a proper subject-teacher relationship in the current data model
        Map<Long, List<Long>> subjectTeacherMap = new HashMap<>();
        for (Subject subject : subjects) {
            List<Long> teacherIds = teachers.stream().map(User::getId).collect(java.util.stream.Collectors.toList());
            subjectTeacherMap.put(subject.getId(), teacherIds);
        }
        
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("subjectTeacherMap", subjectTeacherMap);
        model.addAttribute("timeSlots", timeSlotService.getRegularPeriods());
        
        // Only show weekdays (Monday to Friday) for school timetable
        DayOfWeek[] weekDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        model.addAttribute("weekDays", weekDays);

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
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();
        DayOfWeek[] weekDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        
        // Create a simplified grid structure for the template
        Map<String, TimetableEntry> timetableGrid = new HashMap<>();
        int scheduledPeriods = 0;
        
        for (DayOfWeek day : weekDays) {
            List<TimetableEntry> dayEntries = timetable.get(day);
            if (dayEntries != null) {
                scheduledPeriods += dayEntries.size();
                for (TimetableEntry entry : dayEntries) {
                    String key = day.name() + "_" + entry.getTimeSlot().getId();
                    timetableGrid.put(key, entry);
                }
            }
        }
        
        // Calculate statistics
        int totalPossiblePeriods = timeSlots.size() * weekDays.length; // Total possible periods per week
        
        // Create daily workload map for easier template access
        Map<String, Integer> dailyWorkload = new HashMap<>();
        for (DayOfWeek day : weekDays) {
            List<TimetableEntry> dayEntries = timetable.get(day);
            int dayPeriods = (dayEntries != null) ? dayEntries.size() : 0;
            dailyWorkload.put(day.name(), dayPeriods);
        }
        
        model.addAttribute("classroom", classroom);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timetableGrid", timetableGrid);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("totalPeriods", totalPossiblePeriods);
        model.addAttribute("scheduledPeriods", scheduledPeriods);
        model.addAttribute("dailyWorkload", dailyWorkload);

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
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();
        DayOfWeek[] weekDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        
        // Create a simplified grid structure for the template
        Map<String, TimetableEntry> timetableGrid = new HashMap<>();
        int teachingPeriods = 0;
        
        for (DayOfWeek day : weekDays) {
            List<TimetableEntry> dayEntries = timetable.get(day);
            if (dayEntries != null) {
                teachingPeriods += dayEntries.size();
                for (TimetableEntry entry : dayEntries) {
                    String key = day.name() + "_" + entry.getTimeSlot().getId();
                    timetableGrid.put(key, entry);
                }
            }
        }
        
        // Calculate statistics
        int totalPeriods = timeSlots.size() * weekDays.length; // Total possible periods per week
        
        // Create daily workload map for easier template access
        Map<String, Integer> dailyWorkload = new HashMap<>();
        for (DayOfWeek day : weekDays) {
            List<TimetableEntry> dayEntries = timetable.get(day);
            int dayPeriods = (dayEntries != null) ? dayEntries.size() : 0;
            dailyWorkload.put(day.name(), dayPeriods);
        }
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timetableGrid", timetableGrid);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("totalPeriods", totalPeriods);
        model.addAttribute("teachingPeriods", teachingPeriods);
        model.addAttribute("dailyWorkload", dailyWorkload);

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
        
        // Only show weekdays (Monday to Friday) for school timetable
        DayOfWeek[] weekDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        model.addAttribute("weekDays", weekDays);

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
    public String deleteTimetableEntry(@PathVariable Long id, 
                                     @RequestParam(required = false) String returnTo,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Get the entry details before deletion for smart redirect
            TimetableEntry entry = timetableEntryRepository.findById(id).orElse(null);
            
            timetableService.deleteTimetableEntry(id);
            redirectAttributes.addFlashAttribute("successMessage", "Timetable entry deleted successfully!");
            
            // Smart redirect based on returnTo parameter or entry details
            if (returnTo != null && !returnTo.isEmpty()) {
                return "redirect:" + returnTo;
            } else if (entry != null && entry.getTeacher() != null) {
                // If deleted from teacher timetable, redirect back to teacher timetable
                return "redirect:/admin/timetable/teacher/" + entry.getTeacher().getId();
            } else if (entry != null && entry.getClassroom() != null) {
                // If deleted from classroom timetable, redirect back to classroom timetable
                return "redirect:/admin/timetable/classroom/" + entry.getClassroom().getId();
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting timetable entry: " + e.getMessage());
        }
        
        // Default fallback to dashboard
        return "redirect:/admin/timetable";
    }

    /**
     * Check for conflicts (AJAX endpoint)
     */
    @PostMapping("/check-conflicts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkConflicts(@RequestParam Long classroomId,
                                                            @RequestParam Long teacherId,
                                                            @RequestParam DayOfWeek dayOfWeek,
                                                            @RequestParam Long timeSlotId) {
        try {
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

            List<TimetableService.TimetableConflict> conflictObjects = timetableService.checkConflicts(classroom, teacher, dayOfWeek, timeSlot);
            List<String> conflicts = conflictObjects.stream()
                .map(TimetableService.TimetableConflict::getDescription)
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasConflicts", !conflicts.isEmpty());
            response.put("conflicts", conflicts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get teachers for a specific subject (AJAX endpoint)
     */
    @GetMapping("/teachers-by-subject/{subjectId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTeachersBySubject(@PathVariable Long subjectId) {
        try {
            // Get subject with teachers
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            
            // Get teachers from the many-to-many relationship
            List<User> teachers = new ArrayList<>();
            if (subject.getTeachers() != null && !subject.getTeachers().isEmpty()) {
                teachers = new ArrayList<>(subject.getTeachers());
            } else {
                // Fallback: Get unique teachers from existing timetable entries if no direct relationships exist
                List<TimetableEntry> entriesForSubject = timetableEntryRepository.findBySubjectId(subjectId);
                teachers = entriesForSubject.stream()
                    .map(TimetableEntry::getTeacher)
                    .filter(teacher -> teacher != null)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Convert to response format
            List<Map<String, Object>> teacherList = teachers.stream()
                .map(teacher -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", teacher.getId());
                    teacherMap.put("name", teacher.getFirstName() + " " + teacher.getLastName());
                    teacherMap.put("email", teacher.getEmail());
                    return teacherMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(teacherList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }
}
