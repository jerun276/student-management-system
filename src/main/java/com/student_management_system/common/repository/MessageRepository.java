package com.student_management_system.common.repository;

import com.student_management_system.common.model.Message;
import com.student_management_system.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Find all messages sent TO a specific user (their inbox)
    List<Message> findByRecipientOrderBySentDateDesc(User recipient);

    // Find all messages sent BY a specific user (their sent items)
    List<Message> findBySenderOrderBySentDateDesc(User sender);
}