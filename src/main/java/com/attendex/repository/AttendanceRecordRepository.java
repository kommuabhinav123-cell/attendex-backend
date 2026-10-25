package com.attendex.repository;

import com.attendex.entity.AttendanceRecord;
import com.attendex.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSessionId(Long sessionId);

    List<AttendanceRecord> findByStudentId(Long studentId);

    /** Find all attendance records for a student in a specific subject */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.attendanceSession.subject.id = :subjectId ORDER BY ar.attendanceSession.sessionDate DESC")
    List<AttendanceRecord> findByStudentIdAndSubjectId(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    /** Check if attendance is already marked for a student in a session */
    boolean existsByAttendanceSessionIdAndStudentId(Long sessionId, Long studentId);

    /** Count records for a student in a subject with a specific status */
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.attendanceSession.subject.id = :subjectId AND ar.status = :status")
    long countByStudentAndSubjectAndStatus(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("status") AttendanceStatus status);

    /** Count total attendance records for a student in a subject */
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.attendanceSession.subject.id = :subjectId")
    long countByStudentAndSubject(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    /** Find records for a student within a date range */
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.attendanceSession.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.attendanceSession.sessionDate DESC")
    List<AttendanceRecord> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Find records for a class section, subject, and date range (for reports) */
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.attendanceSession.classSection.id = :classSectionId " +
           "AND ar.attendanceSession.subject.id = :subjectId " +
           "AND ar.attendanceSession.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.student.rollNumber, ar.attendanceSession.sessionDate")
    List<AttendanceRecord> findForReport(
            @Param("classSectionId") Long classSectionId,
            @Param("subjectId") Long subjectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
