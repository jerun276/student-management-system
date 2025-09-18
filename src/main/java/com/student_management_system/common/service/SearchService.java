package com.student_management_system.common.service;

import com.student_management_system.common.dto.SearchResultDto;
import com.student_management_system.common.dto.AdvancedSearchDto;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.principal.model.Announcement;
import com.student_management_system.principal.repository.AnnouncementRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.teacher.model.Grade;
import com.student_management_system.teacher.repository.GradeRepository;
import com.student_management_system.staff.model.Event;
import com.student_management_system.staff.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final EventRepository eventRepository;

    public SearchService(AssignmentRepository assignmentRepository,
                        AnnouncementRepository announcementRepository,
                        UserRepository userRepository,
                        GradeRepository gradeRepository,
                        EventRepository eventRepository) {
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Global search across all entities
     */
    public Page<SearchResultDto> globalSearch(String query, Pageable pageable) {
        List<SearchResultDto> results = new ArrayList<>();
        
        // Search assignments
        List<Assignment> assignments = assignmentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        results.addAll(assignments.stream()
            .map(this::convertAssignmentToSearchResult)
            .collect(Collectors.toList()));
        
        // Search announcements
        List<Announcement> announcements = announcementRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
        results.addAll(announcements.stream()
            .map(this::convertAnnouncementToSearchResult)
            .collect(Collectors.toList()));
        
        // Search users
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        results.addAll(users.stream()
            .map(this::convertUserToSearchResult)
            .collect(Collectors.toList()));
        
        // Search events
        List<Event> events = eventRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        results.addAll(events.stream()
            .map(this::convertEventToSearchResult)
            .collect(Collectors.toList()));
        
        // Sort by relevance (can be enhanced with scoring algorithm)
        results.sort((a, b) -> b.getRelevanceScore().compareTo(a.getRelevanceScore()));
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<SearchResultDto> pageContent = results.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, results.size());
    }

    /**
     * Advanced search with filters
     */
    public Page<SearchResultDto> advancedSearch(AdvancedSearchDto searchDto, Pageable pageable) {
        List<SearchResultDto> results = new ArrayList<>();
        
        // Search based on entity type
        if (searchDto.getEntityTypes().contains("ASSIGNMENT")) {
            results.addAll(searchAssignments(searchDto));
        }
        
        if (searchDto.getEntityTypes().contains("ANNOUNCEMENT")) {
            results.addAll(searchAnnouncements(searchDto));
        }
        
        if (searchDto.getEntityTypes().contains("USER")) {
            results.addAll(searchUsers(searchDto));
        }
        
        if (searchDto.getEntityTypes().contains("EVENT")) {
            results.addAll(searchEvents(searchDto));
        }
        
        if (searchDto.getEntityTypes().contains("GRADE")) {
            results.addAll(searchGrades(searchDto));
        }
        
        // Apply date filters
        if (searchDto.getDateFrom() != null || searchDto.getDateTo() != null) {
            results = results.stream()
                .filter(result -> isWithinDateRange(result, searchDto.getDateFrom(), searchDto.getDateTo()))
                .collect(Collectors.toList());
        }
        
        // Sort by relevance and date
        results.sort((a, b) -> {
            int relevanceCompare = b.getRelevanceScore().compareTo(a.getRelevanceScore());
            if (relevanceCompare != 0) return relevanceCompare;
            return b.getCreatedDate().compareTo(a.getCreatedDate());
        });
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<SearchResultDto> pageContent = results.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, results.size());
    }

    /**
     * Search suggestions for autocomplete
     */
    public List<String> getSearchSuggestions(String query, int limit) {
        List<String> suggestions = new ArrayList<>();
        
        // Get assignment titles
        List<Assignment> assignments = assignmentRepository.findTop5ByTitleContainingIgnoreCaseOrderByTitle(query);
        suggestions.addAll(assignments.stream().map(Assignment::getTitle).collect(Collectors.toList()));
        
        // Get announcement titles
        List<Announcement> announcements = announcementRepository.findTop5ByTitleContainingIgnoreCaseOrderByTitle(query);
        suggestions.addAll(announcements.stream().map(Announcement::getTitle).collect(Collectors.toList()));
        
        // Get user names
        List<User> users = userRepository.findTop5ByUsernameContainingIgnoreCaseOrderByUsername(query);
        suggestions.addAll(users.stream().map(User::getUsername).collect(Collectors.toList()));
        
        // Get event names
        List<Event> events = eventRepository.findTop5ByNameContainingIgnoreCaseOrderByName(query);
        suggestions.addAll(events.stream().map(Event::getName).collect(Collectors.toList()));
        
        return suggestions.stream()
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }

    // Private helper methods
    private List<SearchResultDto> searchAssignments(AdvancedSearchDto searchDto) {
        // Implementation for assignment-specific search
        List<Assignment> assignments = assignmentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            searchDto.getQuery(), searchDto.getQuery());
        return assignments.stream()
            .map(this::convertAssignmentToSearchResult)
            .collect(Collectors.toList());
    }

    private List<SearchResultDto> searchAnnouncements(AdvancedSearchDto searchDto) {
        List<Announcement> announcements = announcementRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            searchDto.getQuery(), searchDto.getQuery());
        return announcements.stream()
            .map(this::convertAnnouncementToSearchResult)
            .collect(Collectors.toList());
    }

    private List<SearchResultDto> searchUsers(AdvancedSearchDto searchDto) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchDto.getQuery(), searchDto.getQuery());
        return users.stream()
            .map(this::convertUserToSearchResult)
            .collect(Collectors.toList());
    }

    private List<SearchResultDto> searchEvents(AdvancedSearchDto searchDto) {
        List<Event> events = eventRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            searchDto.getQuery(), searchDto.getQuery());
        return events.stream()
            .map(this::convertEventToSearchResult)
            .collect(Collectors.toList());
    }

    private List<SearchResultDto> searchGrades(AdvancedSearchDto searchDto) {
        List<Grade> grades = gradeRepository.findByCommentsContainingIgnoreCase(searchDto.getQuery());
        return grades.stream()
            .map(this::convertGradeToSearchResult)
            .collect(Collectors.toList());
    }

    private boolean isWithinDateRange(SearchResultDto result, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate resultDate = result.getCreatedDate().toLocalDate();
        
        if (dateFrom != null && resultDate.isBefore(dateFrom)) {
            return false;
        }
        
        if (dateTo != null && resultDate.isAfter(dateTo)) {
            return false;
        }
        
        return true;
    }

    // Conversion methods
    private SearchResultDto convertAssignmentToSearchResult(Assignment assignment) {
        SearchResultDto result = new SearchResultDto();
        result.setId(assignment.getId());
        result.setTitle(assignment.getTitle());
        result.setDescription(assignment.getDescription());
        result.setEntityType("ASSIGNMENT");
        result.setUrl("/teacher/assignments/" + assignment.getId());
        result.setCreatedDate(LocalDateTime.now()); // You might want to add createdDate to Assignment
        result.setRelevanceScore(calculateRelevanceScore(assignment.getTitle(), assignment.getDescription()));
        return result;
    }

    private SearchResultDto convertAnnouncementToSearchResult(Announcement announcement) {
        SearchResultDto result = new SearchResultDto();
        result.setId(announcement.getId());
        result.setTitle(announcement.getTitle());
        result.setDescription(announcement.getContent());
        result.setEntityType("ANNOUNCEMENT");
        result.setUrl("/principal/announcements/" + announcement.getId());
        result.setCreatedDate(announcement.getPublishedDate());
        result.setRelevanceScore(calculateRelevanceScore(announcement.getTitle(), announcement.getContent()));
        return result;
    }

    private SearchResultDto convertUserToSearchResult(User user) {
        SearchResultDto result = new SearchResultDto();
        result.setId(user.getId());
        result.setTitle(user.getUsername());
        result.setDescription("User - " + user.getRole().name());
        result.setEntityType("USER");
        result.setUrl("/admin/users/" + user.getId());
        result.setCreatedDate(LocalDateTime.now()); // You might want to add createdDate to User
        result.setRelevanceScore(calculateRelevanceScore(user.getUsername(), user.getEmail()));
        return result;
    }

    private SearchResultDto convertEventToSearchResult(Event event) {
        SearchResultDto result = new SearchResultDto();
        result.setId(event.getId());
        result.setTitle(event.getName());
        result.setDescription(event.getDescription());
        result.setEntityType("EVENT");
        result.setUrl("/staff/events/" + event.getId());
        result.setCreatedDate(event.getCreatedDate());
        result.setRelevanceScore(calculateRelevanceScore(event.getName(), event.getDescription()));
        return result;
    }

    private SearchResultDto convertGradeToSearchResult(Grade grade) {
        SearchResultDto result = new SearchResultDto();
        result.setId(grade.getId());
        result.setTitle("Grade: " + grade.getGradeValue());
        result.setDescription("Subject: " + grade.getSubject().getName() + " - Student: " + grade.getStudent().getUsername());
        result.setEntityType("GRADE");
        result.setUrl("/teacher/grades/" + grade.getId());
        result.setCreatedDate(grade.getGradedDate());
        result.setRelevanceScore(calculateRelevanceScore(grade.getGradeValue(), grade.getComments()));
        return result;
    }

    private Double calculateRelevanceScore(String title, String content) {
        // Simple relevance scoring - can be enhanced with more sophisticated algorithms
        double score = 0.0;
        
        if (title != null) {
            score += title.length() * 0.1; // Longer titles might be more relevant
        }
        
        if (content != null) {
            score += content.length() * 0.01; // Content relevance
        }
        
        return Math.min(score, 100.0); // Cap at 100
    }
}
