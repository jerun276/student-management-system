package com.student_management_system.staff.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @Lob
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime eventDateTime;
    
    private LocalDateTime endDateTime;
    
    @Column(nullable = false)
    private String location;
    
    private int maxAttendees = 0; // 0 means unlimited
    
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PLANNED;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType = EventType.GENERAL;
    
    private BigDecimal registrationFee = BigDecimal.ZERO;
    
    private LocalDateTime registrationDeadline;
    
    private boolean requiresApproval = false;
    
    private boolean isPublic = true;
    
    private String targetAudience; // e.g., "All Students", "Grade 10", "Teachers"
    
    private String organizer;
    
    private String contactEmail;
    
    private String contactPhone;
    
    @Lob
    private String requirements; // What participants need to bring/prepare
    
    @Lob
    private String agenda; // Event schedule/agenda
    
    private LocalDateTime createdDate;
    
    private LocalDateTime lastModifiedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventRegistration> registrations;
    
    // Helper methods
    public int getCurrentAttendeeCount() {
        return registrations != null ? registrations.size() : 0;
    }
    
    public boolean isRegistrationOpen() {
        return status == EventStatus.OPEN_FOR_REGISTRATION && 
               (registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline)) &&
               (maxAttendees == 0 || getCurrentAttendeeCount() < maxAttendees);
    }
    
    public boolean isFull() {
        return maxAttendees > 0 && getCurrentAttendeeCount() >= maxAttendees;
    }
}