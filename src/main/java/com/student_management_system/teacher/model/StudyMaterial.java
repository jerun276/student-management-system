package com.student_management_system.teacher.model;

import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class StudyMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String fileName; // The name of the file stored on the server

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy; // The teacher who uploaded it

    private LocalDateTime uploadDate;
}