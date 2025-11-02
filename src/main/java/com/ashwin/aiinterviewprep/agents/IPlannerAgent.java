package com.ashwin.aiinterviewprep.agents;

import com.ashwin.aiinterviewprep.model.SkillPlan;

import java.util.List;

/**
 * Plan which skills to cover and how many questions each.
 */
public interface IPlannerAgent {
    List<SkillPlan> plan(List<String> skills);
}
