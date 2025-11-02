# ai-interview-prep

┌────────────────────────────┐
│         Controller          │
│ (Receives /start /next req) │
└────────────┬───────────────┘
│
┌────────────▼──────────────┐
│     InterviewAgent         │
│  (Reasoning + Planning)    │
│                            │
│ - decideNextAction()       │
│ - trackContext()           │
│ - callLLM(prompt)          │
└────────────┬──────────────┘
│
┌────────────▼──────────────┐
│     PromptBuilder          │
│ (Template + Context merge) │
│                            │
│ - buildExtractSkillsPrompt │
│ - buildQuestionPrompt      │
│ - buildEvaluationPrompt    │
│ - buildSummaryPrompt       │
└────────────┬──────────────┘
│
┌────────────▼──────────────┐
│       GPTClient            │
│ (Handles API call)         │
└────────────────────────────┘
