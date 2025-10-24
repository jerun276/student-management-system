package com.student_management_system.common.controller;

import com.student_management_system.common.service.PublicService;
import com.student_management_system.principal.model.Announcement;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    private final PublicService publicService;
    
    public GlobalControllerAdvice(PublicService publicService) {
        this.publicService = publicService;
    }
    
    @ModelAttribute("latestAnnouncement")
    public Announcement getLatestAnnouncement() {
        Optional<Announcement> announcement = publicService.getLatestAnnouncement();
        return announcement.orElse(null);
    }
}
