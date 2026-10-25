package com.attendex.controller;

import com.attendex.dto.*;
import com.attendex.entity.*;
import com.attendex.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin dashboard and management controller.
 * All URLs under /admin/** require ROLE_ADMIN.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final AcademicService academicService;
    private final ReportService reportService;

    public AdminController(AttendanceService attendanceService, StudentService studentService,
                           FacultyService facultyService, AcademicService academicService,
                           ReportService reportService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.facultyService = facultyService;
        this.academicService = academicService;
        this.reportService = reportService;
    }

    /** Admin Dashboard — summary stats */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = attendanceService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentStudents", studentService.findActive());
        model.addAttribute("recentFaculty", facultyService.findActive());
        return "admin/dashboard";
    }

    // ===== STUDENT MANAGEMENT =====

    @GetMapping("/students")
    public String listStudents(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Student> students = (search != null && !search.trim().isEmpty())
                ? studentService.searchStudents(search)
                : studentService.findAll();
        model.addAttribute("students", students);
        model.addAttribute("search", search);
        return "admin/students";
    }

    @GetMapping("/students/add")
    public String addStudentForm(Model model) {
        model.addAttribute("student", new StudentRegistrationDto());
        model.addAttribute("departments", academicService.findActiveDepartments());
        model.addAttribute("classSections", academicService.findActiveClassSections());
        return "admin/student-form";
    }

    @PostMapping("/students/add")
    public String addStudent(@ModelAttribute("student") StudentRegistrationDto dto,
                             RedirectAttributes redirectAttributes) {
        try {
            studentService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Student added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/students/{id}/edit")
    public String editStudentForm(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id);
        StudentEditDto dto = new StudentEditDto();
        dto.setId(student.getId());
        dto.setFullName(student.getUser().getFullName());
        dto.setEmail(student.getUser().getEmail());
        dto.setRollNumber(student.getRollNumber());
        dto.setEnrollmentYear(student.getEnrollmentYear());
        dto.setActive(student.isActive());
        if (student.getClassSection() != null) dto.setClassSectionId(student.getClassSection().getId());
        if (student.getDepartment() != null) dto.setDepartmentId(student.getDepartment().getId());
        dto.setParentName(student.getParentName());
        dto.setParentEmail(student.getParentEmail());
        dto.setParentPhone(student.getParentPhone());

        model.addAttribute("student", dto);
        model.addAttribute("departments", academicService.findActiveDepartments());
        model.addAttribute("classSections", academicService.findActiveClassSections());
        return "admin/student-edit";
    }

    @PostMapping("/students/{id}/edit")
    public String updateStudent(@PathVariable Long id, @ModelAttribute("student") StudentEditDto dto,
                                RedirectAttributes redirectAttributes) {
        try {
            dto.setId(id);
            studentService.updateStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/{id}/deactivate")
    public String deactivateStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student account deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/{id}/activate")
    public String activateStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student account activated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    // ===== FACULTY MANAGEMENT =====

    @GetMapping("/faculty")
    public String listFaculty(Model model) {
        model.addAttribute("facultyList", facultyService.findAll());
        return "admin/faculty";
    }

    @GetMapping("/faculty/add")
    public String addFacultyForm(Model model) {
        model.addAttribute("faculty", new FacultyDto());
        model.addAttribute("departments", academicService.findActiveDepartments());
        model.addAttribute("classSections", academicService.findActiveClassSections());
        model.addAttribute("subjects", academicService.findActiveSubjects());
        return "admin/faculty-form";
    }

    @PostMapping("/faculty/add")
    public String addFaculty(@ModelAttribute("faculty") FacultyDto dto,
                             RedirectAttributes redirectAttributes) {
        try {
            facultyService.registerFaculty(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/faculty";
    }

    @PostMapping("/faculty/{id}/deactivate")
    public String deactivateFaculty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facultyService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/faculty";
    }

    @PostMapping("/faculty/{id}/activate")
    public String activateFaculty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facultyService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member activated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/faculty";
    }

    // ===== DEPARTMENT MANAGEMENT =====

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        model.addAttribute("departments", academicService.findAllDepartments());
        return "admin/departments";
    }

    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam String name, @RequestParam String code,
                                @RequestParam(required = false) String description,
                                RedirectAttributes redirectAttributes) {
        try {
            academicService.createDepartment(name, code, description);
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ===== COURSE MANAGEMENT =====

    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", academicService.findAllCourses());
        model.addAttribute("departments", academicService.findActiveDepartments());
        return "admin/courses";
    }

    @PostMapping("/courses/add")
    public String addCourse(@RequestParam String name, @RequestParam String code,
                            @RequestParam int duration, @RequestParam Long departmentId,
                            RedirectAttributes redirectAttributes) {
        try {
            academicService.createCourse(name, code, duration, departmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Course created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    // ===== CLASS SECTION MANAGEMENT =====

    @GetMapping("/sections")
    public String listSections(Model model) {
        model.addAttribute("sections", academicService.findAllClassSections());
        model.addAttribute("courses", academicService.findActiveCourses());
        return "admin/sections";
    }

    @PostMapping("/sections/add")
    public String addSection(@RequestParam String name, @RequestParam int year,
                             @RequestParam int semester, @RequestParam Long courseId,
                             RedirectAttributes redirectAttributes) {
        try {
            academicService.createClassSection(name, year, semester, courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Class Section created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/sections";
    }

    // ===== SUBJECT MANAGEMENT =====

    @GetMapping("/subjects")
    public String listSubjects(Model model) {
        model.addAttribute("subjects", academicService.findAllSubjects());
        model.addAttribute("courses", academicService.findActiveCourses());
        return "admin/subjects";
    }

    @PostMapping("/subjects/add")
    public String addSubject(@RequestParam String name, @RequestParam String code,
                             @RequestParam int credits, @RequestParam Long courseId,
                             @RequestParam int semester, RedirectAttributes redirectAttributes) {
        try {
            academicService.createSubject(name, code, credits, courseId, semester);
            redirectAttributes.addFlashAttribute("successMessage", "Subject created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    // ===== ENROLLMENT MANAGEMENT =====

    @GetMapping("/enrollments")
    public String listEnrollments(Model model) {
        model.addAttribute("enrollments", academicService.findAllEnrollments());
        model.addAttribute("students", studentService.findActive());
        model.addAttribute("subjects", academicService.findActiveSubjects());
        return "admin/enrollments";
    }

    @PostMapping("/enrollments/add")
    public String addEnrollment(@RequestParam Long studentId, @RequestParam Long subjectId,
                                RedirectAttributes redirectAttributes) {
        try {
            academicService.enrollStudent(studentId, subjectId);
            redirectAttributes.addFlashAttribute("successMessage", "Student enrolled in subject successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // ===== FACULTY ASSIGNMENTS =====

    @GetMapping("/assignments")
    public String listAssignments(Model model) {
        model.addAttribute("assignments", academicService.findAllAssignments());
        model.addAttribute("facultyList", facultyService.findActive());
        model.addAttribute("subjects", academicService.findActiveSubjects());
        model.addAttribute("sections", academicService.findActiveClassSections());
        return "admin/assignments";
    }

    @PostMapping("/assignments/add")
    public String addAssignment(@RequestParam Long facultyId, @RequestParam Long subjectId,
                                @RequestParam Long classSectionId,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                RedirectAttributes redirectAttributes) {
        try {
            LocalDate start = (startDate != null && !startDate.trim().isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now();
            LocalDate end = (endDate != null && !endDate.trim().isEmpty()) ? LocalDate.parse(endDate) : null;
            academicService.assignFaculty(facultyId, subjectId, classSectionId, start, end);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty assigned to subject successfully with teaching dates!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/assignments";
    }

    @PostMapping("/assignments/{id}/remove")
    public String removeAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            academicService.deassignFaculty(id);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member de-assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/assignments";
    }

    // ===== REPORTS =====

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        model.addAttribute("sections", academicService.findActiveClassSections());
        model.addAttribute("subjects", academicService.findActiveSubjects());
        return "admin/reports";
    }

    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam Long classSectionId,
                                                  @RequestParam Long subjectId,
                                                  @RequestParam String startDate,
                                                  @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<AttendanceRecord> records = attendanceService.getRecordsForReport(
                classSectionId, subjectId, start, end);
        byte[] csv = reportService.generateCsv(records);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report_" + startDate + "_to_" + endDate + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    // ===== ATTENDANCE RECORDS (ADMIN VIEW) =====

    @GetMapping("/attendance")
    public String viewAttendance(@RequestParam(required = false) Long classSectionId,
                                 @RequestParam(required = false) Long subjectId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 Model model) {
        LocalDate start = (startDate != null && !startDate.trim().isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.trim().isEmpty()) ? LocalDate.parse(endDate) : null;

        List<AttendanceSession> sessions = attendanceService.getAllSessionsFiltered(classSectionId, subjectId, start, end);
        java.util.Map<Long, long[]> sessionStats = new java.util.HashMap<>();
        for (AttendanceSession s : sessions) {
            sessionStats.put(s.getId(), attendanceService.getSessionStats(s.getId()));
        }

        model.addAttribute("sessions", sessions);
        model.addAttribute("sessionStats", sessionStats);
        model.addAttribute("sections", academicService.findActiveClassSections());
        model.addAttribute("subjects", academicService.findActiveSubjects());
        return "admin/attendance";
    }

    @GetMapping("/attendance/session/{id}")
    public String viewSessionDetails(@PathVariable Long id, Model model) {
        AttendanceSession session = attendanceService.getSessionById(id);
        List<AttendanceRecord> records = attendanceService.getSessionAttendance(id);
        model.addAttribute("session", session);
        model.addAttribute("records", records);
        return "admin/attendance-session";
    }

    // ===== ADMIN PROFILE =====

    @GetMapping("/profile")
    public String adminProfile(@org.springframework.security.core.annotation.AuthenticationPrincipal com.attendex.security.CustomUserDetails userDetails,
                               Model model) {
        model.addAttribute("user", userDetails);
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "admin/profile";
    }

    @PostMapping("/profile/password")
    public String changeAdminPassword(@org.springframework.security.core.annotation.AuthenticationPrincipal com.attendex.security.CustomUserDetails userDetails,
                                      @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
                return "redirect:/admin/profile";
            }
            studentService.changePassword(userDetails.getUserId(), dto.getCurrentPassword(), dto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
