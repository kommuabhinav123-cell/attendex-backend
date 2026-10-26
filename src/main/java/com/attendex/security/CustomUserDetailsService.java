package com.attendex.security;

import com.attendex.entity.Faculty;
import com.attendex.entity.Student;
import com.attendex.entity.User;
import com.attendex.repository.FacultyRepository;
import com.attendex.repository.StudentRepository;
import com.attendex.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Custom UserDetailsService that loads user accounts.
 * Supports multi-identifier authentication:
 * - Username (e.g. admin, johndoe)
 * - Numeric User ID (e.g. 1, 2)
 * - Email address
 * - Student Roll Number (e.g. 2024CSE001)
 * - Faculty Employee ID (e.g. FAC-1001)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    StudentRepository studentRepository,
                                    FacultyRepository facultyRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Identifier cannot be blank");
        }
        String cleanId = identifier.trim();

        // 1. Try finding by username
        Optional<User> userOpt = userRepository.findByUsername(cleanId);

        // 2. Try finding by email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(cleanId);
        }

        // 3. Try finding by student roll number
        if (userOpt.isEmpty()) {
            userOpt = studentRepository.findByRollNumber(cleanId).map(Student::getUser);
        }

        // 4. Try finding by faculty employee ID
        if (userOpt.isEmpty()) {
            userOpt = facultyRepository.findByEmployeeId(cleanId).map(Faculty::getUser);
        }

        // 5. Try finding by numeric User ID
        if (userOpt.isEmpty() && cleanId.matches("\\d+")) {
            try {
                userOpt = userRepository.findById(Long.parseLong(cleanId));
            } catch (NumberFormatException ignored) {}
        }

        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User account not found: " + cleanId));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account is deactivated: " + cleanId);
        }

        return new CustomUserDetails(user);
    }
}
