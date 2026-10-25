package com.attendex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a single attendance record — one student's status in one session.
 * Unique constraint on (session, student) prevents duplicate marking.
 */
@Entity
@Table(name = "attendance_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"attendance_session_id", "student_id"}))
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(length = 200)
    private String remarks;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @PrePersist
    protected void onCreate() { this.markedAt = LocalDateTime.now(); }

    public AttendanceRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AttendanceSession getAttendanceSession() { return attendanceSession; }
    public void setAttendanceSession(AttendanceSession attendanceSession) { this.attendanceSession = attendanceSession; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getMarkedAt() { return markedAt; }
}
