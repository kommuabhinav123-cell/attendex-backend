package com.attendex.dto;

/**
 * DTO for admin dashboard statistics cards.
 */
public class DashboardStatsDto {

    private long totalStudents;
    private long totalFaculty;
    private long totalSubjects;
    private long totalDepartments;
    private double overallAttendancePercentage;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalStudents, long totalFaculty, long totalSubjects,
                             long totalDepartments, double overallAttendancePercentage) {
        this.totalStudents = totalStudents;
        this.totalFaculty = totalFaculty;
        this.totalSubjects = totalSubjects;
        this.totalDepartments = totalDepartments;
        this.overallAttendancePercentage = overallAttendancePercentage;
    }

    // Getters and Setters
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalFaculty() { return totalFaculty; }
    public void setTotalFaculty(long totalFaculty) { this.totalFaculty = totalFaculty; }

    public long getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }

    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }

    public double getOverallAttendancePercentage() { return overallAttendancePercentage; }
    public void setOverallAttendancePercentage(double overallAttendancePercentage) { this.overallAttendancePercentage = overallAttendancePercentage; }
}
