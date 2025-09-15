package com.student_management_system.principal.controller;

import com.student_management_system.principal.dto.SubjectPerformanceDto;
import com.student_management_system.principal.service.PrincipalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/principal") // Prefix for all API endpoints
public class PrincipalApiController {

    private final PrincipalService principalService;

    public PrincipalApiController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping("/performance-report")
    public ResponseEntity<List<SubjectPerformanceDto>> getPerformanceReport() {
        List<SubjectPerformanceDto> report = principalService.getSubjectPerformanceReport();
        return ResponseEntity.ok(report);
    }
}