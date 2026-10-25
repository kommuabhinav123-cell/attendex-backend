package com.attendex.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for creating/editing faculty via admin panel.
 */
public class FacultyDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password; // nullable for edit mode

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private Long departmentId;

    private Long classSectionId;

    private Long subjectId;

    @Size(max = 100)
    private String designation;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getClassSectionId() { return classSectionId; }
    public void setClassSectionId(Long classSectionId) { this.classSectionId = classSectionId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
