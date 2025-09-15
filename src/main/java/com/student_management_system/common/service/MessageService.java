package com.student_management_system.common.service;

import com.student_management_system.common.model.Message;
import com.student_management_system.common.repository.MessageRepository;
import com.student_management_system.user_management.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void sendMessage(User sender, User recipient, String subject, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setSubject(subject);
        message.setContent(content);
        message.setSentDate(LocalDateTime.now());
        message.setRead(false);
        messageRepository.save(message);
    }

    public List<Message> getInboxForUser(User user) {
        return messageRepository.findByRecipientOrderBySentDateDesc(user);
    }

    // Get a single message and mark it as read
    @Transactional
    public Message findMessageByIdAndMarkAsRead(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Security check: Only the recipient can mark a message as read
        if (!message.getRecipient().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this message.");
        }

        // Mark as read and save
        if (!message.isRead()) {
            message.setRead(true);
            messageRepository.save(message);
        }

        return message;
    }
}