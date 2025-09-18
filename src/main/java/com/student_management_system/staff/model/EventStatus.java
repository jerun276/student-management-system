package com.student_management_system.staff.model;

public enum EventStatus {
    PLANNED("Planned"),
    OPEN_FOR_REGISTRATION("Open for Registration"),
    REGISTRATION_CLOSED("Registration Closed"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    POSTPONED("Postponed");
    
    private final String displayName;
    
    EventStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
