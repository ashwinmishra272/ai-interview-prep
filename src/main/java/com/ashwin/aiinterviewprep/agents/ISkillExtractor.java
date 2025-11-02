package com.ashwin.aiinterviewprep.agents;

import java.util.List;

public interface ISkillExtractor {
    /**
     * Extract skills from resume + jd text.
     */
    List<String> extractSkills(String resumeText, String jdText);
}
