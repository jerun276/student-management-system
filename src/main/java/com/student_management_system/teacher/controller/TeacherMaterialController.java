package com.student_management_system.teacher.controller;

import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.teacher.model.StudyMaterial;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/materials")
public class TeacherMaterialController {

    private final TeacherService teacherService;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public TeacherMaterialController(TeacherService teacherService, SubjectRepository subjectRepository, UserRepository userRepository) {
        this.teacherService = teacherService;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("subjects", teacherService.getAllSubjects());
        return "teacher/upload-material";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam("subjectId") Long subjectId,
                               @RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/teacher/materials/upload";
        }
        try {
            teacherService.uploadStudyMaterial(title, description, subjectId, file);
            redirectAttributes.addFlashAttribute("successMessage", "File uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not upload the file: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }
}