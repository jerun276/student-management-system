package com.student_management_system.teacher.controller;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.teacher.model.StudyMaterial;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.service.TimetableService;
import com.student_management_system.common.service.TimeSlotService;
import com.student_management_system.common.repository.ClassroomRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {
    private final TeacherService teacherService;
    private final AcademicYearService academicYearService;
    private final TimetableService timetableService;
    private final TimeSlotService timeSlotService;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;

    public TeacherDashboardController(TeacherService teacherService, 
                                    AcademicYearService academicYearService,
                                    TimetableService timetableService,
                                    TimeSlotService timeSlotService,
                                    UserRepository userRepository,
                                    SubjectRepository subjectRepository,
                                    ClassroomRepository classroomRepository) {
        this.teacherService = teacherService;
        this.academicYearService = academicYearService;
        this.timetableService = timetableService;
        this.timeSlotService = timeSlotService;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping("/dashboard")
    public String teacherDashboard(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Find classroom where this teacher is the class teacher
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        // Get subjects taught by this teacher
        List<Subject> mySubjects = subjectRepository.findByTeachersContaining(teacher);
        model.addAttribute("mySubjects", mySubjects);

        // Calculate total students - prioritize classroom students if class teacher, otherwise subject-based
        int totalStudents;
        if (assignedClassroom != null) {
            // If class teacher, show students in their classroom
            totalStudents = assignedClassroom.getEnrollments() != null ? 
                assignedClassroom.getEnrollments().size() : 0;
        } else {
            // If not class teacher, count students across all subjects taught
            // Get students from classrooms that match the subject's grade level
            totalStudents = mySubjects.stream()
                .mapToInt(subject -> {
                    List<Classroom> classrooms = classroomRepository.findByGradeLevel(subject.getGradeLevel());
                    return classrooms.stream()
                        .mapToInt(classroom -> classroom.getEnrollments() != null ? 
                            classroom.getEnrollments().size() : 0)
                        .sum();
                })
                .sum();
        }

        // Get assignments to grade (only for this teacher)
        List<Assignment> submittedAssignments = teacherService.getAssignmentsToGrade();
        // Filter by teacher
        List<Assignment> teacherAssignments = submittedAssignments.stream()
            .filter(assignment -> assignment.getTeacher() != null && 
                assignment.getTeacher().getId().equals(teacher.getId()))
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("submittedAssignments", teacherAssignments);
        
        // Get current academic year
        Optional<AcademicYear> currentAcademicYear = academicYearService.getCurrentAcademicYear();
        model.addAttribute("currentAcademicYear", currentAcademicYear.orElse(null));
        
        // Add teacher and classroom data to model
        model.addAttribute("teacher", teacher);
        model.addAttribute("assignedClassroom", assignedClassroom);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalSubjects", mySubjects.size());
        
        return "teacher/dashboard";
    }

    @GetMapping("/subjects")
    public String showSubjects(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get teacher's subjects
        List<Subject> mySubjects = subjectRepository.findByTeachersContaining(teacher);
        model.addAttribute("mySubjects", mySubjects);
        model.addAttribute("teacher", teacher);

        // Get current academic year
        Optional<AcademicYear> currentYear = academicYearService.getCurrentAcademicYear();
        model.addAttribute("selectedAcademicYear", currentYear.orElse(null));

        // Calculate total students for all subjects
        int totalStudents = mySubjects.stream()
            .mapToInt(subject -> {
                List<Classroom> classrooms = classroomRepository.findByGradeLevel(subject.getGradeLevel());
                return classrooms.stream()
                    .mapToInt(classroom -> classroom.getEnrollments() != null ? 
                        classroom.getEnrollments().size() : 0)
                    .sum();
            })
            .sum();
        model.addAttribute("totalStudents", totalStudents);

        return "teacher/subjects";
    }


    @GetMapping("/budget/request")
    public String showBudgetRequestForm(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));
        
        model.addAttribute("teacher", teacher);
        return "teacher/budget-request";
    }
    
    @GetMapping("/budget/history")
    public String viewBudgetRequestHistory(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));
        
        // Get budget request history
        var budgetRequests = teacherService.getBudgetRequestHistory();
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("budgetRequests", budgetRequests);
        return "teacher/budget-history";
    }

    @PostMapping("/budget/request")
    public String submitBudgetRequest(@RequestParam String title,
                                      @RequestParam String description,
                                      @RequestParam BigDecimal amount,
                                      RedirectAttributes redirectAttributes) {
        try {
            teacherService.submitBudgetRequest(title, description, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Budget request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit request.");
        }
        return "redirect:/teacher/dashboard";
    }

    /**
     * Show teacher's personal timetable
     */
    @GetMapping("/timetable")
    public String showTimetable(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get teacher's timetable
        Map<DayOfWeek, List<TimetableEntry>> timetable = timetableService.getTeacherTimetable(teacher);
        
        // Get time slots for display
        List<TimeSlot> timeSlots = timeSlotService.getRegularPeriods();
        
        // Get teacher's free slots for today
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        List<TimetableService.FreeSlot> freeSlots = timetableService.getTeacherFreeSlots(teacher, today);

        model.addAttribute("teacher", teacher);
        model.addAttribute("timetable", timetable);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("freeSlots", freeSlots);
        model.addAttribute("weekDays", new DayOfWeek[]{
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        });

        return "teacher/timetable";
    }

    /**
     * Show current period information
     */
    @GetMapping("/current-period")
    public String showCurrentPeriod(Model model) {
        // Get current time slot
        Optional<TimeSlot> currentSlot = timeSlotService.getCurrentTimeSlot();
        Optional<TimeSlot> nextSlot = timeSlotService.getNextTimeSlot();

        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        model.addAttribute("currentSlot", currentSlot.orElse(null));
        model.addAttribute("nextSlot", nextSlot.orElse(null));
        model.addAttribute("teacher", teacher);

        // If there's a current slot, get the teacher's class for this period
        if (currentSlot.isPresent()) {
            DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
            List<TimetableEntry> currentEntries = timetableService.getTimetableEntriesForSlot(today, currentSlot.get());
            
            // Find teacher's entry for this period
            Optional<TimetableEntry> teacherEntry = currentEntries.stream()
                .filter(entry -> entry.getTeacher().getId().equals(teacher.getId()))
                .findFirst();
            
            model.addAttribute("currentEntry", teacherEntry.orElse(null));
        }

        return "teacher/current-period";
    }

    @GetMapping("/classroom/students")
    public String viewClassroomStudents(Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Find classroom where this teacher is the class teacher
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        if (assignedClassroom == null) {
            model.addAttribute("errorMessage", "You are not assigned as a class teacher to any classroom.");
            return "teacher/dashboard";
        }

        // Get students in the classroom
        List<User> students = teacherService.getStudentsInClassroom(assignedClassroom.getId());
        
        model.addAttribute("classroom", assignedClassroom);
        model.addAttribute("students", students);
        model.addAttribute("teacher", teacher);
        
        return "teacher/classroom-students";
    }

    @GetMapping("/student/{id}/profile")
    public String viewStudentProfile(@PathVariable Long id, Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get the student
        User student = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found"));

        // Verify the student is in the teacher's assigned classroom
        Classroom assignedClassroom = classroomRepository.findByClassTeacher(teacher).orElse(null);
        
        if (assignedClassroom == null) {
            model.addAttribute("errorMessage", "You are not assigned as a class teacher to any classroom.");
            return "teacher/dashboard";
        }

        // Check if student is enrolled in teacher's classroom
        List<User> studentsInClassroom = teacherService.getStudentsInClassroom(assignedClassroom.getId());
        boolean isStudentInClassroom = studentsInClassroom.stream()
            .anyMatch(s -> s.getId().equals(student.getId()));

        if (!isStudentInClassroom) {
            model.addAttribute("errorMessage", "This student is not in your assigned classroom.");
            return "teacher/classroom-students";
        }

        model.addAttribute("student", student);
        model.addAttribute("teacher", teacher);
        model.addAttribute("classroom", assignedClassroom);
        
        return "teacher/student-profile";
    }

    @GetMapping("/subjects/{id}/materials")
    public String viewSubjectMaterials(@PathVariable Long id, Model model) {
        // Get current teacher
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current teacher not found"));

        // Get the subject and verify teacher has access
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Verify teacher teaches this subject
        List<Subject> teacherSubjects = subjectRepository.findByTeachersContaining(teacher);
        boolean canAccess = teacherSubjects.stream()
            .anyMatch(s -> s.getId().equals(subject.getId()));

        if (!canAccess) {
            model.addAttribute("errorMessage", "You don't have access to this subject's materials.");
            return "redirect:/teacher/subjects";
        }

        // Get study materials for this subject
        List<StudyMaterial> materials = teacherService.getStudyMaterialsForSubject(id);

        model.addAttribute("subject", subject);
        model.addAttribute("materials", materials);
        model.addAttribute("teacher", teacher);

        return "teacher/subject-materials";
    }
}