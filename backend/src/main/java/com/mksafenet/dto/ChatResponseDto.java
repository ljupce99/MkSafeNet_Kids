package com.mksafenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    // Used by both /start and /respond
    private String studentId;
    private String phase;           // "INTRO", "SCENARIO", "CONSEQUENCE", "COMPLETE"
    private List<ChatMessageDto> messages;

    // Present during SCENARIO phase
    private Integer scenarioId;
    private String question;
    private List<Map<String, String>> options; // [{"key": "A", "text": "..."}]

    // Present after answering
    private Boolean correct;
    private String consequenceType;
    private List<ChatMessageDto> consequenceMessages;

    // Present in COMPLETE phase
    private Integer score;
    private String grade;
    private Boolean passed;
    private Integer correctCount;
    private Integer totalScenarios;
    private List<String> badges;
    private List<ScenarioResultDto> scenarioResults;
}
