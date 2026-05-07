package com.mksafenet.service;

import com.mksafenet.model.School;
import com.mksafenet.model.User;
import com.mksafenet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;
    private final ScenarioResponseRepository scenarioResponseRepository;
    private final PasswordEncoder passwordEncoder;

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    @Transactional
    public School createSchool(String name, String address, String city) {
        School school = School.builder()
            .name(name)
            .address(address)
            .city(city)
            .build();
        return schoolRepository.save(school);
    }

    @Transactional
    public User createTeacher(String username, String password, String displayName, Long schoolId) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("School not found"));

        User teacher = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .displayName(displayName)
            .role(User.Role.TEACHER)
            .school(school)
            .build();
        return userRepository.save(teacher);
    }

    public Map<String, Object> getGlobalStats() {
        List<School> schools = schoolRepository.findAll();
        long totalStudents = studentRepository.count();
        long completedStudents = studentRepository.findAll().stream()
            .filter(s -> s.isCompleted()).count();

        double avgScore = studentRepository.findAll().stream()
            .filter(s -> s.isCompleted() && s.getScore() > 0)
            .mapToInt(s -> s.getScore())
            .average().orElse(0);

        List<Map<String, Object>> schoolStats = schools.stream()
            .map(school -> buildSchoolStats(school))
            .toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSchools", schools.size());
        stats.put("totalStudents", totalStudents);
        stats.put("completedStudents", completedStudents);
        stats.put("completionRate", totalStudents > 0 ? Math.round((double) completedStudents / totalStudents * 100) : 0);
        stats.put("averageScore", Math.round(avgScore));
        stats.put("schoolStats", schoolStats);
        stats.put("scenarioStats", getScenarioStats());
        return stats;
    }

    private Map<String, Object> buildSchoolStats(School school) {
        List<com.mksafenet.model.Student> students = studentRepository.findBySchoolId(school.getId());
        long completed = students.stream().filter(s -> s.isCompleted()).count();
        double avg = students.stream()
            .filter(s -> s.isCompleted())
            .mapToInt(s -> s.getScore())
            .average().orElse(0);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("schoolId", school.getId());
        m.put("schoolName", school.getName());
        m.put("city", school.getCity());
        m.put("totalStudents", students.size());
        m.put("completedStudents", completed);
        m.put("completionRate", students.size() > 0 ? Math.round((double) completed / students.size() * 100) : 0);
        m.put("averageScore", Math.round(avg));
        return m;
    }

    private List<Map<String, Object>> getScenarioStats() {
        String[] titles = {"", "The Bank Email", "The Prize Pop-up", "Spot the Fake Link",
            "The Urgent Message", "The Suspicious Attachment"};

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int scenarioId = i;
            var allResponses = scenarioResponseRepository.findAll().stream()
                .filter(r -> r.getScenarioId() == scenarioId).toList();
            long total = allResponses.size();
            long correct = allResponses.stream().filter(r -> r.isCorrect()).count();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("scenarioId", i);
            m.put("title", titles[i]);
            m.put("totalAnswers", total);
            m.put("correctAnswers", correct);
            m.put("successRate", total > 0 ? Math.round((double) correct / total * 100) : 0);
            result.add(m);
        }
        return result;
    }

    public List<User> getAllTeachers() {
        return userRepository.findByRole(User.Role.TEACHER);
    }

    public Map<String, Object> getSchoolDetail(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("School not found"));
        List<com.mksafenet.model.Student> students = studentRepository.findBySchoolId(schoolId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("school", Map.of("id", school.getId(), "name", school.getName(),
            "city", school.getCity() != null ? school.getCity() : ""));
        result.put("students", students.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("score", s.getScore());
            m.put("completed", s.isCompleted());
            m.put("startedAt", s.getStartedAt());
            m.put("completedAt", s.getCompletedAt());
            m.put("sessionName", s.getSession().getName());
            return m;
        }).toList());
        return result;
    }
}
