package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.IPlannerAgent;
import com.ashwin.aiinterviewprep.model.SkillPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple planner that allocates a few questions per extracted skill.
 * In the future, this can use GPT to dynamically plan interview coverage.
 */
@Component
public class PlannerAgentSimpleImpl implements IPlannerAgent {

    @Override
    public List<SkillPlan> plan(List<String> skills) {
        List<SkillPlan> plans = new ArrayList<>();

        // Simple heuristic: top 3–5 skills, 2 questions each.
        int count = 0;
        for (String skill : skills) {
            if (count++ >= 5) break; // limit to top 5
            SkillPlan p = new SkillPlan();
            p.setSkill(skill);
            p.setQuestionsCount(2);
            plans.add(p);
        }

        // fallback if no skills
        if (plans.isEmpty()) {
            SkillPlan fallback = new SkillPlan();
            fallback.setSkill("General Communication");
            fallback.setQuestionsCount(1);
            plans.add(fallback);
        }

        return plans;
    }
}
