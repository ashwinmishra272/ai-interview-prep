package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.IPlannerAgent;
import com.ashwin.aiinterviewprep.model.SkillPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class PlannerAgentSimpleImpl implements IPlannerAgent {

    @Override
    public List<SkillPlan> plan(List<String> skills) {
        List<SkillPlan> plans = new ArrayList<>();

        int count = 0;
        for (String skill : skills) {
            if (count++ >= 5) break;
            SkillPlan p = new SkillPlan();
            p.setSkill(skill);
            p.setQuestionsCount(2);
            plans.add(p);
        }

        if (plans.isEmpty()) {
            SkillPlan fallback = new SkillPlan();
            fallback.setSkill("General Communication");
            fallback.setQuestionsCount(1);
            plans.add(fallback);
        }

        return plans;
    }
}
