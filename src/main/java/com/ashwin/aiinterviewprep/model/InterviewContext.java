package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewContext {
    private String resumeText;
    private String jobDescription;
    private List<String> skills = new ArrayList<>();     // extracted skills
    private List<Question> questions = new ArrayList<>(); // asked questions
    private List<AnswerEvaluation> evaluations = new ArrayList<>();
    private int questionIndex = 0;                       // pointer into questions/plan
    private boolean finished = false;
}
