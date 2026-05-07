package com.mksafenet.service;

import com.mksafenet.model.Session;
import com.mksafenet.model.Student;
import com.mksafenet.model.User;
import com.mksafenet.repository.ScenarioResponseRepository;
import com.mksafenet.repository.SessionRepository;
import com.mksafenet.repository.StudentRepository;
import com.mksafenet.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final ScenarioResponseRepository scenarioResponseRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public Map<String, Object> createSession(User teacher, String sessionName) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Session session = Session.builder()
            .token(token)
            .name(sessionName)
            .teacher(teacher)
            .school(teacher.getSchool())
            .active(true)
            .build();
        sessionRepository.save(session);

        String qrUrl = frontendUrl + "/chat?token=" + token;
        String qrBase64 = qrCodeGenerator.generateQrBase64(qrUrl);

        return Map.of(
            "sessionId", session.getId(),
            "token", token,
            "name", sessionName,
            "url", qrUrl,
            "qrCode", qrBase64
        );
    }

    public List<Map<String, Object>> getTeacherSessions(User teacher) {
        return sessionRepository.findByTeacherId(teacher.getId()).stream()
            .map(this::buildSessionSummary)
            .toList();
    }

    public Map<String, Object> getSessionQr(User teacher, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Not your session");
        }
        String qrUrl = frontendUrl + "/chat?token=" + session.getToken();
        String qrBase64 = qrCodeGenerator.generateQrBase64(qrUrl);
        return Map.of("qrCode", qrBase64, "url", qrUrl, "token", session.getToken());
    }

    @Transactional
    public void toggleSession(User teacher, Long sessionId, boolean active) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Not your session");
        }
        session.setActive(active);
        sessionRepository.save(session);
    }

    public Map<String, Object> getSessionDetail(User teacher, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Not your session");
        }

        List<Student> students = studentRepository.findBySessionId(sessionId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session", buildSessionSummary(session));
        result.put("students", students.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("score", s.getScore());
            m.put("completed", s.isCompleted());
            m.put("startedAt", s.getStartedAt());
            m.put("completedAt", s.getCompletedAt());
            var responses = scenarioResponseRepository.findByStudentId(s.getId());
            m.put("responses", responses.stream().map(r -> Map.of(
                "scenarioId", r.getScenarioId(),
                "answer", r.getSelectedAnswer(),
                "correct", r.isCorrect(),
                "points", r.getPointsEarned()
            )).toList());
            return m;
        }).toList());
        return result;
    }

    private Map<String, Object> buildSessionSummary(Session session) {
        List<Student> students = studentRepository.findBySessionId(session.getId());
        long completed = students.stream().filter(Student::isCompleted).count();
        double avg = students.stream()
            .filter(Student::isCompleted)
            .mapToInt(Student::getScore)
            .average().orElse(0);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", session.getId());
        m.put("name", session.getName());
        m.put("token", session.getToken());
        m.put("active", session.isActive());
        m.put("createdAt", session.getCreatedAt());
        m.put("totalStudents", students.size());
        m.put("completedStudents", completed);
        m.put("averageScore", Math.round(avg));
        return m;
    }
}
