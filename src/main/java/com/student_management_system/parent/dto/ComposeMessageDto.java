package com.student_management_system.parent.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComposeMessageDto {
    @NotNull(message = "You must select a recipient.")
    private Long recipientId; // The ID of the teacher to send the message to

    @NotEmpty(message = "Subject cannot be empty.")
    private String subject;

    @NotEmpty(message = "Message content cannot be empty.")
    private String content;
}