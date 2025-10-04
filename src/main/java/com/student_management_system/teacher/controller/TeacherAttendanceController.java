package com.student_management_system.teacher.controller;

import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.repository.ClassroomRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceController {

    private final TeacherService teacherService;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public TeacherAttendanceController(TeacherService teacherService, 
                                     ClassroomRepository classroomRepository,
                                     UserRepository userRepository) {
        this.teacherService = teacherService;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showSelectDatePage(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Find classroom where this teacher is the class teacher
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        if (assignedClassroom == null) {
            model.addAttribute("errorMessage", "You are not assigned as a class teacher to any classroom. Please contact administration.");
            return "teacher/attendance-select";
        }

        model.addAttribute("classroom", assignedClassroom);
        model.addAttribute("teacher", teacher);
        return "teacher/attendance-select";
    }

    @GetMapping("/mark")
    public String showAttendanceSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Validate date - only prevent future dates
        LocalDate today = LocalDate.now();
        
        if (date.isAfter(today)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Cannot take attendance for future dates. Please select today or any past date.");
            return "redirect:/teacher/attendance";
        }
        
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Find classroom where this teacher is the class teacher
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        if (assignedClassroom == null) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "You are not assigned as a class teacher to any classroom.");
            return "redirect:/teacher/attendance";
        }

        // Get students enrolled in this classroom
        List<User> students = teacherService.getStudentsInClassroom(assignedClassroom.getId());
        
        model.addAttribute("students", students);
        model.addAttribute("classroom", assignedClassroom);
        model.addAttribute("date", date);
        model.addAttribute("existingRecords", 
            teacherService.getAttendanceRecordsForClassroomAndDate(assignedClassroom.getId(), date));
        
        return "teacher/attendance-mark";
    }

    @PostMapping("/save")
    public String saveAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Map<String, String> attendanceData,
            RedirectAttributes redirectAttributes) {
        
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Find classroom where this teacher is the class teacher
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        if (assignedClassroom == null) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "You are not assigned as a class teacher to any classroom.");
            return "redirect:/teacher/attendance";
        }

        try {
            teacherService.saveClassroomAttendance(assignedClassroom.getId(), date, attendanceData);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Attendance saved successfully for " + assignedClassroom.getFullName() + " on " + date);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to save attendance: " + e.getMessage());
        }
        
        return "redirect:/teacher/dashboard";
    }
}