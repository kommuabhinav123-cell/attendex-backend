package com.attendex.service;

import com.attendex.entity.*;
import com.attendex.exception.DuplicateResourceException;
import com.attendex.exception.ResourceNotFoundException;
import com.attendex.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing departments, courses, class sections, subjects,
 * enrollments, and faculty assignments.
 */
@Service
public class AcademicService {

    private final DepartmentRepository departmentRepo;
    private final CourseRepository courseRepo;
    private final ClassSectionRepository classSectionRepo;
    private final SubjectRepository subjectRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final FacultyAssignmentRepository assignmentRepo;
    private final StudentRepository studentRepo;
    private final FacultyRepository facultyRepo;

    public AcademicService(DepartmentRepository departmentRepo, CourseRepository courseRepo,
                           ClassSectionRepository classSectionRepo, SubjectRepository subjectRepo,
                           EnrollmentRepository enrollmentRepo, FacultyAssignmentRepository assignmentRepo,
                           StudentRepository studentRepo, FacultyRepository facultyRepo) {
        this.departmentRepo = departmentRepo;
        this.courseRepo = courseRepo;
        this.classSectionRepo = classSectionRepo;
        this.subjectRepo = subjectRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.assignmentRepo = assignmentRepo;
        this.studentRepo = studentRepo;
        this.facultyRepo = facultyRepo;
    }

    // ===== DEPARTMENTS =====

    @Transactional
    public Department createDepartment(String name, String code, String description) {
        if (departmentRepo.findByCode(code.toUpperCase()).isPresent()) {
            throw new DuplicateResourceException("Department code already exists: " + code);
        }
        Department dept = new Department();
        dept.setName(name);
        dept.setCode(code.toUpperCase());
        dept.setDescription(description);
        dept.setActive(true);
        return departmentRepo.save(dept);
    }

    public Department findDepartmentById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    public List<Department> findAllDepartments() { return departmentRepo.findAll(); }
    public List<Department> findActiveDepartments() { return departmentRepo.findByActiveTrue(); }

    // ===== COURSES =====

    @Transactional
    public Course createCourse(String name, String code, int duration, Long departmentId) {
        if (courseRepo.findByCode(code.toUpperCase()).isPresent()) {
            throw new DuplicateResourceException("Course code already exists: " + code);
        }
        Department dept = findDepartmentById(departmentId);
        Course course = new Course();
        course.setName(name);
        course.setCode(code.toUpperCase());
        course.setDuration(duration);
        course.setDepartment(dept);
        course.setActive(true);
        return courseRepo.save(course);
    }

    public Course findCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    public List<Course> findAllCourses() { return courseRepo.findAll(); }
    public List<Course> findActiveCourses() { return courseRepo.findByActiveTrue(); }
    public List<Course> findCoursesByDepartment(Long deptId) { return courseRepo.findByDepartmentId(deptId); }

    // ===== CLASS SECTIONS =====

    @Transactional
    public ClassSection createClassSection(String name, int year, int semester, Long courseId) {
        Course course = findCourseById(courseId);
        ClassSection cs = new ClassSection();
        cs.setName(name);
        cs.setYear(year);
        cs.setSemester(semester);
        cs.setCourse(course);
        cs.setActive(true);
        return classSectionRepo.save(cs);
    }

    public ClassSection findClassSectionById(Long id) {
        return classSectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", id));
    }

    public List<ClassSection> findAllClassSections() { return classSectionRepo.findAll(); }
    public List<ClassSection> findActiveClassSections() { return classSectionRepo.findByActiveTrue(); }
    public List<ClassSection> findClassSectionsByCourse(Long courseId) { return classSectionRepo.findByCourseId(courseId); }

    // ===== SUBJECTS =====

    @Transactional
    public Subject createSubject(String name, String code, int credits, Long courseId, int semester) {
        if (subjectRepo.findByCode(code.toUpperCase()).isPresent()) {
            throw new DuplicateResourceException("Subject code already exists: " + code);
        }
        Course course = findCourseById(courseId);
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code.toUpperCase());
        subject.setCredits(credits);
        subject.setCourse(course);
        subject.setSemester(semester);
        subject.setActive(true);
        return subjectRepo.save(subject);
    }

    public Subject findSubjectById(Long id) {
        return subjectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    public List<Subject> findAllSubjects() { return subjectRepo.findAll(); }
    public List<Subject> findActiveSubjects() { return subjectRepo.findByActiveTrue(); }
    public List<Subject> findSubjectsByCourse(Long courseId) { return subjectRepo.findByCourseId(courseId); }

    // ===== ENROLLMENTS =====

    @Transactional
    public Enrollment enrollStudent(Long studentId, Long subjectId) {
        if (enrollmentRepo.existsByStudentIdAndSubjectId(studentId, subjectId)) {
            throw new DuplicateResourceException("Student is already enrolled in this subject");
        }
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        enrollment.setEnrolledDate(LocalDate.now());
        enrollment.setActive(true);
        return enrollmentRepo.save(enrollment);
    }

    public List<Enrollment> findEnrollmentsByStudent(Long studentId) {
        return enrollmentRepo.findByStudentIdAndActiveTrue(studentId);
    }

    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepo.findAll();
    }

    // ===== FACULTY ASSIGNMENTS =====

    @Transactional
    public FacultyAssignment assignFaculty(Long facultyId, Long subjectId, Long classSectionId) {
        return assignFaculty(facultyId, subjectId, classSectionId, null, null);
    }

    @Transactional
    public FacultyAssignment assignFaculty(Long facultyId, Long subjectId, Long classSectionId, LocalDate startDate, LocalDate endDate) {
        if (assignmentRepo.existsByFacultyIdAndSubjectIdAndClassSectionId(facultyId, subjectId, classSectionId)) {
            throw new DuplicateResourceException("This faculty assignment already exists");
        }
        Faculty faculty = facultyRepo.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", facultyId));
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));
        ClassSection cs = classSectionRepo.findById(classSectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", classSectionId));

        FacultyAssignment assignment = new FacultyAssignment();
        assignment.setFaculty(faculty);
        assignment.setSubject(subject);
        assignment.setClassSection(cs);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStartDate(startDate != null ? startDate : LocalDate.now());
        assignment.setEndDate(endDate);
        assignment.setActive(true);
        return assignmentRepo.save(assignment);
    }

    @Transactional
    public void deassignFaculty(Long assignmentId) {
        FacultyAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("FacultyAssignment", "id", assignmentId));
        assignmentRepo.delete(assignment);
    }

    public List<FacultyAssignment> findAssignmentsByFaculty(Long facultyId) {
        return assignmentRepo.findByFacultyIdAndActiveTrue(facultyId);
    }

    public List<FacultyAssignment> findAllAssignments() {
        return assignmentRepo.findAll();
    }

    public List<FacultyAssignment> findAssignmentsByClassSection(Long classSectionId) {
        return assignmentRepo.findByClassSectionId(classSectionId);
    }
}
