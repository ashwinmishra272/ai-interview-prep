package com.ashwin.aiinterviewprep.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interview_questions")
@Data
@NoArgsConstructor
public class InterviewQuestionRecord {

    @Id
    private String id;

    @Column(nullable = false)
    private String sessionId;

    private int questionIndex;
    private String skill;
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String text;
}
