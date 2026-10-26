package com.attendex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a class section (e.g., CS-A, CS-B) within a course.
 */
@Entity
@Table(name = "class_sections")
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // e.g., "CS-A", "CS-B"

    @Column(name = "academic_year", nullable = false)
    private int year; // current academic year (1, 2, 3, 4)

    @Column(nullable = false)
    private int semester; // current semester (1-8)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ClassSection() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
