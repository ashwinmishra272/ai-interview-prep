package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummary {
    private String sessionId;
    private LocalDateTime createdAt;
    private List<String> skills;
    private int totalQuestions;
    private int answeredQuestions;
    private double averageScore;
    private boolean finished;
    private Map<String, Double> scoreBySkill;
    private Map<String, Double> scoreByDifficulty;
    private List<String> weakSkills;
    private List<String> strongSkills;
}
