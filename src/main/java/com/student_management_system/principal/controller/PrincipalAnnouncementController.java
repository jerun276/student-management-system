package com.student_management_system.principal.controller;

import com.student_management_system.principal.dto.AnnouncementDto;
import com.student_management_system.principal.model.Announcement;
import com.student_management_system.principal.service.PrincipalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/principal/announcements")
public class PrincipalAnnouncementController {

    private final PrincipalService principalService;

    public PrincipalAnnouncementController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping
    public String listAnnouncements(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "publishedDate") String sortBy,
                                   @RequestParam(defaultValue = "desc") String sortDir,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String searchTerm,
                                   Model model) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Announcement> announcementPage;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                announcementPage = principalService.searchAnnouncements(searchTerm, pageable);
            } else if (category != null && !category.trim().isEmpty()) {
                announcementPage = principalService.getAnnouncementsByCategory(category, pageable);
            } else {
                announcementPage = principalService.getAllAnnouncementsPaginated(pageable);
            }
            
            model.addAttribute("announcementPage", announcementPage);
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("currentCategory", category);
            model.addAttribute("currentSearchTerm", searchTerm);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSortDir", sortDir);
            
            return "principal/announcements/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading announcements: " + e.getMessage());
            return "principal/announcements/list";
        }
    }

    @GetMapping("/create")
    public String showCreateAnnouncementForm(Model model) {
        AnnouncementDto announcementDto = new AnnouncementDto();
        model.addAttribute("announcementDto", announcementDto);
        model.addAttribute("categories", principalService.getAnnouncementCategories());
        model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
        return "principal/announcements/create";
    }

    @PostMapping("/create")
    public String createAnnouncement(@Valid @ModelAttribute("announcementDto") AnnouncementDto announcementDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
            return "principal/announcements/create";
        }

        try {
            principalService.createAnnouncement(announcementDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement created successfully!");
            return "redirect:/principal/announcements";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create announcement: " + e.getMessage());
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
            return "principal/announcements/create";
        }
    }

    @GetMapping("/{id}")
    public String viewAnnouncement(@PathVariable Long id, Model model) {
        try {
            Announcement announcement = principalService.getAnnouncementById(id);
            model.addAttribute("announcement", announcement);
            return "principal/announcements/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Announcement not found");
            return "redirect:/principal/announcements";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditAnnouncementForm(@PathVariable Long id, Model model) {
        try {
            Announcement announcement = principalService.getAnnouncementById(id);
            AnnouncementDto announcementDto = principalService.convertAnnouncementToDto(announcement);
            
            model.addAttribute("announcementDto", announcementDto);
            model.addAttribute("announcementId", id);
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
            return "principal/announcements/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Announcement not found");
            return "redirect:/principal/announcements";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateAnnouncement(@PathVariable Long id,
                                   @Valid @ModelAttribute("announcementDto") AnnouncementDto announcementDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("announcementId", id);
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
            return "principal/announcements/edit";
        }

        try {
            principalService.updateAnnouncement(id, announcementDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement updated successfully!");
            return "redirect:/principal/announcements/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update announcement: " + e.getMessage());
            model.addAttribute("announcementId", id);
            model.addAttribute("categories", principalService.getAnnouncementCategories());
            model.addAttribute("priorities", new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
            return "principal/announcements/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, 
                                   Principal principal, 
                                   RedirectAttributes redirectAttributes) {
        try {
            principalService.deleteAnnouncement(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete announcement: " + e.getMessage());
        }
        return "redirect:/principal/announcements";
    }

    @PostMapping("/{id}/archive")
    public String archiveAnnouncement(@PathVariable Long id, 
                                    Principal principal, 
                                    RedirectAttributes redirectAttributes) {
        try {
            principalService.archiveAnnouncement(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement archived successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to archive announcement: " + e.getMessage());
        }
        return "redirect:/principal/announcements/" + id;
    }

    @PostMapping("/{id}/unarchive")
    public String unarchiveAnnouncement(@PathVariable Long id, 
                                      Principal principal, 
                                      RedirectAttributes redirectAttributes) {
        try {
            principalService.unarchiveAnnouncement(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement restored successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to restore announcement: " + e.getMessage());
        }
        return "redirect:/principal/announcements/" + id;
    }

    @PostMapping("/{id}/pin")
    public String pinAnnouncement(@PathVariable Long id, 
                                Principal principal, 
                                RedirectAttributes redirectAttributes) {
        try {
            principalService.pinAnnouncement(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement pinned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to pin announcement: " + e.getMessage());
        }
        return "redirect:/principal/announcements/" + id;
    }

    @PostMapping("/{id}/unpin")
    public String unpinAnnouncement(@PathVariable Long id, 
                                  Principal principal, 
                                  RedirectAttributes redirectAttributes) {
        try {
            principalService.unpinAnnouncement(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement unpinned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unpin announcement: " + e.getMessage());
        }
        return "redirect:/principal/announcements/" + id;
    }

    @GetMapping("/archived")
    public String listArchivedAnnouncements(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());
            Page<Announcement> archivedPage = principalService.getArchivedAnnouncements(pageable);
            
            model.addAttribute("announcementPage", archivedPage);
            model.addAttribute("isArchivedView", true);
            return "principal/announcements/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading archived announcements: " + e.getMessage());
            return "principal/announcements/list";
        }
    }
}
