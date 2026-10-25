package com.attendex;

import com.attendex.dto.AttendancePercentageDto;
import com.attendex.dto.DashboardStatsDto;
import com.attendex.entity.*;
import com.attendex.repository.*;
import com.attendex.service.AttendanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository sessionRepo;
    @Mock
    private AttendanceRecordRepository recordRepo;
    @Mock
    private StudentRepository studentRepo;
    @Mock
    private SubjectRepository subjectRepo;
    @Mock
    private ClassSectionRepository classSectionRepo;
    @Mock
    private FacultyRepository facultyRepo;
    @Mock
    private EnrollmentRepository enrollmentRepo;
    @Mock
    private FacultyAssignmentRepository assignmentRepo;
    @Mock
    private DepartmentRepository departmentRepo;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    @DisplayName("Should accurately calculate attendance percentage")
    void testCalculatePercentage() {
        Long studentId = 1L;
        Long subjectId = 10L;

        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setName("Data Structures");
        subject.setCode("CS201");

        when(subjectRepo.findById(subjectId)).thenReturn(Optional.of(subject));
        when(recordRepo.countByStudentAndSubject(studentId, subjectId)).thenReturn(10L);
        when(recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.PRESENT)).thenReturn(7L);
        when(recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.LATE)).thenReturn(1L);
        when(recordRepo.countByStudentAndSubjectAndStatus(studentId, subjectId, AttendanceStatus.ABSENT)).thenReturn(2L);

        AttendancePercentageDto result = attendanceService.calculatePercentage(studentId, subjectId);

        assertNotNull(result);
        assertEquals(10L, result.getTotalSessions());
        assertEquals(7L, result.getPresentCount());
        assertEquals(1L, result.getLateCount());
        assertEquals(2L, result.getAbsentCount());
        // (7 + 1) / 10 * 100 = 80.0%
        assertEquals(80.0, result.getPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should prevent duplicate sessions on the same period and date")
    void testCreateSessionDuplicatePrevention() {
        Long subjectId = 10L;
        Long classSectionId = 20L;
        Long facultyId = 30L;
        LocalDate date = LocalDate.of(2026, 10, 25);
        int period = 1;

        Subject subject = new Subject();
        ClassSection cs = new ClassSection();
        Faculty faculty = new Faculty();

        when(subjectRepo.findById(subjectId)).thenReturn(Optional.of(subject));
        when(classSectionRepo.findById(classSectionId)).thenReturn(Optional.of(cs));
        when(facultyRepo.findById(facultyId)).thenReturn(Optional.of(faculty));

        AttendanceSession existingSession = new AttendanceSession();
        existingSession.setSessionNumber(1);
        when(sessionRepo.findBySubjectIdAndClassSectionIdAndSessionDate(subjectId, classSectionId, date))
                .thenReturn(List.of(existingSession));

        assertThrows(RuntimeException.class, () ->
                attendanceService.createSession(subjectId, classSectionId, facultyId, date, period));
    }

    @Test
    @DisplayName("Should return valid dashboard stats")
    void testDashboardStats() {
        when(studentRepo.countByActiveTrue()).thenReturn(45L);
        when(facultyRepo.countByActiveTrue()).thenReturn(6L);
        when(subjectRepo.countByActiveTrue()).thenReturn(12L);
        when(departmentRepo.findByActiveTrue()).thenReturn(Collections.emptyList());

        DashboardStatsDto stats = attendanceService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(45L, stats.getTotalStudents());
        assertEquals(6L, stats.getTotalFaculty());
        assertEquals(12L, stats.getTotalSubjects());
    }
}
