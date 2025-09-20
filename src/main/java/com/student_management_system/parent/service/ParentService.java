package com.student_management_system.parent.service;

import com.student_management_system.parent.dto.ChildDataDto;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.teacher.repository.AttendanceRecordRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParentService {

    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public ParentService(UserRepository userRepository, AssignmentRepository assignmentRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<ChildDataDto> getChildrenDashboardData() {
        User parent = getCurrentUser();
        List<ChildDataDto> childrenData = new ArrayList<>();

        for (User child : parent.getChildren()) {
            ChildDataDto childDto = new ChildDataDto();
            childDto.setUsername(child.getUsername());
            childDto.setAssignments(assignmentRepository.findByUserOrderByDueDateAsc(child));
            childDto.setAttendanceRecords(attendanceRecordRepository.findByStudentOrderByDateDesc(child));
            childrenData.add(childDto);
        }
        return childrenData;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsernameWithChildren(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    // Get a list of all unique teachers for the parent's children
    @Transactional(readOnly = true)
    public Set<User> getTeachersForChildren() {
        User parent = getCurrentUser();
        return parent.getChildren().stream()
                .flatMap(child -> child.getAssignments().stream())
                .map(Assignment::getTeacher)
                .collect(Collectors.toSet());
    }
}