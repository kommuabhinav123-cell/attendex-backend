package com.attendex.dto;

import com.attendex.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for marking a single student's attendance in a session.
 */
public class AttendanceEntryDto {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String remarks;

    // Also store the student name and roll number for display
    private String studentName;
    private String rollNumber;

    public AttendanceEntryDto() {}

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
}
