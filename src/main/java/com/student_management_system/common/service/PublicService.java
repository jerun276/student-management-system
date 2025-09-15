package com.student_management_system.common.service;

import com.student_management_system.principal.model.Announcement;
import com.student_management_system.principal.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PublicService {
    private final AnnouncementRepository announcementRepository;

    public PublicService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public Optional<Announcement> getLatestAnnouncement() {
        return announcementRepository.findFirstByOrderByPublishedDateDesc();
    }
}