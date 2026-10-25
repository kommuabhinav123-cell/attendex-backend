package com.attendex.repository;

import com.attendex.entity.FacultyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacultyAssignmentRepository extends JpaRepository<FacultyAssignment, Long> {
    List<FacultyAssignment> findByFacultyId(Long facultyId);
    List<FacultyAssignment> findBySubjectId(Long subjectId);
    List<FacultyAssignment> findByClassSectionId(Long classSectionId);
    boolean existsByFacultyIdAndSubjectIdAndClassSectionId(Long facultyId, Long subjectId, Long classSectionId);
    List<FacultyAssignment> findByFacultyIdAndActiveTrue(Long facultyId);
}
