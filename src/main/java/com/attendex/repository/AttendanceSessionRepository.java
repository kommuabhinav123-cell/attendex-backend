package com.attendex.repository;

import com.attendex.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findBySubjectIdAndClassSectionIdAndSessionDate(
            Long subjectId, Long classSectionId, LocalDate sessionDate);

    List<AttendanceSession> findByFacultyId(Long facultyId);

    List<AttendanceSession> findBySubjectIdAndClassSectionId(Long subjectId, Long classSectionId);

    List<AttendanceSession> findBySessionDateBetween(LocalDate start, LocalDate end);

    List<AttendanceSession> findByClassSectionIdAndSubjectIdAndSessionDateBetween(
            Long classSectionId, Long subjectId, LocalDate start, LocalDate end);

    long countBySubjectIdAndClassSectionId(Long subjectId, Long classSectionId);
}
