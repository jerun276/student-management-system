package com.student_management_system.common.controller;

import com.student_management_system.common.service.PublicService;
import com.student_management_system.principal.model.Announcement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final PublicService publicService;

    public PublicApiController(PublicService publicService) {
        this.publicService = publicService;
    }

    @GetMapping("/latest-announcement")
    public ResponseEntity<Announcement> getLatestAnnouncement() {
        return publicService.getLatestAnnouncement()
                .map(ResponseEntity::ok) // If announcement exists, return it with 200 OK
                .orElse(ResponseEntity.noContent().build()); // If not, return 204 No Content
    }
}