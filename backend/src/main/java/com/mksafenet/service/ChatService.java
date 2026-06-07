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
        Scenario firstScenario = scenarioService.getScenario(1);

        student.setCurrentScenario(1);
        studentRepository.save(student);

        List<ChatMessageDto> allMessages = new ArrayList<>(introMessages);
        allMessages.addAll(firstScenario.getSetupMessages());

        return ChatResponseDto.builder()
            .studentId(student.getId())
            .phase("SCENARIO")
            .messages(allMessages)
            .scenarioId(1)
            .question(firstScenario.getQuestion())
            .options(toOptionMaps(firstScenario.getOptions()))
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
        Scenario scenario = scenarioService.getScenario(scenarioId);
        String answer = request.getAnswer().toUpperCase().trim();
        boolean correct = scenario.getCorrectAnswers().contains(answer);
        int pointsEarned = correct ? scenario.getPoints() : 0;

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
            Scenario scenario, String answer, int nextScenarioId) {

        Scenario nextScenario = scenarioService.getScenario(nextScenarioId);
        List<ChatMessageDto> messages = new ArrayList<>();

        if (!correct) {
            List<ChatMessageDto> transition = scenarioService.getTransitionMessages(scenario.getTypeOfScenario(), false, student.getName());
            messages.addAll(transition);
            messages.addAll(nextScenario.getSetupMessages());

            return ChatResponseDto.builder()
                .studentId(student.getId())
                .phase("CONSEQUENCE")
                .correct(false)
                .consequenceType(scenario.getConsequenceType())
                .consequenceMessages(scenario.getConsequenceMessages())
                .messages(messages)
                .scenarioId(nextScenarioId)
                .question(nextScenario.getQuestion())
                .options(toOptionMaps(nextScenario.getOptions()))
                .score(student.getScore())
                .build();
        }

        messages.addAll(scenarioService.getTransitionMessages(scenario.getTypeOfScenario(), true, student.getName()));
        messages.addAll(nextScenario.getSetupMessages());

        return ChatResponseDto.builder()
            .studentId(student.getId())
            .phase("SCENARIO")
            .correct(true)
            .messages(messages)
            .scenarioId(nextScenarioId)
            .question(nextScenario.getQuestion())
            .options(toOptionMaps(nextScenario.getOptions()))
            .score(student.getScore())
            .build();
    }

    private ChatResponseDto buildCompleteResponse(Student student, boolean lastCorrect,
            Scenario lastScenario, String answer) {

        List<ScenarioResponse> allResponses = scenarioResponseRepository.findByStudentId(student.getId());
        int correctCount = (int) allResponses.stream().filter(ScenarioResponse::isCorrect).count();
        int score = student.getScore();
        boolean passed = score >= 60;
        List<String> badges = scenarioService.calculateBadges(score, allResponses);
        String grade = scenarioService.calculateGrade(score);

        List<ChatMessageDto> messages = new ArrayList<>();
        if (!lastCorrect) {
            messages.addAll(lastScenario.getConsequenceMessages());
        }
        messages.addAll(buildFinalMessages(student.getName(), score, passed, grade));

        List<ScenarioResultDto> results = allResponses.stream()
            .map(r -> ScenarioResultDto.builder()
                .scenarioId(r.getScenarioId())
                .scenarioTitle(scenarioService.getScenario(r.getScenarioId()).getTitle())
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
            .consequenceType(lastCorrect ? null : lastScenario.getConsequenceType())
            .consequenceMessages(lastCorrect ? null : lastScenario.getConsequenceMessages())
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
        //TODO : base with meseges and ?
        if (passed) {
            msgs.add(msg("success", "🎉 Одлична работа, " + name + "! Го заврши Предизвикот за фишинг!", 0));
            msgs.add(msg("success", "Освои " + score + "/100 — Оценка: " + grade, 1500));
            msgs.add(msg("bot", "Сега си сертифициран Препознавач на фишинг! 🛡️", 3000));
            msgs.add(msg("bot", "Запомни ги златните правила: Никогаш не кликнувај на сомнителни линкови, никогаш не споделувај лозинки и секогаш прашувај доверлив возрасен кога не си сигурен!", 4500));
        } else {
            msgs.add(msg("bot", "Го заврши предизвикот, " + name + "! Освои " + score + "/100 — Оценка: " + grade, 0));
            msgs.add(msg("bot", "Во ред е — затоа и вежбаме! Најважно е што сега ЗНАЕШ како изгледа фишингот.", 1800));
            msgs.add(msg("bot", "Запомни: Кога се сомневаш, не кликнувај — прашај доверлив возрасен! 🛡️", 3400));
        }
        return msgs;
    }

    private List<Map<String, String>> toOptionMaps(List<ScenarioOptionDto> options) {
        return options.stream()
            .map(o -> Map.of("key", o.getKey(), "text", o.getText()))
            .toList();
    }

    private ChatMessageDto msg(String type, String text, int delayMs) {
        return ChatMessageDto.builder().type(type).text(text).delayMs(delayMs).build();
    }
}
