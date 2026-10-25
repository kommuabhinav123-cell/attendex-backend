package com.attendex.repository;

import com.attendex.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    List<ClassSection> findByCourseId(Long courseId);
    List<ClassSection> findByActiveTrue();
}
