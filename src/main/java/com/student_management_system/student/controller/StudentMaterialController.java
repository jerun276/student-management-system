package com.student_management_system.student.controller;

import com.student_management_system.common.service.FileStorageService;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.service.StudentService;
import com.student_management_system.teacher.model.StudyMaterial;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/student/materials")
public class StudentMaterialController {

    private final StudentService studentService;
    private final SubjectRepository subjectRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public StudentMaterialController(StudentService studentService, SubjectRepository subjectRepository, FileStorageService fileStorageService, UserRepository userRepository) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listAllMaterials(Model model) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get all subjects (for now, we'll show all subjects - this could be filtered by student's enrollment)
        List<Subject> allSubjects = subjectRepository.findAll();
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("subjects", allSubjects);
        
        return "student/materials";
    }

    @GetMapping("/subject/{id}")
    public String listMaterialsForSubject(@PathVariable Long id, Model model) {
        Subject subject = subjectRepository.findById(id).orElseThrow(() -> new RuntimeException("Subject not found"));
        model.addAttribute("subject", subject);
        model.addAttribute("materials", studentService.getStudyMaterialsForSubject(id));
        return "student/materials-list";
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Fallback
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}