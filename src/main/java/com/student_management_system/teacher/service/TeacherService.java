package com.student_management_system.teacher.service;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.teacher.model.AttendanceRecord;
import com.student_management_system.teacher.model.AttendanceStatus;
import com.student_management_system.teacher.repository.AttendanceRecordRepository;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.student_management_system.common.service.FileStorageService;
import com.student_management_system.teacher.model.StudyMaterial;
import com.student_management_system.teacher.repository.StudyMaterialRepository;
import org.springframework.web.multipart.MultipartFile;
import com.student_management_system.common.model.BudgetRequest;
import com.student_management_system.common.model.RequestStatus;
import com.student_management_system.common.repository.BudgetRequestRepository;
import com.student_management_system.teacher.dto.AssignmentCreationDto;

// NEW: Import new academic structure
import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.repository.EnrollmentRepository;
import com.student_management_system.common.repository.ClassroomRepository;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final FileStorageService fileStorageService;
    private final BudgetRequestRepository budgetRequestRepository;
    
    // NEW: Academic structure repositories
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;

    public TeacherService(AssignmentRepository assignmentRepository, SubjectRepository subjectRepository,
                          UserRepository userRepository, AttendanceRecordRepository attendanceRecordRepository, 
                          StudyMaterialRepository studyMaterialRepository, FileStorageService fileStorageService, 
                          BudgetRequestRepository budgetRequestRepository,
                          EnrollmentRepository enrollmentRepository,
                          ClassroomRepository classroomRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.fileStorageService = fileStorageService;
        this.budgetRequestRepository = budgetRequestRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.classroomRepository = classroomRepository;
    }

    // For now, we get all submitted assignments. Later, we can filter by teacher.
    public List<Assignment> getAssignmentsToGrade() {
        return assignmentRepository.findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED);
    }
    public Assignment getAssignmentToGradeById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
    }

    @Transactional
    public void gradeAssignment(Long assignmentId, String grade, String feedback) {
        Assignment assignment = getAssignmentToGradeById(assignmentId);

        assignment.setGrade(grade);
        assignment.setStatus(AssignmentStatus.GRADED);

        assignmentRepository.save(assignment);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<User> getStudentsForAttendance() {
        return userRepository.findByRole(Role.ROLE_STUDENT);
    }

    public Map<Long, AttendanceStatus> getAttendanceRecordsForSubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRecordRepository.findBySubjectIdAndDate(subjectId, date)
                .stream()
                .collect(Collectors.toMap(
                        record -> record.getStudent().getId(),
                        AttendanceRecord::getStatus
                ));
    }

    @Transactional
    public void saveAttendance(Long subjectId, LocalDate date, Map<String, String> attendanceData) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        for (Map.Entry<String, String> entry : attendanceData.entrySet()) {
            if (!entry.getKey().matches("\\d+")) {
                continue;
            }

            Long studentId = Long.parseLong(entry.getKey());
            AttendanceStatus status = AttendanceStatus.valueOf(entry.getValue());

            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // "Upsert" logic: Update existing record or create a new one
            List<AttendanceRecord> existingRecords = attendanceRecordRepository.findBySubjectIdAndDate(subjectId, date);
            AttendanceRecord record = existingRecords.stream()
                    .filter(r -> r.getStudent().getId().equals(studentId))
                    .findFirst()
                    .orElse(new AttendanceRecord());

            record.setStudent(student);
            record.setSubject(subject);
            record.setDate(date);
            record.setStatus(status);

            attendanceRecordRepository.save(record);
        }
    }

    // Handle the file upload and save the metadata
    @Transactional
    public void uploadStudyMaterial(String title, String description, Long subjectId, MultipartFile file) {
        // 1. Store the file on disk
        String fileName = fileStorageService.storeFile(file);

        // 2. Get the current teacher user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // 3. Get the subject
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // 4. Create and save the StudyMaterial entity
        StudyMaterial studyMaterial = new StudyMaterial();
        studyMaterial.setTitle(title);
        studyMaterial.setDescription(description);
        studyMaterial.setFileName(fileName);
        studyMaterial.setSubject(subject);
        studyMaterial.setUploadedBy(teacher);
        studyMaterial.setUploadDate(LocalDateTime.now());

        studyMaterialRepository.save(studyMaterial);
    }

    public List<StudyMaterial> getStudyMaterialsForSubject(Long subjectId) {
        return studyMaterialRepository.findBySubjectId(subjectId);
    }

    // Submit a budget request
    public void submitBudgetRequest(String title, String description, BigDecimal amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User teacher = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        BudgetRequest request = new BudgetRequest();
        request.setTitle(title);
        request.setAmount(amount);
        request.setRequester(teacher);
        request.setRequestDate(LocalDate.now());
        request.setStatus(RequestStatus.PENDING);

        budgetRequestRepository.save(request);
    }

    @Transactional
    public void createAssignment(AssignmentCreationDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User teacher = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // NEW: Verify teacher is assigned to this subject
        if (!subject.hasTeacher(teacher)) {
            throw new IllegalStateException("You are not assigned to teach this subject. Cannot create assignments.");
        }

        // Get students enrolled in classrooms for this subject's grade level
        Set<User> students = new java.util.HashSet<>();
        List<Classroom> classrooms = enrollmentRepository.findClassroomsByGradeLevel(subject.getGradeLevel());
        
        for (Classroom classroom : classrooms) {
            List<User> classroomStudents = enrollmentRepository.findStudentsByClassroom(classroom);
            students.addAll(classroomStudents);
        }

        if (students.isEmpty()) {
            throw new IllegalStateException("No students are enrolled in classrooms for this subject's grade level. Cannot create assignments.");
        }

        // Create an assignment for each student
        for (User student : students) {
            Assignment assignment = new Assignment();
            assignment.setTitle(dto.getTitle());
            assignment.setDescription(dto.getDescription());
            assignment.setDueDate(dto.getDueDate());
            assignment.setSubject(subject);
            assignment.setUser(student);
            assignment.setTeacher(teacher);
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            
            // Set classroom - find the student's classroom for this grade level
            Classroom studentClassroom = enrollmentRepository.findClassroomByStudentAndGradeLevel(student, subject.getGradeLevel());
            if (studentClassroom != null) {
                assignment.setClassroom(studentClassroom);
            }

            assignmentRepository.save(assignment);
        }
    }

    // NEW: Classroom-based attendance methods
    public List<User> getStudentsInClassroom(Long classroomId) {
        // Find classroom first, then get students
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
        
        return enrollmentRepository.findStudentsByClassroom(classroom);
    }

    public Map<Long, AttendanceStatus> getAttendanceRecordsForClassroomAndDate(Long classroomId, LocalDate date) {
        // For now, we'll use the existing subject-based method
        // TODO: Update AttendanceRecord entity to include classroom reference
        return new java.util.HashMap<>();
    }

    @Transactional
    public void saveClassroomAttendance(Long classroomId, LocalDate date, Map<String, String> attendanceData) {
        // Get the classroom and teacher information
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        User teacher = classroom.getClassTeacher();
        if (teacher == null) {
            throw new RuntimeException("No class teacher assigned to this classroom");
        }
        
        // For classroom-based attendance, we'll use the first subject taught by the teacher
        // or create a default "Homeroom" subject
        Subject defaultSubject = subjectRepository.findByTeachersContaining(teacher)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    // Create a default "Homeroom" subject if teacher has no subjects
                    Subject newHomeroom = new Subject();
                    newHomeroom.setName("Homeroom");
                    newHomeroom.setSubjectCode("HR-" + classroom.getGradeLevel().getName().replace(" ", ""));
                    newHomeroom.setGradeLevel(classroom.getGradeLevel());
                    newHomeroom.setActive(true);
                    return subjectRepository.save(newHomeroom);
                });
        
        for (Map.Entry<String, String> entry : attendanceData.entrySet()) {
            if (!entry.getKey().matches("\\d+")) {
                continue;
            }

            Long studentId = Long.parseLong(entry.getKey());
            AttendanceStatus status = AttendanceStatus.valueOf(entry.getValue());

            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // For classroom attendance, we'll create a new record each time
            // (or you could implement update logic if needed)
            AttendanceRecord record = new AttendanceRecord();
            record.setStudent(student);
            record.setSubject(defaultSubject); // Use default subject for classroom attendance
            record.setClassroom(classroom);
            record.setTeacher(teacher);
            record.setDate(date);
            record.setStatus(status);

            attendanceRecordRepository.save(record);
        }
    }

    // ===== ASSIGNMENT MANAGEMENT METHODS =====

    public List<Assignment> getAssignmentsByTeacher(User teacher) {
        return assignmentRepository.findByTeacher(teacher);
    }

    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    public List<Assignment> getSubmissionsForAssignment(Assignment assignment) {
        // Get all assignments for the same subject, classroom, and title (represents submissions)
        return assignmentRepository.findBySubjectAndClassroomAndTitle(
                assignment.getSubject(), assignment.getClassroom(), assignment.getTitle());
    }

    @Transactional
    public void createAssignmentForSubject(AssignmentCreationDto assignmentDto) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get the subject
        Subject subject = subjectRepository.findById(assignmentDto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Verify teacher teaches this subject
        if (!subject.getTeachers().contains(teacher)) {
            throw new RuntimeException("You are not assigned to teach this subject");
        }

        // Get the specific classroom
        Classroom classroom = classroomRepository.findById(assignmentDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Verify the classroom is for the same grade level as the subject
        if (!classroom.getGradeLevel().equals(subject.getGradeLevel())) {
            throw new RuntimeException("Classroom grade level does not match subject grade level");
        }

        // Get students in the specific classroom
        List<User> students = getStudentsInClassroom(classroom.getId());
        
        // Create assignment for each student in the specific classroom
        for (User student : students) {
            Assignment assignment = new Assignment();
            assignment.setTitle(assignmentDto.getTitle());
            assignment.setDescription(assignmentDto.getDescription());
            assignment.setDueDate(assignmentDto.getDueDate());
            assignment.setSubject(subject);
            assignment.setClassroom(classroom);
            assignment.setUser(student);
            assignment.setTeacher(teacher);
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            assignment.setCreatedDate(LocalDateTime.now());

            assignmentRepository.save(assignment);
        }
    }

    @Transactional
    public void updateAssignmentTemplate(User teacher, String originalTitle, Long originalSubjectId, Long originalClassroomId, AssignmentCreationDto assignmentDto) {

        // Find all assignments with the original title, subject, and classroom
        List<Assignment> assignments = assignmentRepository.findByTeacher(teacher);
        List<Assignment> templateAssignments = assignments.stream()
                .filter(a -> a.getTitle().equals(originalTitle) && 
                           a.getSubject().getId().equals(originalSubjectId) &&
                           a.getClassroom().getId().equals(originalClassroomId))
                .collect(java.util.stream.Collectors.toList());

        if (templateAssignments.isEmpty()) {
            throw new RuntimeException("Assignment template not found");
        }

        // Get the new subject and classroom
        Subject newSubject = subjectRepository.findById(assignmentDto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Classroom newClassroom = classroomRepository.findById(assignmentDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Verify teacher teaches the new subject
        if (!newSubject.getTeachers().contains(teacher)) {
            throw new RuntimeException("You are not assigned to teach this subject");
        }

        // Update all assignments in this template
        for (Assignment assignment : templateAssignments) {
            assignment.setTitle(assignmentDto.getTitle());
            assignment.setDescription(assignmentDto.getDescription());
            assignment.setDueDate(assignmentDto.getDueDate());
            assignment.setSubject(newSubject);
            assignment.setClassroom(newClassroom);
            assignmentRepository.save(assignment);
        }
    }

    @Transactional
    public void deleteAssignmentTemplate(User teacher, String title, Long subjectId, Long classroomId) {

        // Find all assignments with this title, subject, and classroom
        List<Assignment> assignments = assignmentRepository.findByTeacher(teacher);
        List<Assignment> templateAssignments = assignments.stream()
                .filter(a -> a.getTitle().equals(title) && 
                           a.getSubject().getId().equals(subjectId) &&
                           a.getClassroom().getId().equals(classroomId))
                .collect(java.util.stream.Collectors.toList());

        if (templateAssignments.isEmpty()) {
            throw new RuntimeException("Assignment template not found");
        }

        // Delete all assignments in this template
        for (Assignment assignment : templateAssignments) {
            assignmentRepository.delete(assignment);
        }
    }

}