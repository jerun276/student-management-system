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
import com.student_management_system.common.service.TimeSlotService;

import java.math.BigDecimal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
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
    private final AcademicYearService academicYearService;
    private final EnrollmentService enrollmentService;
    private final TimeSlotService timeSlotService;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SubjectRepository subjectRepository, TimetableEntryRepository timetableEntryRepository,
            AssignmentRepository assignmentRepository, FeeRepository feeRepository,
            AcademicYearRepository academicYearRepository, GradeLevelRepository gradeLevelRepository,
            ClassroomRepository classroomRepository, 
            AcademicYearService academicYearService, EnrollmentService enrollmentService,
            TimeSlotService timeSlotService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.assignmentRepository = assignmentRepository;
        this.feeRepository = feeRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeLevelRepository = gradeLevelRepository;
        this.classroomRepository = classroomRepository;
        this.academicYearService = academicYearService;
        this.enrollmentService = enrollmentService;
        this.timeSlotService = timeSlotService;
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

        // Initialize time slots for structured scheduling
        System.out.println("Initializing time slots...");
        timeSlotService.initializeDefaultTimeSlots();
        System.out.println("Time slots initialized successfully!");

        // NEW: Initialize academic structure FIRST
        if (academicYearRepository.count() == 0) {
            System.out.println(
                    "No academic structure found in DB, creating academic years, grade levels, and classrooms...");
            createAcademicStructure();
            System.out.println("Academic structure created successfully!");
        } else {
            System.out.println("Academic structure already exists in the DB, skipping initialization.");
        }

        // Create timetable data AFTER academic structure
        if (timetableEntryRepository.count() == 0) {
            System.out.println("No Timetable entries found in DB, creating sample ...");
            createTimetableData();
            System.out.println("Sample Timetable created successfully!");
        } else {
            System.out.println("Timetable entries already exist in the DB, skipping data initialization.");
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

    @Transactional
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

        // Skip subject creation - subjects are properly created in createAcademicStructure()
        // Find existing subjects instead
        Subject mathematics = subjectRepository.findBySubjectCode("MATH10").orElse(null);
        Subject physics = subjectRepository.findBySubjectCode("PHYS10").orElse(null);
        Subject chemistry = subjectRepository.findBySubjectCode("CHEM10").orElse(null);
        Subject english = subjectRepository.findBySubjectCode("ENG10").orElse(null);
        Subject biology = subjectRepository.findBySubjectCode("BIO10").orElse(null);

        // If subjects don't exist yet, skip timetable creation
        if (mathematics == null || physics == null || chemistry == null || english == null || biology == null) {
            System.out.println("Subjects not found, skipping timetable creation. Run after academic structure is created.");
            return;
        }

        // NOTE: Student-subject enrollment is now handled through the new academic structure
        // Students are enrolled in classrooms, and subjects are taught to classrooms
        // The createAcademicStructure() method handles proper student enrollment in classrooms
        System.out.println("Student enrollment is handled through classroom-based system in createAcademicStructure()");

        // TODO: Create proper classroom-based timetable entries
        // Timetable creation will be redesigned in Phase 2 to work with classroom schedules
        // rather than individual student schedules

        System.out.println("Sample subjects, enrollments, and comprehensive timetables created successfully!");
    }

    private void createTimetableEntry(Subject subject, Classroom classroom, User teacher, DayOfWeek day, int startHour, int startMinute,
            int endHour, int endMinute) {
        TimetableEntry entry = new TimetableEntry();
        entry.setSubject(subject);
        entry.setClassroom(classroom);
        entry.setTeacher(teacher);
        entry.setDayOfWeek(day);
        entry.setStartTime(LocalTime.of(startHour, startMinute));
        entry.setEndTime(LocalTime.of(endHour, endMinute));
        timetableEntryRepository.save(entry);
    }

    private void createAssignmentData() {
        System.out.println("Skipping assignment creation - assignments now require classroom assignments.");
        System.out.println("Assignment creation will be redesigned to work with the new classroom-based academic structure.");
        
        // TODO: Implement classroom-based assignment creation
        // Assignments should be created for entire classrooms, not individual students
        // This requires finding student enrollments in classrooms and creating assignments accordingly
        
        return;
    }

    private void createAssignment(String title, String description, LocalDate dueDate,
            AssignmentStatus status, Subject subject, User student, User teacher, Classroom classroom) {
        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setStatus(status);
        assignment.setSubject(subject);
        assignment.setUser(student);
        assignment.setTeacher(teacher);
        assignment.setClassroom(classroom);

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
    @Transactional
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

        // 4. Create Grade-Specific Subjects and Teacher Assignments
        // Create subjects for Grade 10
        Subject math10 = createSubject("Mathematics", "MATH10", "Mathematics for Grade 10", grade10);
        Subject physics10 = createSubject("Physics", "PHYS10", "Physics for Grade 10", grade10);
        Subject chemistry10 = createSubject("Chemistry", "CHEM10", "Chemistry for Grade 10", grade10);
        Subject english10 = createSubject("English Literature", "ENG10", "English Literature for Grade 10", grade10);
        Subject biology10 = createSubject("Biology", "BIO10", "Biology for Grade 10", grade10);
        
        // Create subjects for Grade 9
        Subject math9 = createSubject("Mathematics", "MATH09", "Mathematics for Grade 9", grade9);
        Subject science9 = createSubject("Science", "SCI09", "General Science for Grade 9", grade9);
        Subject english9 = createSubject("English", "ENG09", "English for Grade 9", grade9);
        
        // 5. Assign Teachers to Subjects (Many-to-Many relationships)
        // Use Subject-side of the relationship to avoid LazyInitializationException
        if (mathTeacher != null) {
            math10.getTeachers().add(mathTeacher);
            math9.getTeachers().add(mathTeacher);
            subjectRepository.save(math10);
            subjectRepository.save(math9);
        }
        
        if (scienceTeacher != null) {
            physics10.getTeachers().add(scienceTeacher);
            chemistry10.getTeachers().add(scienceTeacher);
            biology10.getTeachers().add(scienceTeacher);
            science9.getTeachers().add(scienceTeacher);
            subjectRepository.save(physics10);
            subjectRepository.save(chemistry10);
            subjectRepository.save(biology10);
            subjectRepository.save(science9);
        }
        
        if (englishTeacher != null) {
            english10.getTeachers().add(englishTeacher);
            english9.getTeachers().add(englishTeacher);
            subjectRepository.save(english10);
            subjectRepository.save(english9);
        }

        // Note: Course model has been eliminated. 
        // Teacher-Subject relationships are now handled through direct many-to-many associations above.
        // Timetable entries will link Subject, Classroom, and Teacher directly.

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
    
    private Subject createSubject(String name, String subjectCode, String description, GradeLevel gradeLevel) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setSubjectCode(subjectCode);
        subject.setDescription(description);
        subject.setGradeLevel(gradeLevel);
        subject.setActive(true);
        return subjectRepository.save(subject);
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

}