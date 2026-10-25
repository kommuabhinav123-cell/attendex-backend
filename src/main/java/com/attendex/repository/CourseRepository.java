package com.attendex.repository;

import com.attendex.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentId(Long departmentId);
    List<Course> findByActiveTrue();
    Optional<Course> findByCode(String code);
}
