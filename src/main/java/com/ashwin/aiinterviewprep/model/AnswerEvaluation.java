package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEvaluation {
    private String id;
    private String questionId;
    private int score;
    private String feedback;
    private String improvement;
}
