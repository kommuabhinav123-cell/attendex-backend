package com.attendex.repository;

import com.attendex.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUserId(Long userId);
    Optional<Faculty> findByEmployeeId(String employeeId);
    List<Faculty> findByDepartmentId(Long departmentId);
    List<Faculty> findByActiveTrue();
    long countByActiveTrue();
}
