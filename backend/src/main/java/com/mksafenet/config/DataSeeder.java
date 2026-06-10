package com.mksafenet.config;

import com.mksafenet.model.School;
import com.mksafenet.model.User;
import com.mksafenet.repository.SchoolRepository;
import com.mksafenet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDemoSchoolAndTeacher();
    }

    private void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .displayName("System Administrator")
                .role(User.Role.ADMIN)
                .build();
            userRepository.save(admin);
            log.info("Seeded admin user — username: admin, password: admin123");
        }
    }

    private void seedDemoSchoolAndTeacher() {
        if (!schoolRepository.existsByName("ООУ „Гоце Делчев“")) {
            School school = School.builder()
                .name("ООУ „Гоце Делчев“")
                .address("ул. „Цветан Димов“, бр. 1")
                .city("Кавадарци")
                .build();
            schoolRepository.save(school);

            User teacher = User.builder()
                .username("teacher")
                .password(passwordEncoder.encode("teacher123"))
                .displayName("Наставник Јована Јованова")
                .role(User.Role.TEACHER)
                .school(school)
                .build();
            userRepository.save(teacher);
            log.info("Seeded demo school and teacher — username: teacher, password: teacher123");
        }
    }
}
