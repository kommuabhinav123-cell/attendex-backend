package com.attendex.service;

import com.attendex.dto.FacultyDto;
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
 * Service for managing faculty records.
 */
@Service
public class FacultyService {

    private final FacultyRepository facultyRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final ClassSectionRepository classSectionRepo;
    private final SubjectRepository subjectRepo;
    private final FacultyAssignmentRepository assignmentRepo;
    private final PasswordEncoder passwordEncoder;

    public FacultyService(FacultyRepository facultyRepo, UserRepository userRepo,
                          RoleRepository roleRepo, DepartmentRepository departmentRepo,
                          ClassSectionRepository classSectionRepo, SubjectRepository subjectRepo,
                          FacultyAssignmentRepository assignmentRepo,
                          PasswordEncoder passwordEncoder) {
        this.facultyRepo = facultyRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.departmentRepo = departmentRepo;
        this.classSectionRepo = classSectionRepo;
        this.subjectRepo = subjectRepo;
        this.assignmentRepo = assignmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register a new faculty member — creates User + Faculty profile */
    @Transactional
    public Faculty registerFaculty(FacultyDto dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (facultyRepo.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
            throw new DuplicateResourceException("Employee ID already exists: " + dto.getEmployeeId());
        }

        Role facultyRole = roleRepo.findByName("ROLE_FACULTY")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_FACULTY"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setActive(true);
        user.setRoles(Collections.singleton(facultyRole));
        user = userRepo.save(user);

        Faculty faculty = new Faculty();
        faculty.setEmployeeId(dto.getEmployeeId());
        faculty.setUser(user);
        faculty.setActive(true);
        faculty.setDesignation(dto.getDesignation());

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            faculty.setDepartment(dept);
        }

        Faculty savedFaculty = facultyRepo.save(faculty);

        // Optional: Assign to Class Section and Subject upon creation
        if (dto.getClassSectionId() != null && dto.getSubjectId() != null) {
            ClassSection cs = classSectionRepo.findById(dto.getClassSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", dto.getClassSectionId()));
            Subject subject = subjectRepo.findById(dto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", dto.getSubjectId()));

            FacultyAssignment assignment = new FacultyAssignment();
            assignment.setFaculty(savedFaculty);
            assignment.setClassSection(cs);
            assignment.setSubject(subject);
            assignment.setActive(true);
            assignmentRepo.save(assignment);
        }

        return savedFaculty;
    }

    /** Update faculty details */
    @Transactional
    public Faculty updateFaculty(Long facultyId, String fullName, String email, String designation, Long departmentId) {
        Faculty faculty = findById(facultyId);
        User user = faculty.getUser();
        user.setFullName(fullName);
        user.setEmail(email);
        userRepo.save(user);

        faculty.setDesignation(designation);
        if (departmentId != null) {
            Department dept = departmentRepo.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            faculty.setDepartment(dept);
        } else {
            faculty.setDepartment(null);
        }
        return facultyRepo.save(faculty);
    }

    public Faculty findById(Long id) {
        return facultyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
    }

    public Faculty findByUserId(Long userId) {
        return facultyRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "userId", userId));
    }

    public List<Faculty> findAll() { return facultyRepo.findAll(); }
    public List<Faculty> findActive() { return facultyRepo.findByActiveTrue(); }
    public long countActive() { return facultyRepo.countByActiveTrue(); }

    @Transactional
    public void deactivate(Long id) {
        Faculty faculty = findById(id);
        faculty.setActive(false);
        faculty.getUser().setActive(false);
        facultyRepo.save(faculty);
    }

    @Transactional
    public void activate(Long id) {
        Faculty faculty = findById(id);
        faculty.setActive(true);
        faculty.getUser().setActive(true);
        facultyRepo.save(faculty);
    }
}
