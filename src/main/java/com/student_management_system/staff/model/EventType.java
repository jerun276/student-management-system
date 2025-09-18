package com.student_management_system.staff.model;

public enum EventType {
    GENERAL("General"),
    ACADEMIC("Academic"),
    SPORTS("Sports"),
    CULTURAL("Cultural"),
    MEETING("Meeting"),
    WORKSHOP("Workshop"),
    SEMINAR("Seminar"),
    CONFERENCE("Conference"),
    EXAMINATION("Examination"),
    HOLIDAY("Holiday"),
    ASSEMBLY("Assembly"),
    FIELD_TRIP("Field Trip"),
    PARENT_MEETING("Parent Meeting"),
    GRADUATION("Graduation"),
    ORIENTATION("Orientation");
    
    private final String displayName;
    
    EventType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
