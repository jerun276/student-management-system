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
}