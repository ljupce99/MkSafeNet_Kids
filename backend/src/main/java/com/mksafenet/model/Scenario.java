package com.mksafenet.model;

import com.mksafenet.converter.ChatMessageListConverter;
import com.mksafenet.converter.ScenarioOptionListConverter;
import com.mksafenet.dto.ChatMessageDto;
import com.mksafenet.dto.ScenarioOptionDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    public Scenario(String title, List<ChatMessageDto> setupMessages, String question, List<ScenarioOptionDto> options, Set<String> correctAnswers, String correctExplanation, String wrongExplanation, String consequenceType, List<ChatMessageDto> consequenceMessages, int points, int typeOfScenario) {
        this.title = title;
        this.setupMessages = setupMessages;
        this.question = question;
        this.options = options;
        this.correctAnswers = correctAnswers;
        this.correctExplanation = correctExplanation;
        this.wrongExplanation = wrongExplanation;
        this.consequenceType = consequenceType;
        this.consequenceMessages = consequenceMessages;
        this.points = points;
        this.typeOfScenario = typeOfScenario;
    }

    @Convert(converter = ChatMessageListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<ChatMessageDto> setupMessages;

    private String question;

    @Convert(converter = ScenarioOptionListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<ScenarioOptionDto> options;

    @ElementCollection
    @CollectionTable(name = "scenario_correct_answers", joinColumns = @JoinColumn(name = "scenario_id"))
    @Column(name = "answer")
    private Set<String> correctAnswers;

    private String correctExplanation;
    private String wrongExplanation;
    private String consequenceType;

    @Convert(converter = ChatMessageListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<ChatMessageDto> consequenceMessages;

    private int points;
    private int typeOfScenario;
}
