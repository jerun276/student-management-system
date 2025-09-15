package com.student_management_system.student.controller;

import com.student_management_system.common.service.FileStorageService;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/student/materials")
public class StudentMaterialController {

    private final StudentService studentService;
    private final SubjectRepository subjectRepository; // For getting subject name
    private final FileStorageService fileStorageService;

    public StudentMaterialController(StudentService studentService, SubjectRepository subjectRepository, FileStorageService fileStorageService) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.fileStorageService = fileStorageService;
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