package com.student_management_system.parent.controller;

import com.student_management_system.common.service.MessageService;
import com.student_management_system.parent.dto.ComposeMessageDto;
import com.student_management_system.parent.service.ParentService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/parent/messages")
public class ParentMessageController {

    private final ParentService parentService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public ParentMessageController(ParentService parentService, MessageService messageService, UserRepository userRepository) {
        this.parentService = parentService;
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    // This method will show the main messaging page with inbox and compose form
    @GetMapping
    public String showMessagesPage(Model model) {
        User currentUser = getCurrentUser();

        model.addAttribute("inboxMessages", messageService.getInboxForUser(currentUser));
        model.addAttribute("teachers", parentService.getTeachersForChildren());

        // Ensure the composeMessage object is available for the form
        if (!model.containsAttribute("composeMessage")) {
            model.addAttribute("composeMessage", new ComposeMessageDto());
        }

        return "parent/messages";
    }

    // This method handles the form submission for sending a new message
    @PostMapping("/send")
    public String sendMessage(@Valid @ModelAttribute("composeMessage") ComposeMessageDto composeMessageDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // If there are errors, pass them back to the view
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.composeMessage", bindingResult);
            redirectAttributes.addFlashAttribute("composeMessage", composeMessageDto);
            return "redirect:/parent/messages";
        }

        try {
            User sender = getCurrentUser();
            User recipient = userRepository.findById(composeMessageDto.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));

            messageService.sendMessage(sender, recipient, composeMessageDto.getSubject(), composeMessageDto.getContent());
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
        }

        return "redirect:/parent/messages";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
    }
}