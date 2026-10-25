package com.attendex.service;

import com.attendex.dto.StudentEditDto;
import com.attendex.dto.StudentRegistrationDto;
import com.attendex.entity.*;
import com.attendex.exception.DuplicateResourceException;
import com.attendex.exception.ResourceNotFoundException;
import com.attendex.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Service for managing student records.
 * Handles registration (creates both User and Student), updates, searches, and queries.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final ClassSectionRepository classSectionRepo;
    private final DepartmentRepository departmentRepo;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepo, UserRepository userRepo,
                          RoleRepository roleRepo, ClassSectionRepository classSectionRepo,
                          DepartmentRepository departmentRepo, PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.classSectionRepo = classSectionRepo;
        this.departmentRepo = departmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register a new student — creates a User account + Student profile */
    @Transactional
    public Student registerStudent(StudentRegistrationDto dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (studentRepo.findByRollNumber(dto.getRollNumber()).isPresent()) {
            throw new DuplicateResourceException("Roll number already exists: " + dto.getRollNumber());
        }

        Role studentRole = roleRepo.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_STUDENT"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setActive(true);
        user.setRoles(Collections.singleton(studentRole));
        user = userRepo.save(user);

        Student student = new Student();
        student.setRollNumber(dto.getRollNumber());
        student.setUser(user);
        student.setActive(true);
        student.setEnrollmentYear(dto.getEnrollmentYear() >= 1 && dto.getEnrollmentYear() <= 4 ? dto.getEnrollmentYear() : 1);

        if (dto.getClassSectionId() != null) {
            ClassSection cs = classSectionRepo.findById(dto.getClassSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", dto.getClassSectionId()));
            student.setClassSection(cs);
        }
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            student.setDepartment(dept);
        }

        // Parent/Guardian contact information
        student.setParentName(dto.getParentName());
        student.setParentEmail(dto.getParentEmail());
        student.setParentPhone(dto.getParentPhone());

        return studentRepo.save(student);
    }

    /** Update student profile information */
    @Transactional
    public Student updateStudent(StudentEditDto dto) {
        Student student = findById(dto.getId());

        // Validate roll number uniqueness if changed
        if (!student.getRollNumber().equalsIgnoreCase(dto.getRollNumber())) {
            if (studentRepo.findByRollNumber(dto.getRollNumber()).isPresent()) {
                throw new DuplicateResourceException("Roll number already in use: " + dto.getRollNumber());
            }
            student.setRollNumber(dto.getRollNumber());
        }

        User user = student.getUser();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        userRepo.save(user);

        student.setEnrollmentYear(dto.getEnrollmentYear() >= 1 && dto.getEnrollmentYear() <= 4 ? dto.getEnrollmentYear() : 1);
        if (dto.getClassSectionId() != null) {
            ClassSection cs = classSectionRepo.findById(dto.getClassSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", dto.getClassSectionId()));
            student.setClassSection(cs);
        } else {
            student.setClassSection(null);
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            student.setDepartment(dept);
        } else {
            student.setDepartment(null);
        }

        // Update parent/guardian contact info
        student.setParentName(dto.getParentName());
        student.setParentEmail(dto.getParentEmail());
        student.setParentPhone(dto.getParentPhone());

        return studentRepo.save(student);
    }

    public Student findById(Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    public Student findByUserId(Long userId) {
        return studentRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId));
    }

    public List<Student> findAll() { return studentRepo.findAll(); }
    public List<Student> findActive() { return studentRepo.findByActiveTrue(); }
    public List<Student> findByClassSection(Long classSectionId) { return studentRepo.findByClassSectionId(classSectionId); }
    public long countActive() { return studentRepo.countByActiveTrue(); }

    /** Search students by keyword (name, roll number, email) */
    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String kw = keyword.trim().toLowerCase();
        return studentRepo.findAll().stream()
                .filter(s -> (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(kw)) ||
                             (s.getUser() != null && s.getUser().getFullName() != null && s.getUser().getFullName().toLowerCase().contains(kw)) ||
                             (s.getUser() != null && s.getUser().getEmail() != null && s.getUser().getEmail().toLowerCase().contains(kw)))
                .toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Student student = findById(id);
        student.setActive(false);
        student.getUser().setActive(false);
        studentRepo.save(student);
    }

    @Transactional
    public void activate(Long id) {
        Student student = findById(id);
        student.setActive(true);
        student.getUser().setActive(true);
        studentRepo.save(student);
    }

    /** Change student account password securely */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    /** Update parent contact details from student profile */
    @Transactional
    public void updateParentContact(Long userId, String parentName, String parentPhone) {
        Student student = findByUserId(userId);
        student.setParentName(parentName);
        student.setParentPhone(parentPhone);
        studentRepo.save(student);
    }
}
