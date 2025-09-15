package com.student_management_system.common.service;

import com.student_management_system.common.model.Message;
import com.student_management_system.common.repository.MessageRepository;
import com.student_management_system.user_management.model.User;
import org.springframework.stereotype.Service;

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
}