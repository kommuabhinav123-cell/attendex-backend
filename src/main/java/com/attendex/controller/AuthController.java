package com.attendex.controller;

import com.attendex.dto.StudentRegistrationDto;
import com.attendex.service.AcademicService;
import com.attendex.service.AttendanceService;
import com.attendex.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles public landing page, multi-role login, student registration, and error handling.
 */
@Controller
public class AuthController {

    private final StudentService studentService;
    private final AcademicService academicService;
    private final AttendanceService attendanceService;

    @Value("${attendex.student-registration-enabled:true}")
    private boolean registrationEnabled;

    public AuthController(StudentService studentService, AcademicService academicService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.academicService = academicService;
        this.attendanceService = attendanceService;
    }

    /** Modern landing page */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stats", attendanceService.getDashboardStats());
        return "index";
    }

    /** Login page with direct credentials entry */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /** Student registration form */
    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!registrationEnabled) {
            model.addAttribute("errorMessage", "Student registration is currently disabled by administrator.");
            return "login";
        }
        model.addAttribute("student", new StudentRegistrationDto());
        model.addAttribute("departments", academicService.findActiveDepartments());
        model.addAttribute("classSections", academicService.findActiveClassSections());
        return "register";
    }

    /** Process student registration */
    @PostMapping("/register")
    public String registerStudent(@Valid @ModelAttribute("student") StudentRegistrationDto dto,
                                  BindingResult result, Model model,
                                  RedirectAttributes redirectAttributes) {
        if (!registrationEnabled) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student registration is currently disabled.");
            return "redirect:/login";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.student", "Passwords do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", academicService.findActiveDepartments());
            model.addAttribute("classSections", academicService.findActiveClassSections());
            return "register";
        }

        try {
            studentService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! You can now log in with your credentials.");
            return "redirect:/login?role=student";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", academicService.findActiveDepartments());
            model.addAttribute("classSections", academicService.findActiveClassSections());
            return "register";
        }
    }

    /** Error page */
    @GetMapping("/error")
    public String errorPage() {
        return "error/error";
    }
}
