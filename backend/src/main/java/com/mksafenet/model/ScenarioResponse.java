package com.mksafenet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scenario_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private int scenarioId;

    @Column(nullable = false)
    private String selectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int pointsEarned;

    @Column(nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    @PrePersist
    void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}
