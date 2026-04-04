package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionListItem {
    private String sessionId;
    private LocalDateTime createdAt;
    private List<String> skills;
    private int totalQuestions;
    private int answeredQuestions;
    private double averageScore;
    private boolean finished;
}
