package com.student_management_system.principal.repository;

import com.student_management_system.principal.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Find all announcements, ordered by the most recently published
    List<Announcement> findAllByOrderByPublishedDateDesc();

    // Find the single most recent announcement
    Optional<Announcement> findFirstByOrderByPublishedDateDesc();
}