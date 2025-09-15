package com.student_management_system.common.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private String subject;

    @Lob
    private String content;

    private LocalDateTime sentDate;

    // This helps the recipient know if they've seen the message
    private boolean isRead = false;
}