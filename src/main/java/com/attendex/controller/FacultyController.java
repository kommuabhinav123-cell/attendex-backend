package com.attendex.controller;

import com.attendex.dto.AttendanceEntryDto;
import com.attendex.dto.LowAttendanceStudentDto;
import com.attendex.entity.*;
import com.attendex.security.CustomUserDetails;
import com.attendex.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Faculty dashboard and attendance management controller.
 * All URLs under /faculty/** require ROLE_FACULTY.
 */
@Controller
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;
    private final AcademicService academicService;
    private final AttendanceService attendanceService;
    private final StudentService studentService;

    @Value("${attendex.min-attendance-percentage:75}")
    private double minAttendancePercentage;

    public FacultyController(FacultyService facultyService, AcademicService academicService,
                             AttendanceService attendanceService, StudentService studentService) {
        this.facultyService = facultyService;
        this.academicService = academicService;
        this.attendanceService = attendanceService;
        this.studentService = studentService;
    }

    /** Faculty Dashboard — show assigned classes & low attendance alerts */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        List<FacultyAssignment> assignments = academicService.findAssignmentsByFaculty(faculty.getId());
        List<AttendanceSession> recentSessions = attendanceService.getSessionsByFaculty(faculty.getId());
        List<LowAttendanceStudentDto> lowAttendanceList = attendanceService.getLowAttendanceStudentsForFaculty(
                faculty.getId(), minAttendancePercentage);

        model.addAttribute("faculty", faculty);
        model.addAttribute("assignments", assignments);
        model.addAttribute("recentSessions", recentSessions);
        model.addAttribute("lowAttendanceList", lowAttendanceList);
        model.addAttribute("minPercentage", minAttendancePercentage);
        return "faculty/dashboard";
    }

    /** Enroll / claim teaching subject by faculty */
    @GetMapping("/subjects/enroll")
    public String enrollSubjectForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        List<FacultyAssignment> myAssignments = academicService.findAssignmentsByFaculty(faculty.getId());
        List<Subject> subjects = academicService.findActiveSubjects();
        List<ClassSection> sections = academicService.findActiveClassSections();

        model.addAttribute("faculty", faculty);
        model.addAttribute("myAssignments", myAssignments);
        model.addAttribute("subjects", subjects);
        model.addAttribute("sections", sections);
        model.addAttribute("today", LocalDate.now());
        return "faculty/enroll-subject";
    }

    @PostMapping("/subjects/enroll")
    public String enrollSubject(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam Long subjectId,
                                @RequestParam Long classSectionId,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                RedirectAttributes redirectAttributes) {
        try {
            Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
            LocalDate start = (startDate != null && !startDate.trim().isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now();
            LocalDate end = (endDate != null && !endDate.trim().isEmpty()) ? LocalDate.parse(endDate) : null;
            academicService.assignFaculty(faculty.getId(), subjectId, classSectionId, start, end);
            redirectAttributes.addFlashAttribute("successMessage", "Teaching subject enrolled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/subjects/{assignmentId}/drop")
    public String dropSubject(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long assignmentId,
                              RedirectAttributes redirectAttributes) {
        try {
            Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
            FacultyAssignment assignment = academicService.findAssignmentsByFaculty(faculty.getId()).stream()
                    .filter(a -> a.getId().equals(assignmentId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found for your account."));
            academicService.deassignFaculty(assignment.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Teaching subject de-assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    /** Show form to create an attendance session and mark attendance */
    @GetMapping("/attendance/mark")
    public String markAttendanceForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam(required = false) Long assignmentId,
                                     Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        List<FacultyAssignment> assignments = academicService.findAssignmentsByFaculty(faculty.getId());
        model.addAttribute("assignments", assignments);

        if (assignmentId != null) {
            FacultyAssignment assignment = assignments.stream()
                    .filter(a -> a.getId().equals(assignmentId))
                    .findFirst().orElse(null);

            if (assignment != null) {
                List<Student> students = studentService.findByClassSection(
                        assignment.getClassSection().getId());

                List<AttendanceEntryDto> entries = new ArrayList<>();
                for (Student s : students) {
                    AttendanceEntryDto entry = new AttendanceEntryDto();
                    entry.setStudentId(s.getId());
                    entry.setStudentName(s.getUser().getFullName());
                    entry.setRollNumber(s.getRollNumber());
                    entry.setStatus(AttendanceStatus.PRESENT);
                    entries.add(entry);
                }

                model.addAttribute("selectedAssignment", assignment);
                model.addAttribute("entries", entries);
                model.addAttribute("today", LocalDate.now());
            }
        }

        return "faculty/mark-attendance";
    }

    /** Process attendance marking */
    @PostMapping("/attendance/mark")
    public String markAttendance(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam Long subjectId,
                                 @RequestParam Long classSectionId,
                                 @RequestParam String sessionDate,
                                 @RequestParam int sessionNumber,
                                 @RequestParam List<Long> studentIds,
                                 @RequestParam List<String> statuses,
                                 RedirectAttributes redirectAttributes) {
        try {
            Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
            LocalDate date = LocalDate.parse(sessionDate);

            AttendanceSession session = attendanceService.createSession(
                    subjectId, classSectionId, faculty.getId(), date, sessionNumber);

            List<AttendanceEntryDto> entries = new ArrayList<>();
            for (int i = 0; i < studentIds.size(); i++) {
                AttendanceEntryDto entry = new AttendanceEntryDto();
                entry.setStudentId(studentIds.get(i));
                entry.setStatus(AttendanceStatus.valueOf(statuses.get(i)));
                entries.add(entry);
            }

            attendanceService.markAttendance(session.getId(), entries);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Attendance marked successfully for " + entries.size() + " students!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/dashboard";
    }

    /** View attendance records for a session with optional update */
    @GetMapping("/attendance/view/{sessionId}")
    public String viewSessionAttendance(@PathVariable Long sessionId, Model model) {
        List<AttendanceRecord> records = attendanceService.getSessionAttendance(sessionId);
        model.addAttribute("records", records);
        if (!records.isEmpty()) {
            model.addAttribute("session", records.get(0).getAttendanceSession());
        } else {
            model.addAttribute("session", attendanceService.getSessionById(sessionId));
        }
        return "faculty/view-attendance";
    }

    /** Edit single attendance record */
    @PostMapping("/attendance/record/{recordId}/update")
    public String updateAttendanceRecord(@PathVariable Long recordId,
                                         @RequestParam String status,
                                         @RequestParam(required = false) String remarks,
                                         @RequestParam Long sessionId,
                                         RedirectAttributes redirectAttributes) {
        try {
            attendanceService.updateRecord(recordId, AttendanceStatus.valueOf(status), remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Record updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/attendance/view/" + sessionId;
    }

    /** Close session */
    @PostMapping("/attendance/session/{sessionId}/close")
    public String closeSession(@PathVariable Long sessionId, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.closeSession(sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Session closed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/attendance/view/" + sessionId;
    }

    /** View all sessions for faculty with optional filtering */
    @GetMapping("/sessions")
    public String viewSessions(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) Long subjectId,
                               @RequestParam(required = false) Long classSectionId,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        List<AttendanceSession> sessions = attendanceService.getSessionsByFacultyFiltered(
                faculty.getId(), subjectId, classSectionId, start, end);
        List<FacultyAssignment> assignments = academicService.findAssignmentsByFaculty(faculty.getId());

        model.addAttribute("sessions", sessions);
        model.addAttribute("assignments", assignments);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedClassSectionId", classSectionId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "faculty/sessions";
    }

    /** Low attendance overview page */
    @GetMapping("/low-attendance")
    public String lowAttendancePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        List<LowAttendanceStudentDto> lowAttendanceList = attendanceService.getLowAttendanceStudentsForFaculty(
                faculty.getId(), minAttendancePercentage);

        model.addAttribute("lowAttendanceList", lowAttendanceList);
        model.addAttribute("minPercentage", minAttendancePercentage);
        return "faculty/low-attendance";
    }

    /** Faculty Profile & Password Settings */
    @GetMapping("/profile")
    public String facultyProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Faculty faculty = facultyService.findByUserId(userDetails.getUserId());
        List<FacultyAssignment> assignments = academicService.findAssignmentsByFaculty(faculty.getId());

        model.addAttribute("faculty", faculty);
        model.addAttribute("assignments", assignments);
        model.addAttribute("changePasswordDto", new com.attendex.dto.ChangePasswordDto());
        return "faculty/profile";
    }

    @PostMapping("/password/change")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @ModelAttribute("changePasswordDto") com.attendex.dto.ChangePasswordDto dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
                return "redirect:/faculty/profile";
            }
            studentService.changePassword(userDetails.getUserId(), dto.getCurrentPassword(), dto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/profile";
    }
}
