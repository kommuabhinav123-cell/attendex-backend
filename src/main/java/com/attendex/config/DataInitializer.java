package com.attendex.config;

import com.attendex.entity.*;
import com.attendex.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Runs on startup to create default roles, admin account,
 * and seed sample academic data (departments, courses, sections, subjects)
 * so that dropdown selectors work immediately.
 *
 * ⚠️ DEFAULT CREDENTIALS (change these immediately in production):
 *   Username: admin
 *   Password: admin123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final DepartmentRepository departmentRepo;
    private final CourseRepository courseRepo;
    private final ClassSectionRepository classSectionRepo;
    private final SubjectRepository subjectRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepo, UserRepository userRepo,
                           DepartmentRepository departmentRepo, CourseRepository courseRepo,
                           ClassSectionRepository classSectionRepo, SubjectRepository subjectRepo,
                           PasswordEncoder passwordEncoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.departmentRepo = departmentRepo;
        this.courseRepo = courseRepo;
        this.classSectionRepo = classSectionRepo;
        this.subjectRepo = subjectRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create roles if they don't exist
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_FACULTY");
        createRoleIfNotExists("ROLE_STUDENT");

        // Create default admin if no admin user exists
        if (!userRepo.existsByUsername("admin")) {
            Role adminRole = roleRepo.findByName("ROLE_ADMIN").orElseThrow();

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt hash
            admin.setEmail("admin@attendex.com");
            admin.setFullName("System Administrator");
            admin.setRoles(Collections.singleton(adminRole));
            userRepo.save(admin);

            log.warn("╔══════════════════════════════════════════════╗");
            log.warn("║  DEFAULT ADMIN ACCOUNT CREATED               ║");
            log.warn("║  Username: admin                             ║");
            log.warn("║  Password: admin123                          ║");
            log.warn("║  ⚠ CHANGE THIS PASSWORD IMMEDIATELY!        ║");
            log.warn("╚══════════════════════════════════════════════╝");
        }

        // Seed academic data if no departments exist
        if (departmentRepo.count() == 0) {
            seedAcademicData();
        }

        log.info("Attendex initialized successfully.");
    }

    /**
     * Seeds sample departments, courses, class sections, and subjects
     * so that dropdown selectors in forms are populated on first run.
     */
    private void seedAcademicData() {
        log.info("Seeding sample academic data...");

        // ── Departments ──
        Department cse = createDepartment("Computer Science & Engineering", "CSE",
                "Department of Computer Science and Engineering");
        Department ece = createDepartment("Electronics & Communication", "ECE",
                "Department of Electronics and Communication Engineering");
        Department me = createDepartment("Mechanical Engineering", "ME",
                "Department of Mechanical Engineering");
        Department ce = createDepartment("Civil Engineering", "CE",
                "Department of Civil Engineering");
        Department eee = createDepartment("Electrical & Electronics", "EEE",
                "Department of Electrical and Electronics Engineering");

        // ── Courses ──
        Course btechCSE = createCourse("B.Tech Computer Science", "BTECH-CSE", 4, cse);
        Course btechECE = createCourse("B.Tech Electronics", "BTECH-ECE", 4, ece);
        Course btechME  = createCourse("B.Tech Mechanical", "BTECH-ME", 4, me);
        Course btechCE  = createCourse("B.Tech Civil", "BTECH-CE", 4, ce);
        Course btechEEE = createCourse("B.Tech Electrical", "BTECH-EEE", 4, eee);

        // ── Class Sections (Year, Semester) ──
        // CSE sections
        ClassSection cseA1 = createSection("CSE-A", 1, 1, btechCSE);
        ClassSection cseB1 = createSection("CSE-B", 1, 1, btechCSE);
        ClassSection cseA2 = createSection("CSE-A", 2, 3, btechCSE);
        ClassSection cseB2 = createSection("CSE-B", 2, 3, btechCSE);
        ClassSection cseA3 = createSection("CSE-A", 3, 5, btechCSE);
        ClassSection cseA4 = createSection("CSE-A", 4, 7, btechCSE);

        // ECE sections
        ClassSection eceA1 = createSection("ECE-A", 1, 1, btechECE);
        ClassSection eceB1 = createSection("ECE-B", 1, 1, btechECE);
        ClassSection eceA2 = createSection("ECE-A", 2, 3, btechECE);

        // ME sections
        ClassSection meA1 = createSection("ME-A", 1, 1, btechME);
        ClassSection meA2 = createSection("ME-A", 2, 3, btechME);

        // CE sections
        ClassSection ceA1 = createSection("CE-A", 1, 1, btechCE);

        // EEE sections
        ClassSection eeeA1 = createSection("EEE-A", 1, 1, btechEEE);

        // ── Subjects (Semester 1 — common + branch-specific) ──
        // Semester 1 common subjects
        createSubject("Engineering Mathematics - I", "MA101", 4, btechCSE, 1);
        createSubject("Engineering Physics", "PH101", 3, btechCSE, 1);
        createSubject("Engineering Chemistry", "CH101", 3, btechCSE, 1);
        createSubject("Programming in C", "CS101", 4, btechCSE, 1);
        createSubject("Engineering Graphics", "ME101", 3, btechCSE, 1);
        createSubject("English Communication", "HS101", 2, btechCSE, 1);

        // Semester 3 CSE subjects
        createSubject("Data Structures", "CS201", 4, btechCSE, 3);
        createSubject("Object Oriented Programming", "CS202", 4, btechCSE, 3);
        createSubject("Discrete Mathematics", "CS203", 3, btechCSE, 3);
        createSubject("Digital Logic Design", "CS204", 3, btechCSE, 3);
        createSubject("Database Management Systems", "CS205", 4, btechCSE, 3);

        // Semester 5 CSE subjects
        createSubject("Operating Systems", "CS301", 4, btechCSE, 5);
        createSubject("Computer Networks", "CS302", 4, btechCSE, 5);
        createSubject("Software Engineering", "CS303", 3, btechCSE, 5);
        createSubject("Web Technologies", "CS304", 3, btechCSE, 5);

        // Semester 7 CSE subjects
        createSubject("Machine Learning", "CS401", 4, btechCSE, 7);
        createSubject("Cloud Computing", "CS402", 3, btechCSE, 7);

        // ECE subjects
        createSubject("Electronic Devices & Circuits", "EC201", 4, btechECE, 3);
        createSubject("Signals and Systems", "EC202", 4, btechECE, 3);
        createSubject("Network Analysis", "EC203", 3, btechECE, 3);

        // ME subjects
        createSubject("Engineering Thermodynamics", "ME201", 4, btechME, 3);
        createSubject("Strength of Materials", "ME202", 4, btechME, 3);

        // CE subjects
        createSubject("Surveying", "CE201", 4, btechCE, 3);

        // EEE subjects
        createSubject("Electrical Machines", "EE201", 4, btechEEE, 3);

        log.info("✅ Seeded {} departments, {} courses, {} sections, {} subjects",
                departmentRepo.count(), courseRepo.count(),
                classSectionRepo.count(), subjectRepo.count());
    }

    // ── Helper methods ──

    private void createRoleIfNotExists(String roleName) {
        if (roleRepo.findByName(roleName).isEmpty()) {
            roleRepo.save(new Role(roleName));
            log.info("Created role: {}", roleName);
        }
    }

    private Department createDepartment(String name, String code, String description) {
        Department dept = new Department();
        dept.setName(name);
        dept.setCode(code);
        dept.setDescription(description);
        dept.setActive(true);
        return departmentRepo.save(dept);
    }

    private Course createCourse(String name, String code, int duration, Department dept) {
        Course course = new Course();
        course.setName(name);
        course.setCode(code);
        course.setDuration(duration);
        course.setDepartment(dept);
        course.setActive(true);
        return courseRepo.save(course);
    }

    private ClassSection createSection(String name, int year, int semester, Course course) {
        ClassSection cs = new ClassSection();
        cs.setName(name);
        cs.setYear(year);
        cs.setSemester(semester);
        cs.setCourse(course);
        cs.setActive(true);
        return classSectionRepo.save(cs);
    }

    private Subject createSubject(String name, String code, int credits, Course course, int semester) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code);
        subject.setCredits(credits);
        subject.setCourse(course);
        subject.setSemester(semester);
        subject.setActive(true);
        return subjectRepo.save(subject);
    }
}
