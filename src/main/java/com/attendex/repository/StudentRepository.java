package com.attendex.repository;

import com.attendex.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByClassSectionId(Long classSectionId);
    List<Student> findByActiveTrue();
    long countByActiveTrue();
}
