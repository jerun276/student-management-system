package com.student_management_system.init;

import com.student_management_system.student.model.Subject;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.repository.AssignmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AssignmentRepository assignmentRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           SubjectRepository subjectRepository, TimetableEntryRepository timetableEntryRepository, AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only run this if the user table is empty to avoid creating duplicate users on
        // every restart
        if (userRepository.count() == 0) {
            System.out.println("No users found in DB, creating sample users...");
            createUsers();
            System.out.println("Sample users created successfully!");
        } else {
            System.out.println("Users already exist in the DB, skipping data initialization.");
        }

        if (subjectRepository.count() == 0) {
            System.out.println("No Timetable found in DB, creating sample ...");
            createTimetableData();
            System.out.println("Sample Timetable created successfully!");
        } else {
            System.out.println("Timetable already exist in the DB, skipping data initialization.");
        }

        if (assignmentRepository.count() == 0) {
            System.out.println("No Assignment found in DB, creating sample ...");
            createAssignmentData();
            System.out.println("Sample Assignment created successfully!");
        } else {
            System.out.println("Assignment already exist in the DB, skipping data initialization.");
        }
    }

    private void createUsers() {
        // Create an Admin User
        User admin = new User();
        admin.setUsername("admin");
        // IMPORTANT: Always encode passwords before saving!
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@school.com");
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // Create a Student User
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@school.com");
        student.setRole(Role.ROLE_STUDENT);
        userRepository.save(student);

        // Create a Teacher User
        User teacher = new User();
        teacher.setUsername("teacher");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setEmail("teacher@school.com");
        teacher.setRole(Role.ROLE_TEACHER);
        userRepository.save(teacher);

        // Create a Parent User
        User parent = new User();
        parent.setUsername("parent");
        parent.setPassword(passwordEncoder.encode("parent123"));
        parent.setEmail("parent@school.com");
        parent.setRole(Role.ROLE_PARENT);
        userRepository.save(parent);

        student.setParent(parent);
        userRepository.save(student);

        System.out.println("Sample users including parent-child link created successfully!");
    }

    private void createTimetableData() {
        System.out.println("Creating sample subjects and timetable...");

        // Find the student user we created
        User student = userRepository.findByUsername("student").orElse(null);
        if (student == null) return; // Can't create timetable without a student

        // Create Subjects
        Subject math = new Subject();
        math.setName("Mathematics");
        math.setTeacherName("Mr. A. Smith");
        subjectRepository.save(math);

        Subject physics = new Subject();
        physics.setName("Physics");
        physics.setTeacherName("Ms. B. Curie");
        subjectRepository.save(physics);

        // Create Timetable Entries for the student
        TimetableEntry entry1 = new TimetableEntry();
        entry1.setUser(student);
        entry1.setSubject(math);
        entry1.setDayOfWeek(DayOfWeek.MONDAY);
        entry1.setStartTime(LocalTime.of(9, 0));
        entry1.setEndTime(LocalTime.of(10, 0));
        timetableEntryRepository.save(entry1);

        TimetableEntry entry2 = new TimetableEntry();
        entry2.setUser(student);
        entry2.setSubject(physics);
        entry2.setDayOfWeek(DayOfWeek.TUESDAY);
        entry2.setStartTime(LocalTime.of(11, 0));
        entry2.setEndTime(LocalTime.of(12, 0));
        timetableEntryRepository.save(entry2);

        System.out.println("Sample timetable created.");
    }

    private void createAssignmentData() {
        System.out.println("Creating sample assignments...");

        User student = userRepository.findByUsername("student").orElse(null);
        Subject math = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Mathematics")).findFirst().orElse(null);

        if (student == null || math == null) return;

        Assignment assignment1 = new Assignment();
        assignment1.setTitle("Algebra Homework 1");
        assignment1.setDescription("Complete exercises 1-10 on page 42.");
        assignment1.setDueDate(LocalDate.now().plusDays(7));
        assignment1.setStatus(AssignmentStatus.PENDING);
        assignment1.setSubject(math);
        assignment1.setUser(student);
        assignmentRepository.save(assignment1);

        Assignment assignment2 = new Assignment();
        assignment2.setTitle("Geometry Proofs");
        assignment2.setDescription("Complete the two proofs from the worksheet.");
        assignment2.setDueDate(LocalDate.now().plusDays(3));
        assignment2.setStatus(AssignmentStatus.PENDING);
        assignment2.setSubject(math);
        assignment2.setUser(student);
        assignmentRepository.save(assignment2);

        System.out.println("Sample assignments created.");
    }
}