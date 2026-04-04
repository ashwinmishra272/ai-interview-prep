package com.ashwin.aiinterviewprep.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interview_evaluations")
@Data
@NoArgsConstructor
public class InterviewEvaluationRecord {

    @Id
    private String id;

    @Column(nullable = false)
    private String sessionId;

    private String questionId;
    private int score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String improvement;
}
