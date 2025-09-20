package com.student_management_system.teacher.controller;

import com.student_management_system.teacher.service.TeacherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceController {

    private final TeacherService teacherService;

    public TeacherAttendanceController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String showSelectSubjectAndDatePage(Model model) {
        model.addAttribute("subjects", teacherService.getAllSubjects());
        return "teacher/attendance-select";
    }

    @GetMapping("/mark")
    public String showAttendanceSheet(
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        model.addAttribute("students", teacherService.getStudentsForAttendance());
        model.addAttribute("subject", teacherService.getAllSubjects().stream().filter(s -> s.getId().equals(subjectId)).findFirst().get());
        model.addAttribute("date", date);
        model.addAttribute("existingRecords", teacherService.getAttendanceRecordsForSubjectAndDate(subjectId, date));
        return "teacher/attendance-mark";
    }

    @PostMapping("/save")
    public String saveAttendance(
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Map<String, String> attendanceData) {
        teacherService.saveAttendance(subjectId, date, attendanceData);
        return "redirect:/teacher/dashboard";
    }
}