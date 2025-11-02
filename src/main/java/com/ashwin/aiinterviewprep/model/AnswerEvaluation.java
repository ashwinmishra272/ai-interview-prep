package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of evaluating a candidate's answer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEvaluation {
    private String id;           // unique evaluation id
    private String questionId;
    private int score;           // 1..10
    private String feedback;     // short feedback
    private String improvement;  // suggestion for improvement
}
