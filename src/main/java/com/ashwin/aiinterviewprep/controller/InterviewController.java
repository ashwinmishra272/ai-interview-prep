package com.ashwin.aiinterviewprep.controller;

import com.ashwin.aiinterviewprep.service.FileParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private final FileParserService fileParserService;

    @Autowired
    public InterviewController(FileParserService fileParserService) {
        this.fileParserService = fileParserService;
    }

    @PostMapping("/questions")
    public ResponseEntity<Map<String, String>> uploadFiles(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jd") MultipartFile jd
    ) throws IOException {

        String resumeText = fileParserService.extractText(resume);
        String jdText = fileParserService.extractText(jd);

        Map<String, String> response = new HashMap<>();
        response.put("resumeText", resumeText);
        response.put("jdText", jdText);

        return ResponseEntity.ok(response);
    }
}
