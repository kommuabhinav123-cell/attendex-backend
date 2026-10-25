package com.attendex.service;

import com.attendex.dto.DashboardStatsDto;
import com.attendex.dto.AttendancePercentageDto;
import com.attendex.dto.AttendanceEntryDto;
import com.attendex.dto.LowAttendanceStudentDto;
import com.attendex.entity.*;
import com.attendex.exception.DuplicateResourceException;
import com.attendex.exception.ResourceNotFoundException;
import com.attendex.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core attendance business logic.
 * Handles session creation, attendance marking, percentage calculation, reporting,
 * and low attendance tracking.
 */
@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepo;
    private final AttendanceRecordRepository recordRepo;
    private final StudentRepository studentRepo;
    private final SubjectRepository subjectRepo;
    private final ClassSectionRepository classSectionRepo;
    private final FacultyRepository facultyRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final FacultyAssignmentRepository assignmentRepo;
    private final DepartmentRepository departmentRepo;
    private final SmsNotificationService smsService;

    public AttendanceService(AttendanceSessionRepository sessionRepo,
                             AttendanceRecordRepository recordRepo,
                             StudentRepository studentRepo,
                             SubjectRepository subjectRepo,
                             ClassSectionRepository classSectionRepo,
                             FacultyRepository facultyRepo,
                             EnrollmentRepository enrollmentRepo,
                             FacultyAssignmentRepository assignmentRepo,
                             DepartmentRepository departmentRepo,
                             SmsNotificationService smsService) {
        this.sessionRepo = sessionRepo;
        this.recordRepo = recordRepo;
        this.studentRepo = studentRepo;
        this.subjectRepo = subjectRepo;
        this.classSectionRepo = classSectionRepo;
        this.facultyRepo = facultyRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.assignmentRepo = assignmentRepo;
        this.departmentRepo = departmentRepo;
        this.smsService = smsService;
    }

    /** Create a new attendance session */
    @Transactional
    public AttendanceSession createSession(Long subjectId, Long classSectionId,
                                           Long facultyId, LocalDate date, int sessionNumber) {
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));
        ClassSection cs = classSectionRepo.findById(classSectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection", "id", classSectionId));
        Faculty faculty = facultyRepo.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", facultyId));

        // Check if session already exists for this combination
        List<AttendanceSession> existing = sessionRepo
                .findBySubjectIdAndClassSectionIdAndSessionDate(subjectId, classSectionId, date);
        for (AttendanceSession s : existing) {
            if (s.getSessionNumber() == sessionNumber) {
                throw new DuplicateResourceException(
                    "Attendance session already exists for this subject, class, date, and period.");
            }
        }

        AttendanceSession session = new AttendanceSession();
        session.setSubject(subject);
        session.setClassSection(cs);
        session.setFaculty(faculty);
        session.setSessionDate(date);
        session.setSessionNumber(sessionNumber);
        session.setStatus(SessionStatus.OPEN);

        return sessionRepo.save(session);
    }

    /** Mark attendance for multiple students in a session */
    @Transactional
    public void markAttendance(Long sessionId, List<AttendanceEntryDto> entries) {
        AttendanceSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        for (AttendanceEntryDto entry : entries) {
            if (recordRepo.existsByAttendanceSessionIdAndStudentId(sessionId, entry.getStudentId())) {
                continue;
            }

            Student student = studentRepo.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", entry.getStudentId()));

            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceSession(session);
            record.setStudent(student);
            record.setStatus(entry.getStatus());
            record.setRemarks(entry.getRemarks());
            record = recordRepo.save(record);

            // Send SMS to parent if student is ABSENT
            if (entry.getStatus() == AttendanceStatus.ABSENT) {
                smsService.sendAbsenceAlert(record);
            }
        }
    }

    /** Close a session to prevent further edits */
    @Transactional
    public void closeSession(Long sessionId) {
        AttendanceSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
        session.setStatus(SessionStatus.CLOSED);
        sessionRepo.save(session);
    }

    /** Get all attendance records for a session */
    public List<AttendanceRecord> getSessionAttendance(Long sessionId) {
        return recordRepo.findByAttendanceSessionId(sessionId);
    }

    public AttendanceSession getSessionById(Long sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
    }

    /** Get attendance records for a student, optionally filtered by subject */
    public List<AttendanceRecord> getStudentAttendance(Long studentId, Long subjectId) {
        if (subjectId != null) {
            return recordRepo.findByStudentIdAndSubjectId(studentId, subjectId);
        }
        return recordRepo.findByStudentId(studentId);
    }

    /**
     * Calculate attendance percentage for a student in a specific subject.
     * Percentage = (Present + Late) / Total * 100
     */
    public AttendancePercentageDto calculatePercentage(Long studentId, Long subjectId) {
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        long total = recordRepo.countByStudentAndSubject(studentId, subjectId);
        long present = recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.PRESENT);
        long late = recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.LATE);
        long absent = recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.ABSENT);

        double percentage = total > 0 ? ((double) (present + late) / total) * 100.0 : 0.0;

        AttendancePercentageDto dto = new AttendancePercentageDto();
        dto.setSubjectId(subjectId);
        dto.setSubjectName(subject.getName());
        dto.setSubjectCode(subject.getCode());
        dto.setTotalSessions(total);
        dto.setPresentCount(present);
        dto.setLateCount(late);
        dto.setAbsentCount(absent);
        dto.setPercentage(Math.round(percentage * 100.0) / 100.0);
        return dto;
    }

    /**
     * Get attendance summary for a student across all enrolled subjects.
     */
    public List<AttendancePercentageDto> getStudentSummary(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepo.findByStudentIdAndActiveTrue(studentId);
        List<AttendancePercentageDto> summary = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            summary.add(calculatePercentage(studentId, enrollment.getSubject().getId()));
        }
        return summary;
    }

    /**
     * Calculate overall attendance percentage across all subjects for a student.
     */
    public double getOverallPercentage(Long studentId) {
        List<AttendancePercentageDto> summary = getStudentSummary(studentId);
        if (summary.isEmpty()) return 0.0;

        long totalAll = 0;
        long presentAll = 0;
        for (AttendancePercentageDto dto : summary) {
            totalAll += dto.getTotalSessions();
            presentAll += dto.getPresentCount() + dto.getLateCount();
        }
        return totalAll > 0 ? Math.round(((double) presentAll / totalAll) * 10000.0) / 100.0 : 0.0;
    }

    /** Get dashboard statistics for admin */
    public DashboardStatsDto getDashboardStats() {
        long students = studentRepo.countByActiveTrue();
        long faculty = facultyRepo.countByActiveTrue();
        long subjects = subjectRepo.countByActiveTrue();
        long departments = departmentRepo.findByActiveTrue().size();

        return new DashboardStatsDto(students, faculty, subjects, departments, 0);
    }

    /** Get all sessions with optional filters for admin view */
    public List<AttendanceSession> getAllSessionsFiltered(Long classSectionId, Long subjectId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceSession> list = sessionRepo.findAll();
        return list.stream()
                .filter(s -> classSectionId == null || s.getClassSection().getId().equals(classSectionId))
                .filter(s -> subjectId == null || s.getSubject().getId().equals(subjectId))
                .filter(s -> startDate == null || !s.getSessionDate().isBefore(startDate))
                .filter(s -> endDate == null || !s.getSessionDate().isAfter(endDate))
                .sorted((a, b) -> b.getSessionDate().compareTo(a.getSessionDate()))
                .toList();
    }

    /** Count present and absent counts for a session */
    public long[] getSessionStats(Long sessionId) {
        List<AttendanceRecord> records = recordRepo.findByAttendanceSessionId(sessionId);
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        return new long[]{present, absent};
    }

    /** Get sessions for a faculty member */
    public List<AttendanceSession> getSessionsByFaculty(Long facultyId) {
        return sessionRepo.findByFacultyId(facultyId);
    }

    /** Filter sessions for faculty with optional filters */
    public List<AttendanceSession> getSessionsByFacultyFiltered(Long facultyId, Long subjectId, Long classSectionId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceSession> list = sessionRepo.findByFacultyId(facultyId);
        return list.stream()
                .filter(s -> subjectId == null || s.getSubject().getId().equals(subjectId))
                .filter(s -> classSectionId == null || s.getClassSection().getId().equals(classSectionId))
                .filter(s -> startDate == null || !s.getSessionDate().isBefore(startDate))
                .filter(s -> endDate == null || !s.getSessionDate().isAfter(endDate))
                .toList();
    }

    /** Get list of students whose attendance is below minimum percentage in assigned classes */
    public List<LowAttendanceStudentDto> getLowAttendanceStudentsForFaculty(Long facultyId, double threshold) {
        List<FacultyAssignment> assignments = assignmentRepo.findByFacultyIdAndActiveTrue(facultyId);
        List<LowAttendanceStudentDto> result = new ArrayList<>();

        for (FacultyAssignment fa : assignments) {
            List<Student> students = studentRepo.findByClassSectionId(fa.getClassSection().getId());
            for (Student s : students) {
                AttendancePercentageDto dto = calculatePercentage(s.getId(), fa.getSubject().getId());
                if (dto.getTotalSessions() > 0 && dto.getPercentage() < threshold) {
                    LowAttendanceStudentDto low = new LowAttendanceStudentDto();
                    low.setStudentId(s.getId());
                    low.setStudentName(s.getUser().getFullName());
                    low.setRollNumber(s.getRollNumber());
                    low.setSubjectName(fa.getSubject().getName());
                    low.setSubjectCode(fa.getSubject().getCode());
                    low.setPercentage(dto.getPercentage());
                    low.setTotalSessions(dto.getTotalSessions());
                    low.setAttendedSessions(dto.getPresentCount() + dto.getLateCount());
                    result.add(low);
                }
            }
        }
        return result;
    }

    /** Get all records for CSV report */
    public List<AttendanceRecord> getRecordsForReport(Long classSectionId, Long subjectId,
                                                       LocalDate start, LocalDate end) {
        return recordRepo.findForReport(classSectionId, subjectId, start, end);
    }

    /** Update an attendance record's status */
    @Transactional
    public void updateRecord(Long recordId, AttendanceStatus newStatus, String remarks) {
        AttendanceRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", "id", recordId));
        record.setStatus(newStatus);
        if (remarks != null) {
            record.setRemarks(remarks);
        }
        recordRepo.save(record);
    }
}
