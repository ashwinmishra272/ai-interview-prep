package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String id;         // question id (UUID or simple index)
    private String skill;      // skill this question targets
    private String difficulty; // easy|medium|hard
    private String text;       // actual question text
}
