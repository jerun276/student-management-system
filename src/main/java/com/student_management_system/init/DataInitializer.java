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
import com.student_management_system.staff.model.Fee;
import com.student_management_system.staff.model.FeeStatus;
import com.student_management_system.staff.repository.FeeRepository;

// NEW: Import new academic structure entities and services
import com.student_management_system.common.model.*;
import com.student_management_system.common.repository.*;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.service.EnrollmentService;

import java.math.BigDecimal;

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
    private final FeeRepository feeRepository;

    // NEW: Academic structure repositories and services
    private final AcademicYearRepository academicYearRepository;
    private final GradeLevelRepository gradeLevelRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AcademicYearService academicYearService;
    private final EnrollmentService enrollmentService;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SubjectRepository subjectRepository, TimetableEntryRepository timetableEntryRepository,
            AssignmentRepository assignmentRepository, FeeRepository feeRepository,
            AcademicYearRepository academicYearRepository, GradeLevelRepository gradeLevelRepository,
            ClassroomRepository classroomRepository, EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository, AcademicYearService academicYearService,
            EnrollmentService enrollmentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.assignmentRepository = assignmentRepository;
        this.feeRepository = feeRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeLevelRepository = gradeLevelRepository;
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.academicYearService = academicYearService;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public void run(String... args) {
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

        if (feeRepository.count() == 0) {
            createStaffAndFees();
        }

        // NEW: Initialize academic structure
        if (academicYearRepository.count() == 0) {
            System.out.println(
                    "No academic structure found in DB, creating academic years, grade levels, and classrooms...");
            createAcademicStructure();
            System.out.println("Academic structure created successfully!");
        } else {
            System.out.println("Academic structure already exists in the DB, skipping initialization.");
        }

    }

    private void createUsers() {
        System.out.println("No users found in DB, creating sample users...");

        // Create an Admin User
        User admin = new User();
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@school.com");
        admin.setDateOfBirth(LocalDate.of(1980, 1, 1));
        admin.setAddress("123 Admin Way");
        admin.setPhoneNumber("555-0101");
        admin.setNic("801234567V");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // Create Teachers
        User mathTeacher = new User();
        mathTeacher.setUsername("math_teacher");
        mathTeacher.setFirstName("John");
        mathTeacher.setLastName("Smith");
        mathTeacher.setEmail("john.smith@school.com");
        mathTeacher.setDateOfBirth(LocalDate.of(1985, 3, 15));
        mathTeacher.setAddress("789 Teacher Ave");
        mathTeacher.setPhoneNumber("555-0103");
        mathTeacher.setNic("851234567V");
        mathTeacher.setPassword(passwordEncoder.encode("teacher123"));
        mathTeacher.setRole(Role.ROLE_TEACHER);
        userRepository.save(mathTeacher);

        User scienceTeacher = new User();
        scienceTeacher.setUsername("science_teacher");
        scienceTeacher.setFirstName("Marie");
        scienceTeacher.setLastName("Curie");
        scienceTeacher.setEmail("marie.curie@school.com");
        scienceTeacher.setDateOfBirth(LocalDate.of(1982, 7, 20));
        scienceTeacher.setAddress("456 Science Blvd");
        scienceTeacher.setPhoneNumber("555-0107");
        scienceTeacher.setNic("821234567V");
        scienceTeacher.setPassword(passwordEncoder.encode("teacher123"));
        scienceTeacher.setRole(Role.ROLE_TEACHER);
        userRepository.save(scienceTeacher);

        User englishTeacher = new User();
        englishTeacher.setUsername("english_teacher");
        englishTeacher.setFirstName("William");
        englishTeacher.setLastName("Shakespeare");
        englishTeacher.setEmail("william.shakespeare@school.com");
        englishTeacher.setDateOfBirth(LocalDate.of(1980, 4, 23));
        englishTeacher.setAddress("123 Literature Lane");
        englishTeacher.setPhoneNumber("555-0108");
        englishTeacher.setNic("801234568V");
        englishTeacher.setPassword(passwordEncoder.encode("teacher123"));
        englishTeacher.setRole(Role.ROLE_TEACHER);
        userRepository.save(englishTeacher);

        // Create Parents
        User parent1 = new User();
        parent1.setUsername("parent1");
        parent1.setFirstName("David");
        parent1.setLastName("Johnson");
        parent1.setEmail("david.johnson@email.com");
        parent1.setDateOfBirth(LocalDate.of(1975, 8, 20));
        parent1.setAddress("456 Student St");
        parent1.setPhoneNumber("555-0104");
        parent1.setNic("751234567V");
        parent1.setPassword(passwordEncoder.encode("parent123"));
        parent1.setRole(Role.ROLE_PARENT);
        userRepository.save(parent1);

        User parent2 = new User();
        parent2.setUsername("parent2");
        parent2.setFirstName("Sarah");
        parent2.setLastName("Williams");
        parent2.setEmail("sarah.williams@email.com");
        parent2.setDateOfBirth(LocalDate.of(1978, 12, 10));
        parent2.setAddress("789 Family Ave");
        parent2.setPhoneNumber("555-0109");
        parent2.setNic("781234567V");
        parent2.setPassword(passwordEncoder.encode("parent123"));
        parent2.setRole(Role.ROLE_PARENT);
        userRepository.save(parent2);

        User parent3 = new User();
        parent3.setUsername("parent3");
        parent3.setFirstName("Michael");
        parent3.setLastName("Brown");
        parent3.setEmail("michael.brown@email.com");
        parent3.setDateOfBirth(LocalDate.of(1973, 6, 15));
        parent3.setAddress("321 Parent Rd");
        parent3.setPhoneNumber("555-0110");
        parent3.setNic("731234567V");
        parent3.setPassword(passwordEncoder.encode("parent123"));
        parent3.setRole(Role.ROLE_PARENT);
        userRepository.save(parent3);

        // Create Students
        User student1 = new User();
        student1.setUsername("alice_johnson");
        student1.setFirstName("Alice");
        student1.setLastName("Johnson");
        student1.setEmail("alice.johnson@school.com");
        student1.setDateOfBirth(LocalDate.of(2005, 5, 10));
        student1.setAddress("456 Student St");
        student1.setPhoneNumber("555-0102");
        student1.setNic("051234567V");
        student1.setPassword(passwordEncoder.encode("student123"));
        student1.setRole(Role.ROLE_STUDENT);
        student1.setParent(parent1);
        userRepository.save(student1);

        User student2 = new User();
        student2.setUsername("bob_williams");
        student2.setFirstName("Bob");
        student2.setLastName("Williams");
        student2.setEmail("bob.williams@school.com");
        student2.setDateOfBirth(LocalDate.of(2006, 3, 22));
        student2.setAddress("789 Family Ave");
        student2.setPhoneNumber("555-0111");
        student2.setNic("061234567V");
        student2.setPassword(passwordEncoder.encode("student123"));
        student2.setRole(Role.ROLE_STUDENT);
        student2.setParent(parent2);
        userRepository.save(student2);

        User student3 = new User();
        student3.setUsername("charlie_brown");
        student3.setFirstName("Charlie");
        student3.setLastName("Brown");
        student3.setEmail("charlie.brown@school.com");
        student3.setDateOfBirth(LocalDate.of(2005, 11, 8));
        student3.setAddress("321 Parent Rd");
        student3.setPhoneNumber("555-0112");
        student3.setNic("051234568V");
        student3.setPassword(passwordEncoder.encode("student123"));
        student3.setRole(Role.ROLE_STUDENT);
        student3.setParent(parent3);
        userRepository.save(student3);

        User student4 = new User();
        student4.setUsername("diana_smith");
        student4.setFirstName("Diana");
        student4.setLastName("Smith");
        student4.setEmail("diana.smith@school.com");
        student4.setDateOfBirth(LocalDate.of(2006, 9, 14));
        student4.setAddress("654 School Lane");
        student4.setPhoneNumber("555-0113");
        student4.setNic("061234568V");
        student4.setPassword(passwordEncoder.encode("student123"));
        student4.setRole(Role.ROLE_STUDENT);
        student4.setParent(parent1); // Alice and Diana share the same parent
        userRepository.save(student4);

        // Create a Principal User
        User principal = new User();
        principal.setUsername("principal");
        principal.setFirstName("Principal");
        principal.setLastName("Anderson");
        principal.setEmail("principal@school.com");
        principal.setDateOfBirth(LocalDate.of(1970, 2, 25));
        principal.setAddress("1 School Rd");
        principal.setPhoneNumber("555-0105");
        principal.setNic("701234567V");
        principal.setPassword(passwordEncoder.encode("principal123"));
        principal.setRole(Role.ROLE_PRINCIPAL);
        userRepository.save(principal);

        System.out.println(
                "Sample users including multiple teachers, students, and parent-child relationships created successfully!");
    }

    private void createTimetableData() {
        System.out.println("Creating sample subjects, enrollments, and timetable...");

        // Find the teachers we created
        User mathTeacher = userRepository.findByUsername("math_teacher").orElse(null);
        User scienceTeacher = userRepository.findByUsername("science_teacher").orElse(null);
        User englishTeacher = userRepository.findByUsername("english_teacher").orElse(null);

        // Find the students we created
        User alice = userRepository.findByUsername("alice_johnson").orElse(null);
        User bob = userRepository.findByUsername("bob_williams").orElse(null);
        User charlie = userRepository.findByUsername("charlie_brown").orElse(null);
        User diana = userRepository.findByUsername("diana_smith").orElse(null);

        if (mathTeacher == null || scienceTeacher == null || englishTeacher == null ||
                alice == null || bob == null || charlie == null || diana == null) {
            System.out.println("Required users not found, skipping timetable creation");
            return;
        }

        // Create Subjects (now simplified - teacher assignments handled through
        // Courses)
        Subject mathematics = new Subject();
        mathematics.setName("Mathematics");
        mathematics.setCode("MATH");
        mathematics.setDescription("Mathematics curriculum");
        mathematics.setActive(true);
        subjectRepository.save(mathematics);

        Subject physics = new Subject();
        physics.setName("Physics");
        physics.setCode("PHYS");
        physics.setDescription("Physics curriculum");
        physics.setActive(true);
        subjectRepository.save(physics);

        Subject chemistry = new Subject();
        chemistry.setName("Chemistry");
        chemistry.setCode("CHEM");
        chemistry.setDescription("Chemistry curriculum");
        chemistry.setActive(true);
        subjectRepository.save(chemistry);

        Subject english = new Subject();
        english.setName("English Literature");
        english.setCode("ENG");
        english.setDescription("English Literature curriculum");
        english.setActive(true);
        subjectRepository.save(english);

        Subject biology = new Subject();
        biology.setName("Biology");
        biology.setCode("BIO");
        biology.setDescription("Biology curriculum");
        biology.setActive(true);
        subjectRepository.save(biology);

        // Establish student-subject enrollments (Many-to-Many relationships)
        // Alice is enrolled in Math, Physics, and English
        alice.getEnrolledSubjects().add(mathematics);
        alice.getEnrolledSubjects().add(physics);
        alice.getEnrolledSubjects().add(english);
        userRepository.save(alice);

        // Bob is enrolled in Math, Chemistry, and English
        bob.getEnrolledSubjects().add(mathematics);
        bob.getEnrolledSubjects().add(chemistry);
        bob.getEnrolledSubjects().add(english);
        userRepository.save(bob);

        // Charlie is enrolled in Physics, Chemistry, and Biology
        charlie.getEnrolledSubjects().add(physics);
        charlie.getEnrolledSubjects().add(chemistry);
        charlie.getEnrolledSubjects().add(biology);
        userRepository.save(charlie);

        // Diana is enrolled in all subjects (a high-achieving student)
        diana.getEnrolledSubjects().add(mathematics);
        diana.getEnrolledSubjects().add(physics);
        diana.getEnrolledSubjects().add(chemistry);
        diana.getEnrolledSubjects().add(english);
        diana.getEnrolledSubjects().add(biology);
        userRepository.save(diana);

        // Create comprehensive timetable entries
        // Alice's timetable
        createTimetableEntry(alice, mathematics, DayOfWeek.MONDAY, 9, 0, 10, 0);
        createTimetableEntry(alice, physics, DayOfWeek.TUESDAY, 10, 0, 11, 0);
        createTimetableEntry(alice, english, DayOfWeek.WEDNESDAY, 11, 0, 12, 0);
        createTimetableEntry(alice, mathematics, DayOfWeek.THURSDAY, 9, 0, 10, 0);
        createTimetableEntry(alice, physics, DayOfWeek.FRIDAY, 10, 0, 11, 0);

        // Bob's timetable
        createTimetableEntry(bob, mathematics, DayOfWeek.MONDAY, 10, 0, 11, 0);
        createTimetableEntry(bob, chemistry, DayOfWeek.TUESDAY, 11, 0, 12, 0);
        createTimetableEntry(bob, english, DayOfWeek.WEDNESDAY, 9, 0, 10, 0);
        createTimetableEntry(bob, mathematics, DayOfWeek.THURSDAY, 10, 0, 11, 0);
        createTimetableEntry(bob, chemistry, DayOfWeek.FRIDAY, 11, 0, 12, 0);

        // Charlie's timetable
        createTimetableEntry(charlie, physics, DayOfWeek.MONDAY, 11, 0, 12, 0);
        createTimetableEntry(charlie, chemistry, DayOfWeek.TUESDAY, 9, 0, 10, 0);
        createTimetableEntry(charlie, biology, DayOfWeek.WEDNESDAY, 10, 0, 11, 0);
        createTimetableEntry(charlie, physics, DayOfWeek.THURSDAY, 11, 0, 12, 0);
        createTimetableEntry(charlie, biology, DayOfWeek.FRIDAY, 9, 0, 10, 0);

        // Diana's comprehensive timetable (all subjects)
        createTimetableEntry(diana, mathematics, DayOfWeek.MONDAY, 8, 0, 9, 0);
        createTimetableEntry(diana, physics, DayOfWeek.MONDAY, 12, 0, 13, 0);
        createTimetableEntry(diana, chemistry, DayOfWeek.TUESDAY, 8, 0, 9, 0);
        createTimetableEntry(diana, english, DayOfWeek.TUESDAY, 12, 0, 13, 0);
        createTimetableEntry(diana, biology, DayOfWeek.WEDNESDAY, 8, 0, 9, 0);
        createTimetableEntry(diana, mathematics, DayOfWeek.WEDNESDAY, 12, 0, 13, 0);
        createTimetableEntry(diana, physics, DayOfWeek.THURSDAY, 8, 0, 9, 0);
        createTimetableEntry(diana, chemistry, DayOfWeek.THURSDAY, 12, 0, 13, 0);
        createTimetableEntry(diana, english, DayOfWeek.FRIDAY, 8, 0, 9, 0);
        createTimetableEntry(diana, biology, DayOfWeek.FRIDAY, 12, 0, 13, 0);

        System.out.println("Sample subjects, enrollments, and comprehensive timetables created successfully!");
    }

    private void createTimetableEntry(User student, Subject subject, DayOfWeek day, int startHour, int startMinute,
            int endHour, int endMinute) {
        TimetableEntry entry = new TimetableEntry();
        entry.setUser(student);
        entry.setSubject(subject);
        entry.setDayOfWeek(day);
        entry.setStartTime(LocalTime.of(startHour, startMinute));
        entry.setEndTime(LocalTime.of(endHour, endMinute));
        timetableEntryRepository.save(entry);
    }

    private void createAssignmentData() {
        System.out.println("Creating sample assignments...");

        // Find teachers
        User mathTeacher = userRepository.findByUsername("math_teacher").orElse(null);
        User scienceTeacher = userRepository.findByUsername("science_teacher").orElse(null);
        User englishTeacher = userRepository.findByUsername("english_teacher").orElse(null);

        // Find students
        User alice = userRepository.findByUsername("alice_johnson").orElse(null);
        User bob = userRepository.findByUsername("bob_williams").orElse(null);
        User charlie = userRepository.findByUsername("charlie_brown").orElse(null);
        User diana = userRepository.findByUsername("diana_smith").orElse(null);

        // Find subjects
        Subject mathematics = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Mathematics")).findFirst().orElse(null);
        Subject physics = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Physics")).findFirst().orElse(null);
        Subject chemistry = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Chemistry")).findFirst().orElse(null);
        Subject english = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("English Literature")).findFirst().orElse(null);
        Subject biology = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Biology")).findFirst().orElse(null);

        if (mathTeacher == null || scienceTeacher == null || englishTeacher == null ||
                alice == null || bob == null || charlie == null || diana == null ||
                mathematics == null || physics == null || chemistry == null || english == null || biology == null) {
            System.out.println("Required data not found, skipping assignment creation");
            return;
        }

        // Mathematics assignments
        createAssignment("Algebra Homework 1", "Complete exercises 1-10 on page 42.",
                LocalDate.now().plusDays(7), AssignmentStatus.ASSIGNED, mathematics, alice, mathTeacher);
        createAssignment("Algebra Homework 1", "Complete exercises 1-10 on page 42.",
                LocalDate.now().plusDays(7), AssignmentStatus.ASSIGNED, mathematics, bob, mathTeacher);
        createAssignment("Algebra Homework 1", "Complete exercises 1-10 on page 42.",
                LocalDate.now().plusDays(7), AssignmentStatus.ASSIGNED, mathematics, diana, mathTeacher);

        createAssignment("Geometry Proofs", "Complete the two proofs from the worksheet.",
                LocalDate.now().plusDays(3), AssignmentStatus.SUBMITTED, mathematics, alice, mathTeacher);
        createAssignment("Geometry Proofs", "Complete the two proofs from the worksheet.",
                LocalDate.now().plusDays(3), AssignmentStatus.PENDING, mathematics, bob, mathTeacher);
        createAssignment("Geometry Proofs", "Complete the two proofs from the worksheet.",
                LocalDate.now().plusDays(3), AssignmentStatus.GRADED, mathematics, diana, mathTeacher);

        // Physics assignments
        createAssignment("Newton's Laws Lab Report",
                "Write a comprehensive lab report on Newton's three laws of motion.",
                LocalDate.now().plusDays(10), AssignmentStatus.ASSIGNED, physics, alice, scienceTeacher);
        createAssignment("Newton's Laws Lab Report",
                "Write a comprehensive lab report on Newton's three laws of motion.",
                LocalDate.now().plusDays(10), AssignmentStatus.ASSIGNED, physics, charlie, scienceTeacher);
        createAssignment("Newton's Laws Lab Report",
                "Write a comprehensive lab report on Newton's three laws of motion.",
                LocalDate.now().plusDays(10), AssignmentStatus.ASSIGNED, physics, diana, scienceTeacher);

        // Chemistry assignments
        createAssignment("Chemical Bonding Quiz",
                "Study chapters 5-7 and prepare for the quiz on ionic and covalent bonds.",
                LocalDate.now().plusDays(5), AssignmentStatus.ASSIGNED, chemistry, bob, scienceTeacher);
        createAssignment("Chemical Bonding Quiz",
                "Study chapters 5-7 and prepare for the quiz on ionic and covalent bonds.",
                LocalDate.now().plusDays(5), AssignmentStatus.SUBMITTED, chemistry, charlie, scienceTeacher);
        createAssignment("Chemical Bonding Quiz",
                "Study chapters 5-7 and prepare for the quiz on ionic and covalent bonds.",
                LocalDate.now().plusDays(5), AssignmentStatus.ASSIGNED, chemistry, diana, scienceTeacher);

        // English assignments
        createAssignment("Shakespeare Essay", "Write a 500-word essay analyzing the themes in Romeo and Juliet.",
                LocalDate.now().plusDays(14), AssignmentStatus.ASSIGNED, english, alice, englishTeacher);
        createAssignment("Shakespeare Essay", "Write a 500-word essay analyzing the themes in Romeo and Juliet.",
                LocalDate.now().plusDays(14), AssignmentStatus.ASSIGNED, english, bob, englishTeacher);
        createAssignment("Shakespeare Essay", "Write a 500-word essay analyzing the themes in Romeo and Juliet.",
                LocalDate.now().plusDays(14), AssignmentStatus.ASSIGNED, english, diana, englishTeacher);

        // Biology assignments
        createAssignment("Cell Structure Diagram", "Draw and label a detailed diagram of plant and animal cells.",
                LocalDate.now().plusDays(8), AssignmentStatus.ASSIGNED, biology, charlie, scienceTeacher);
        createAssignment("Cell Structure Diagram", "Draw and label a detailed diagram of plant and animal cells.",
                LocalDate.now().plusDays(8), AssignmentStatus.ASSIGNED, biology, diana, scienceTeacher);

        System.out.println("Sample assignments created across multiple subjects and students.");
    }

    private void createAssignment(String title, String description, LocalDate dueDate,
            AssignmentStatus status, Subject subject, User student, User teacher) {
        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setStatus(status);
        assignment.setSubject(subject);
        assignment.setUser(student);
        assignment.setTeacher(teacher);

        // Add some sample data for submitted/graded assignments
        if (status == AssignmentStatus.SUBMITTED) {
            assignment.setSubmissionText("This is a sample submission for " + title);
            assignment.setSubmissionDate(LocalDate.now().minusDays(1).atStartOfDay());
        } else if (status == AssignmentStatus.GRADED) {
            assignment.setSubmissionText("This is a sample submission for " + title);
            assignment.setSubmissionDate(LocalDate.now().minusDays(3).atStartOfDay());
            assignment.setGrade("A-");
            assignment.setFeedback("Excellent work! Well-structured and thorough analysis.");
        }

        assignmentRepository.save(assignment);
    }

    private void createStaffAndFees() {
        System.out.println("Creating sample staff users and fees...");

        // Create Staff Users
        User staff1 = new User();
        staff1.setUsername("staff_admin");
        staff1.setFirstName("Jennifer");
        staff1.setLastName("Adams");
        staff1.setEmail("jennifer.adams@school.com");
        staff1.setDateOfBirth(LocalDate.of(1990, 6, 30));
        staff1.setAddress("321 Staff Blvd");
        staff1.setPhoneNumber("555-0106");
        staff1.setNic("901234567V");
        staff1.setPassword(passwordEncoder.encode("staff123"));
        staff1.setRole(Role.ROLE_STAFF);
        userRepository.save(staff1);

        User staff2 = new User();
        staff2.setUsername("staff_finance");
        staff2.setFirstName("Robert");
        staff2.setLastName("Miller");
        staff2.setEmail("robert.miller@school.com");
        staff2.setDateOfBirth(LocalDate.of(1988, 11, 15));
        staff2.setAddress("456 Finance Ave");
        staff2.setPhoneNumber("555-0114");
        staff2.setNic("881234567V");
        staff2.setPassword(passwordEncoder.encode("staff123"));
        staff2.setRole(Role.ROLE_STAFF);
        userRepository.save(staff2);

        // Find students for fee creation
        User alice = userRepository.findByUsername("alice_johnson").orElse(null);
        User bob = userRepository.findByUsername("bob_williams").orElse(null);
        User charlie = userRepository.findByUsername("charlie_brown").orElse(null);
        User diana = userRepository.findByUsername("diana_smith").orElse(null);

        if (alice == null || bob == null || charlie == null || diana == null) {
            System.out.println("Students not found, skipping fee creation");
            return;
        }

        // Create comprehensive fee data for all students
        // Alice's fees
        createFee("Annual Tuition Fee 2025", alice, new BigDecimal("5000.00"),
                new BigDecimal("5000.00"), LocalDate.now().plusMonths(1), FeeStatus.PAID);
        createFee("Library Fee 2025", alice, new BigDecimal("200.00"),
                new BigDecimal("200.00"), LocalDate.now().plusMonths(2), FeeStatus.PAID);
        createFee("Sports Fee 2025", alice, new BigDecimal("300.00"),
                new BigDecimal("150.00"), LocalDate.now().plusDays(15), FeeStatus.PARTIALLY_PAID);

        // Bob's fees
        createFee("Annual Tuition Fee 2025", bob, new BigDecimal("5000.00"),
                new BigDecimal("3000.00"), LocalDate.now().plusMonths(1), FeeStatus.PARTIALLY_PAID);
        createFee("Library Fee 2025", bob, new BigDecimal("200.00"),
                new BigDecimal("0.00"), LocalDate.now().plusMonths(2), FeeStatus.UNPAID);
        createFee("Lab Fee 2025", bob, new BigDecimal("400.00"),
                new BigDecimal("400.00"), LocalDate.now().plusDays(30), FeeStatus.PAID);

        // Charlie's fees
        createFee("Annual Tuition Fee 2025", charlie, new BigDecimal("5000.00"),
                new BigDecimal("2500.00"), LocalDate.now().plusMonths(1), FeeStatus.PARTIALLY_PAID);
        createFee("Sports Fee 2025", charlie, new BigDecimal("300.00"),
                new BigDecimal("0.00"), LocalDate.now().plusDays(10), FeeStatus.OVERDUE);
        createFee("Activity Fee 2025", charlie, new BigDecimal("250.00"),
                new BigDecimal("250.00"), LocalDate.now().plusDays(45), FeeStatus.PAID);

        // Diana's fees
        createFee("Annual Tuition Fee 2025", diana, new BigDecimal("5000.00"),
                new BigDecimal("1000.00"), LocalDate.now().plusMonths(1), FeeStatus.PARTIALLY_PAID);
        createFee("Library Fee 2025", diana, new BigDecimal("200.00"),
                new BigDecimal("0.00"), LocalDate.now().minusDays(5), FeeStatus.OVERDUE);
        createFee("Lab Fee 2025", diana, new BigDecimal("400.00"),
                new BigDecimal("0.00"), LocalDate.now().plusDays(20), FeeStatus.UNPAID);
        createFee("Sports Fee 2025", diana, new BigDecimal("300.00"),
                new BigDecimal("300.00"), LocalDate.now().plusDays(25), FeeStatus.PAID);

        System.out.println("Sample staff users and comprehensive fee data created successfully!");
    }

    private void createFee(String title, User student, BigDecimal totalAmount,
            BigDecimal amountPaid, LocalDate dueDate, FeeStatus status) {
        Fee fee = new Fee();
        fee.setTitle(title);
        fee.setStudent(student);
        fee.setTotalAmount(totalAmount);
        fee.setAmountPaid(amountPaid);
        fee.setDueDate(dueDate);
        fee.setStatus(status);

        // Set reminder date for overdue fees
        if (status == FeeStatus.OVERDUE) {
            fee.setLastReminderSent(LocalDate.now().minusDays(3));
        }

        feeRepository.save(fee);
    }

    /**
     * Create the new academic structure with academic years, grade levels,
     * classrooms, and enrollments
     */
    private void createAcademicStructure() {
        System.out.println("Creating comprehensive academic structure...");

        // 1. Create Academic Years
        AcademicYear currentYear = academicYearService.createAcademicYear(
                "2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 8, 31));
        academicYearService.setActiveAcademicYear(currentYear.getId());

        AcademicYear previousYear = academicYearService.createAcademicYear(
                "2023-2024",
                LocalDate.of(2023, 9, 1),
                LocalDate.of(2024, 8, 31));

        // 2. Create Grade Levels
        GradeLevel grade1 = createGradeLevel("Grade 1", 1, "Primary education - Grade 1");
        GradeLevel grade2 = createGradeLevel("Grade 2", 2, "Primary education - Grade 2");
        GradeLevel grade3 = createGradeLevel("Grade 3", 3, "Primary education - Grade 3");
        GradeLevel grade4 = createGradeLevel("Grade 4", 4, "Primary education - Grade 4");
        GradeLevel grade5 = createGradeLevel("Grade 5", 5, "Primary education - Grade 5");
        GradeLevel grade6 = createGradeLevel("Grade 6", 6, "Junior secondary - Grade 6");
        GradeLevel grade7 = createGradeLevel("Grade 7", 7, "Junior secondary - Grade 7");
        GradeLevel grade8 = createGradeLevel("Grade 8", 8, "Junior secondary - Grade 8");
        GradeLevel grade9 = createGradeLevel("Grade 9", 9, "Junior secondary - Grade 9");
        GradeLevel grade10 = createGradeLevel("Grade 10", 10, "Senior secondary - Grade 10");
        GradeLevel grade11 = createGradeLevel("Grade 11", 11, "Advanced Level - Grade 11");
        GradeLevel grade12 = createGradeLevel("Grade 12", 12, "Advanced Level - Grade 12");

        // 3. Create Classrooms for current academic year
        // Find teachers for class teacher assignments
        User mathTeacher = userRepository.findByUsername("math_teacher").orElse(null);
        User scienceTeacher = userRepository.findByUsername("science_teacher").orElse(null);
        User englishTeacher = userRepository.findByUsername("english_teacher").orElse(null);

        // Create classrooms for different grades and mediums
        Classroom grade10A_English = createClassroom("A", Medium.ENGLISH, grade10, currentYear, mathTeacher, 35);
        Classroom grade10B_English = createClassroom("B", Medium.ENGLISH, grade10, currentYear, scienceTeacher, 35);
        Classroom grade10A_Tamil = createClassroom("A", Medium.TAMIL, grade10, currentYear, englishTeacher, 30);

        Classroom grade9A_English = createClassroom("A", Medium.ENGLISH, grade9, currentYear, mathTeacher, 40);
        Classroom grade9B_English = createClassroom("B", Medium.ENGLISH, grade9, currentYear, scienceTeacher, 40);

        // 4. Create Courses (Subject-Classroom-Teacher assignments)
        // Find subjects
        Subject mathematics = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Mathematics")).findFirst().orElse(null);
        Subject physics = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Physics")).findFirst().orElse(null);
        Subject chemistry = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Chemistry")).findFirst().orElse(null);
        Subject english = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("English Literature")).findFirst().orElse(null);
        Subject biology = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("Biology")).findFirst().orElse(null);

        if (mathematics != null && physics != null && chemistry != null && english != null && biology != null) {
            // Create courses for Grade 10A English Medium
            createCourse(mathematics, grade10A_English, mathTeacher, currentYear);
            createCourse(physics, grade10A_English, scienceTeacher, currentYear);
            createCourse(chemistry, grade10A_English, scienceTeacher, currentYear);
            createCourse(english, grade10A_English, englishTeacher, currentYear);
            createCourse(biology, grade10A_English, scienceTeacher, currentYear);

            // Create courses for Grade 10B English Medium
            createCourse(mathematics, grade10B_English, mathTeacher, currentYear);
            createCourse(physics, grade10B_English, scienceTeacher, currentYear);
            createCourse(chemistry, grade10B_English, scienceTeacher, currentYear);
            createCourse(english, grade10B_English, englishTeacher, currentYear);

            // Create courses for Grade 9A English Medium
            createCourse(mathematics, grade9A_English, mathTeacher, currentYear);
            createCourse(english, grade9A_English, englishTeacher, currentYear);
            createCourse(physics, grade9A_English, scienceTeacher, currentYear);
        }

        // 5. Enroll students in classrooms
        User alice = userRepository.findByUsername("alice_johnson").orElse(null);
        User bob = userRepository.findByUsername("bob_williams").orElse(null);
        User charlie = userRepository.findByUsername("charlie_brown").orElse(null);
        User diana = userRepository.findByUsername("diana_smith").orElse(null);

        if (alice != null && bob != null && charlie != null && diana != null) {
            try {
                // Enroll students in Grade 10A English Medium
                enrollmentService.enrollStudent(alice, grade10A_English, currentYear);
                enrollmentService.enrollStudent(diana, grade10A_English, currentYear);

                // Enroll students in Grade 10B English Medium
                enrollmentService.enrollStudent(bob, grade10B_English, currentYear);
                enrollmentService.enrollStudent(charlie, grade10B_English, currentYear);

                System.out.println("Students enrolled in classrooms successfully!");
            } catch (Exception e) {
                System.out.println("Error enrolling students: " + e.getMessage());
            }
        }

        System.out.println("Comprehensive academic structure created with:");
        System.out.println("- 2 Academic Years (2023-2024, 2024-2025)");
        System.out.println("- 12 Grade Levels (Grade 1-12)");
        System.out.println("- 5 Classrooms with different mediums");
        System.out.println("- Multiple Courses linking subjects to classrooms");
        System.out.println("- Student enrollments in appropriate classrooms");
    }

    private GradeLevel createGradeLevel(String name, Integer level, String description) {
        GradeLevel gradeLevel = new GradeLevel();
        gradeLevel.setName(name);
        gradeLevel.setLevel(level);
        gradeLevel.setDescription(description);
        return gradeLevelRepository.save(gradeLevel);
    }

    private Classroom createClassroom(String name, Medium medium, GradeLevel gradeLevel,
            AcademicYear academicYear, User classTeacher, Integer maxStudents) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setMedium(medium);
        classroom.setGradeLevel(gradeLevel);
        classroom.setAcademicYear(academicYear);
        classroom.setClassTeacher(classTeacher);
        classroom.setMaxStudents(maxStudents);
        return classroomRepository.save(classroom);
    }

    private Course createCourse(Subject subject, Classroom classroom, User teacher, AcademicYear academicYear) {
        Course course = new Course();
        course.setSubject(subject);
        course.setClassroom(classroom);
        course.setTeacher(teacher);
        course.setAcademicYear(academicYear);
        course.setActive(true);
        course.setDescription(subject.getName() + " for " + classroom.getFullName());
        return courseRepository.save(course);
    }
}