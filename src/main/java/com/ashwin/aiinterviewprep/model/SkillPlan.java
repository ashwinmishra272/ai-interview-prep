package com.ashwin.aiinterviewprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple plan item for which skill to cover and how many questions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillPlan {
    private String skill;
    private int questionsCount;
}
