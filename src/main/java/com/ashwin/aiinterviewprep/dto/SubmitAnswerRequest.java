package com.ashwin.aiinterviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "answer is required")
    private String answer;
}
