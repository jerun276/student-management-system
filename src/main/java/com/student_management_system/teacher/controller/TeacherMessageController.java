package com.student_management_system.teacher.controller;

import com.student_management_system.common.service.MessageService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.student_management_system.common.model.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/messages")
public class TeacherMessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    public TeacherMessageController(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showMessagesPage(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("inboxMessages", messageService.getInboxForUser(currentUser));
        // We can add a "sent" box later if needed
        return "teacher/messages";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
    }

    // Show the reply form
    @GetMapping("/{id}/reply")
    public String showReplyForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Message originalMessage = messageService.findMessageByIdAndMarkAsRead(id, currentUser);

        model.addAttribute("originalMessage", originalMessage);

        return "teacher/message-reply";
    }

    // Handle sending the reply
    @PostMapping("/{id}/reply")
    public String sendReply(@PathVariable Long id,
                            @RequestParam String content,
                            RedirectAttributes redirectAttributes) {
        try {
            User teacher = getCurrentUser();
            Message originalMessage = messageService.findMessageByIdAndMarkAsRead(id, teacher); // Find original to get recipient
            User parent = originalMessage.getSender(); // The sender of the original message is the new recipient
            String subject = "Re: " + originalMessage.getSubject();

            messageService.sendMessage(teacher, parent, subject, content);
            redirectAttributes.addFlashAttribute("successMessage", "Reply sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reply: " + e.getMessage());
        }

        return "redirect:/teacher/messages";
    }
}