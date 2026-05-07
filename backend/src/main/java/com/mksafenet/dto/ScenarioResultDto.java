package com.mksafenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResultDto {
    private int scenarioId;
    private String scenarioTitle;
    private String selectedAnswer;
    private boolean correct;
    private int pointsEarned;
}
