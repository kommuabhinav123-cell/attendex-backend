package com.attendex.dto;

public class LowAttendanceStudentDto {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String subjectName;
    private String subjectCode;
    private double percentage;
    private long totalSessions;
    private long attendedSessions;

    public LowAttendanceStudentDto() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getAttendedSessions() { return attendedSessions; }
    public void setAttendedSessions(long attendedSessions) { this.attendedSessions = attendedSessions; }
}
