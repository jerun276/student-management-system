package com.student_management_system.common.model;

/**
 * Enum representing different types of time slots in the school schedule
 */
public enum TimeSlotType {
    REGULAR("Regular Class Period"),
    BREAK("Break Time"),
    LUNCH("Lunch Break"),
    ASSEMBLY("Assembly/Special Event"),
    STUDY_HALL("Study Hall"),
    EXTRA_CURRICULAR("Extra-curricular Activity");

    private final String displayName;

    TimeSlotType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
