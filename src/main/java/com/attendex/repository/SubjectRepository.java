package com.attendex.repository;

import com.attendex.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByCourseId(Long courseId);
    List<Subject> findBySemester(int semester);
    Optional<Subject> findByCode(String code);
    List<Subject> findByActiveTrue();
    long countByActiveTrue();
}
