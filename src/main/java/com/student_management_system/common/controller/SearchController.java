package com.student_management_system.common.controller;

import com.student_management_system.common.dto.AdvancedSearchDto;
import com.student_management_system.common.dto.SearchResultDto;
import com.student_management_system.common.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Global search page
     */
    @GetMapping
    public String searchPage(@RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        
        if (q != null && !q.trim().isEmpty()) {
            try {
                Pageable pageable = PageRequest.of(page, size);
                Page<SearchResultDto> results = searchService.globalSearch(q.trim(), pageable);
                
                model.addAttribute("results", results);
                model.addAttribute("query", q);
                model.addAttribute("totalResults", results.getTotalElements());
                
                // Add search statistics
                model.addAttribute("searchStats", getSearchStatistics(results));
                
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Search failed: " + e.getMessage());
            }
        }
        
        model.addAttribute("searchDto", new AdvancedSearchDto());
        return "common/search/results";
    }

    /**
     * Advanced search page
     */
    @GetMapping("/advanced")
    public String advancedSearchPage(Model model) {
        model.addAttribute("searchDto", new AdvancedSearchDto());
        model.addAttribute("entityTypes", getAvailableEntityTypes());
        model.addAttribute("categories", getAvailableCategories());
        model.addAttribute("statuses", getAvailableStatuses());
        return "common/search/advanced";
    }

    /**
     * Process advanced search
     */
    @PostMapping("/advanced")
    public String processAdvancedSearch(@Valid @ModelAttribute("searchDto") AdvancedSearchDto searchDto,
                                      BindingResult bindingResult,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("entityTypes", getAvailableEntityTypes());
            model.addAttribute("categories", getAvailableCategories());
            model.addAttribute("statuses", getAvailableStatuses());
            return "common/search/advanced";
        }

        try {
            Sort sort = createSortFromDto(searchDto);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SearchResultDto> results = searchService.advancedSearch(searchDto, pageable);
            
            model.addAttribute("results", results);
            model.addAttribute("searchDto", searchDto);
            model.addAttribute("totalResults", results.getTotalElements());
            model.addAttribute("searchStats", getSearchStatistics(results));
            
            return "common/search/results";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Advanced search failed: " + e.getMessage());
            model.addAttribute("entityTypes", getAvailableEntityTypes());
            model.addAttribute("categories", getAvailableCategories());
            model.addAttribute("statuses", getAvailableStatuses());
            return "common/search/advanced";
        }
    }

    /**
     * Search suggestions API for autocomplete
     */
    @GetMapping("/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String q,
                                                           @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> suggestions = searchService.getSearchSuggestions(q, limit);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Quick search API for header search
     */
    @GetMapping("/quick")
    @ResponseBody
    public ResponseEntity<Page<SearchResultDto>> quickSearch(@RequestParam String q,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SearchResultDto> results = searchService.globalSearch(q, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Export search results
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportSearchResults(@RequestParam String q,
                                                     @RequestParam(defaultValue = "csv") String format) {
        try {
            // Implementation for exporting search results
            // This would integrate with the Data Export functionality
            return ResponseEntity.ok("Export functionality will be implemented");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Export failed: " + e.getMessage());
        }
    }

    // Helper methods
    private List<String> getAvailableEntityTypes() {
        return List.of("ASSIGNMENT", "ANNOUNCEMENT", "USER", "EVENT", "GRADE");
    }

    private List<String> getAvailableCategories() {
        return List.of("Academic", "Administrative", "Events", "General", "Emergency");
    }

    private List<String> getAvailableStatuses() {
        return List.of("Active", "Inactive", "Pending", "Completed", "Cancelled");
    }

    private Sort createSortFromDto(AdvancedSearchDto searchDto) {
        String sortBy = searchDto.getSortBy();
        String direction = searchDto.getSortDirection();
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                                     Sort.Direction.DESC : Sort.Direction.ASC;
        
        switch (sortBy) {
            case "date":
                return Sort.by(sortDirection, "createdDate");
            case "title":
                return Sort.by(sortDirection, "title");
            case "relevance":
            default:
                return Sort.by(Sort.Direction.DESC, "relevanceScore");
        }
    }

    private SearchStatistics getSearchStatistics(Page<SearchResultDto> results) {
        SearchStatistics stats = new SearchStatistics();
        
        // Count by entity type
        results.getContent().forEach(result -> {
            switch (result.getEntityType()) {
                case "ASSIGNMENT":
                    stats.incrementAssignmentCount();
                    break;
                case "ANNOUNCEMENT":
                    stats.incrementAnnouncementCount();
                    break;
                case "USER":
                    stats.incrementUserCount();
                    break;
                case "EVENT":
                    stats.incrementEventCount();
                    break;
                case "GRADE":
                    stats.incrementGradeCount();
                    break;
            }
        });
        
        return stats;
    }

    // Inner class for search statistics
    public static class SearchStatistics {
        private int assignmentCount = 0;
        private int announcementCount = 0;
        private int userCount = 0;
        private int eventCount = 0;
        private int gradeCount = 0;

        // Getters and increment methods
        public int getAssignmentCount() { return assignmentCount; }
        public int getAnnouncementCount() { return announcementCount; }
        public int getUserCount() { return userCount; }
        public int getEventCount() { return eventCount; }
        public int getGradeCount() { return gradeCount; }

        public void incrementAssignmentCount() { this.assignmentCount++; }
        public void incrementAnnouncementCount() { this.announcementCount++; }
        public void incrementUserCount() { this.userCount++; }
        public void incrementEventCount() { this.eventCount++; }
        public void incrementGradeCount() { this.gradeCount++; }
    }
}
