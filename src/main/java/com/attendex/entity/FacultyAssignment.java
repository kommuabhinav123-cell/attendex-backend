package com.attendex.entity;

import jakarta.persistence.*;

/**
 * Represents the assignment of a faculty member to teach a subject in a class section.
 * Prevents duplicate assignments via unique constraint.
 */
@Entity
@Table(name = "faculty_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id", "subject_id", "class_section_id"}))
public class FacultyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @Column(name = "assigned_date")
    private java.time.LocalDate assignedDate = java.time.LocalDate.now();

    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    public FacultyAssignment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public ClassSection getClassSection() { return classSection; }
    public void setClassSection(ClassSection classSection) { this.classSection = classSection; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public java.time.LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(java.time.LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public java.time.LocalDate getStartDate() { return startDate; }
    public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }

    public java.time.LocalDate getEndDate() { return endDate; }
    public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
}
