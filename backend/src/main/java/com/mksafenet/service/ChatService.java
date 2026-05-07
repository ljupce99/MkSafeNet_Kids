package com.mksafenet.service;

import com.mksafenet.dto.*;
import com.mksafenet.model.*;
import com.mksafenet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final ScenarioResponseRepository scenarioResponseRepository;
    private final ScenarioService scenarioService;

    public record SessionInfo(boolean valid, String sessionName, String schoolName) {}

    public SessionInfo validateSession(String token) {
        return sessionRepository.findByToken(token)
            .map(s -> new SessionInfo(s.isActive(), s.getName(), s.getSchool().getName()))
            .orElse(new SessionInfo(false, null, null));
    }

    @Transactional
    public ChatResponseDto startChat(ChatStartRequest request) {
        Session session = sessionRepository.findByToken(request.getSessionToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid session token"));

        if (!session.isActive()) {
            throw new IllegalArgumentException("This session is no longer active");
        }

        Student student = Student.builder()
            .id(UUID.randomUUID().toString())
            .name(request.getStudentName().trim())
            .session(session)
            .currentScenario(0)
            .score(0)
            .completed(false)
            .build();
        studentRepository.save(student);

        List<ChatMessageDto> introMessages = scenarioService.getIntroMessages(student.getName());
        ScenarioService.Scenario firstScenario = scenarioService.getScenario(1);

        student.setCurrentScenario(1);
        studentRepository.save(student);

        List<ChatMessageDto> allMessages = new ArrayList<>(introMessages);
        allMessages.addAll(firstScenario.setupMessages());

        return ChatResponseDto.builder()
            .studentId(student.getId())
            .phase("SCENARIO")
            .messages(allMessages)
            .scenarioId(1)
            .question(firstScenario.question())
            .options(toOptionMaps(firstScenario.options()))
            .build();
    }

    @Transactional
    public ChatResponseDto respond(ChatRespondRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.isCompleted()) {
            throw new IllegalStateException("Chat already completed");
        }

        int scenarioId = student.getCurrentScenario();
        ScenarioService.Scenario scenario = scenarioService.getScenario(scenarioId);
        String answer = request.getAnswer().toUpperCase().trim();
        boolean correct = scenario.correctAnswers().contains(answer);
        int pointsEarned = correct ? scenario.points() : 0;

        ScenarioResponse scenarioResponse = ScenarioResponse.builder()
            .student(student)
            .scenarioId(scenarioId)
            .selectedAnswer(answer)
            .correct(correct)
            .pointsEarned(pointsEarned)
            .build();
        scenarioResponseRepository.save(scenarioResponse);

        student.setScore(student.getScore() + pointsEarned);

        boolean isLastScenario = scenarioId >= ScenarioService.TOTAL_SCENARIOS;

        if (isLastScenario) {
            student.setCurrentScenario(scenarioId + 1);
            student.setCompleted(true);
            student.setCompletedAt(LocalDateTime.now());
            studentRepository.save(student);
            return buildCompleteResponse(student, correct, scenario, answer);
        }

        int nextScenarioId = scenarioId + 1;
        student.setCurrentScenario(nextScenarioId);
        studentRepository.save(student);

        return buildNextScenarioResponse(student, correct, scenario, answer, nextScenarioId);
    }

    private ChatResponseDto buildNextScenarioResponse(Student student, boolean correct,
            ScenarioService.Scenario scenario, String answer, int nextScenarioId) {

        ScenarioService.Scenario nextScenario = scenarioService.getScenario(nextScenarioId);
        List<ChatMessageDto> messages = new ArrayList<>();

        if (!correct) {
            List<ChatMessageDto> transition = scenarioService.getTransitionMessages(scenario.id(), false, student.getName());
            messages.addAll(transition);
            messages.addAll(nextScenario.setupMessages());

            return ChatResponseDto.builder()
                .studentId(student.getId())
                .phase("CONSEQUENCE")
                .correct(false)
                .consequenceType(scenario.consequenceType())
                .consequenceMessages(scenario.consequenceMessages())
                .messages(messages)
                .scenarioId(nextScenarioId)
                .question(nextScenario.question())
                .options(toOptionMaps(nextScenario.options()))
                .score(student.getScore())
                .build();
        }

        messages.addAll(scenarioService.getTransitionMessages(scenario.id(), true, student.getName()));
        messages.addAll(nextScenario.setupMessages());

        return ChatResponseDto.builder()
            .studentId(student.getId())
            .phase("SCENARIO")
            .correct(true)
            .messages(messages)
            .scenarioId(nextScenarioId)
            .question(nextScenario.question())
            .options(toOptionMaps(nextScenario.options()))
            .score(student.getScore())
            .build();
    }

    private ChatResponseDto buildCompleteResponse(Student student, boolean lastCorrect,
            ScenarioService.Scenario lastScenario, String answer) {

        List<ScenarioResponse> allResponses = scenarioResponseRepository.findByStudentId(student.getId());
        int correctCount = (int) allResponses.stream().filter(ScenarioResponse::isCorrect).count();
        int score = student.getScore();
        boolean passed = score >= 60;
        List<String> badges = scenarioService.calculateBadges(score, allResponses);
        String grade = scenarioService.calculateGrade(score);

        List<ChatMessageDto> messages = new ArrayList<>();
        if (!lastCorrect) {
            messages.addAll(lastScenario.consequenceMessages());
        }
        messages.addAll(buildFinalMessages(student.getName(), score, passed, grade));

        List<ScenarioResultDto> results = allResponses.stream()
            .map(r -> ScenarioResultDto.builder()
                .scenarioId(r.getScenarioId())
                .scenarioTitle(scenarioService.getScenario(r.getScenarioId()).title())
                .selectedAnswer(r.getSelectedAnswer())
                .correct(r.isCorrect())
                .pointsEarned(r.getPointsEarned())
                .build())
            .sorted(Comparator.comparingInt(ScenarioResultDto::getScenarioId))
            .toList();

        return ChatResponseDto.builder()
            .studentId(student.getId())
            .phase("COMPLETE")
            .correct(lastCorrect)
            .consequenceType(lastCorrect ? null : lastScenario.consequenceType())
            .consequenceMessages(lastCorrect ? null : lastScenario.consequenceMessages())
            .messages(messages)
            .score(score)
            .grade(grade)
            .passed(passed)
            .correctCount(correctCount)
            .totalScenarios(ScenarioService.TOTAL_SCENARIOS)
            .badges(badges)
            .scenarioResults(results)
            .build();
    }

    private List<ChatMessageDto> buildFinalMessages(String name, int score, boolean passed, String grade) {
        List<ChatMessageDto> msgs = new ArrayList<>();
        if (passed) {
            msgs.add(msg("success", "🎉 Amazing work, " + name + "! You completed the Phishing Challenge!", 0));
            msgs.add(msg("success", "You scored " + score + "/100 — Grade: " + grade, 1500));
            msgs.add(msg("bot", "You are now a certified Phishing Spotter! 🛡️", 3000));
            msgs.add(msg("bot", "Remember the golden rules: Never click suspicious links, never share passwords, and always ask a trusted adult when unsure!", 4500));
        } else {
            msgs.add(msg("bot", "You completed the challenge, " + name + "! You scored " + score + "/100 — Grade: " + grade, 0));
            msgs.add(msg("bot", "It's okay — that's why we practice! The most important thing is that you now KNOW what phishing looks like.", 1800));
            msgs.add(msg("bot", "Remember: When in doubt, don't click — ask a trusted adult! 🛡️", 3400));
        }
        return msgs;
    }

    private List<Map<String, String>> toOptionMaps(List<ScenarioService.ScenarioOption> options) {
        return options.stream()
            .map(o -> Map.of("key", o.key(), "text", o.text()))
            .toList();
    }

    private ChatMessageDto msg(String type, String text, int delayMs) {
        return ChatMessageDto.builder().type(type).text(text).delayMs(delayMs).build();
    }
}
