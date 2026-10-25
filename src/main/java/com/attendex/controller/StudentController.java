package com.attendex.controller;

import com.attendex.dto.AttendancePercentageDto;
import com.attendex.dto.ChangePasswordDto;
import com.attendex.entity.*;
import com.attendex.security.CustomUserDetails;
import com.attendex.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Student dashboard, attendance history, profile, and security settings.
 * All URLs under /student/** require ROLE_STUDENT.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final AcademicService academicService;

    @Value("${attendex.min-attendance-percentage:75}")
    private int minAttendancePercentage;

    public StudentController(StudentService studentService,
                             AttendanceService attendanceService,
                             AcademicService academicService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.academicService = academicService;
    }

    /** Student Dashboard — attendance summary */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Student student = studentService.findByUserId(userDetails.getUserId());
        List<AttendancePercentageDto> subjectWise = attendanceService.getStudentSummary(student.getId());
        double overallPercentage = attendanceService.getOverallPercentage(student.getId());
        List<AttendanceRecord> recentRecords = attendanceService.getStudentAttendance(student.getId(), null);
        if (recentRecords.size() > 5) {
            recentRecords = recentRecords.subList(0, 5);
        }

        model.addAttribute("student", student);
        model.addAttribute("subjectWise", subjectWise);
        model.addAttribute("overallPercentage", overallPercentage);
        model.addAttribute("recentRecords", recentRecords);
        model.addAttribute("minPercentage", minAttendancePercentage);
        model.addAttribute("showWarning", overallPercentage > 0 && overallPercentage < minAttendancePercentage);
        return "student/dashboard";
    }

    /** View attendance details for a specific subject */
    @GetMapping("/attendance/{subjectId}")
    public String viewSubjectAttendance(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable Long subjectId, Model model) {
        Student student = studentService.findByUserId(userDetails.getUserId());
        List<AttendanceRecord> records = attendanceService.getStudentAttendance(student.getId(), subjectId);
        AttendancePercentageDto percentage = attendanceService.calculatePercentage(student.getId(), subjectId);

        model.addAttribute("student", student);
        model.addAttribute("records", records);
        model.addAttribute("percentage", percentage);
        model.addAttribute("minPercentage", minAttendancePercentage);
        return "student/subject-attendance";
    }

    /** View student profile */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Student student = studentService.findByUserId(userDetails.getUserId());
        List<Enrollment> enrollments = academicService.findEnrollmentsByStudent(student.getId());

        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "student/profile";
    }

    /** Change student account password */
    @PostMapping("/password/change")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.dto", "New passwords do not match");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password change failed. Please check the inputs.");
            return "redirect:/student/profile";
        }

        try {
            studentService.changePassword(userDetails.getUserId(), dto.getCurrentPassword(), dto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/profile";
    }

    /** Update parent/guardian contact details */
    @PostMapping("/contact/update")
    public String updateParentContact(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam String parentName,
                                      @RequestParam String parentPhone,
                                      RedirectAttributes redirectAttributes) {
        try {
            studentService.updateParentContact(userDetails.getUserId(), parentName, parentPhone);
            redirectAttributes.addFlashAttribute("successMessage", "Parent contact details updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/profile";
    }
}
