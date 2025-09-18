package com.student_management_system.principal.repository;

import com.student_management_system.principal.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Find all announcements, ordered by the most recently published
    List<Announcement> findAllByOrderByPublishedDateDesc();

    // Find the single most recent announcement
    Optional<Announcement> findFirstByOrderByPublishedDateDesc();
    
    // Find active announcements
    List<Announcement> findByIsActiveTrueOrderByPublishedDateDesc();
    
    // Find archived announcements
    Page<Announcement> findByIsActiveFalseOrderByPublishedDateDesc(Pageable pageable);
    
    // Find announcements by category
    Page<Announcement> findByCategoryOrderByPublishedDateDesc(String category, Pageable pageable);
    
    // Find pinned announcements
    List<Announcement> findByIsPinnedTrueOrderByPublishedDateDesc();
    
    // Find top announcements for suggestions
    List<Announcement> findTop5ByTitleContainingIgnoreCaseOrderByTitle(String title);
    
    // Find announcements by priority
    List<Announcement> findByPriorityOrderByPublishedDateDesc(String priority);
    
    // Find active announcements that haven't expired
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND (a.expiryDate IS NULL OR a.expiryDate > :currentDate) ORDER BY a.publishedDate DESC")
    List<Announcement> findActiveNonExpiredAnnouncements(@Param("currentDate") LocalDateTime currentDate);
}