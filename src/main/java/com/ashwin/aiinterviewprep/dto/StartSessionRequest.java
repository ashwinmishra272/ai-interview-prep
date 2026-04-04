package com.ashwin.aiinterviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartSessionRequest {
    @NotBlank(message = "resumeText is required")
    private String resumeText;

    @NotBlank(message = "jdText is required")
    private String jdText;
}
