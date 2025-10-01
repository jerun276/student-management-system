package com.student_management_system.teacher.model;

import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import com.student_management_system.common.model.Course;
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

    // NEW: Course-based relationship for the new academic structure
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy; // The teacher who uploaded it

    private LocalDateTime uploadDate;
}