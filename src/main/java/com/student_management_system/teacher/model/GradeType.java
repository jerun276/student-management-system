package com.student_management_system.teacher.model;

public enum GradeType {
    ASSIGNMENT("Assignment"),
    EXAM("Exam"),
    QUIZ("Quiz"),
    PROJECT("Project"),
    MIDTERM("Midterm"),
    FINAL("Final Exam"),
    PARTICIPATION("Participation"),
    HOMEWORK("Homework"),
    LAB("Lab Work"),
    PRESENTATION("Presentation");
    
    private final String displayName;
    
    GradeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
