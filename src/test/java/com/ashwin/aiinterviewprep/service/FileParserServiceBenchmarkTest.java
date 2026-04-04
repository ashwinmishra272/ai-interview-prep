package com.ashwin.aiinterviewprep.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileParserServiceBenchmarkTest {

    private final FileParserService fileParserService = new FileParserService();

    @Test
    void benchmarkPdfParsing() throws IOException {
        Path pdfPath = Paths.get("Ashwin_Mishra_Resume.pdf");
        assertTrue(Files.exists(pdfPath), "Resume PDF not found at: " + pdfPath.toAbsolutePath());

        byte[] pdfBytes = Files.readAllBytes(pdfPath);
        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "Ashwin_Mishra_Resume.pdf",
                "application/pdf",
                pdfBytes
        );

        int warmupRuns = 3;
        int benchmarkRuns = 10;

        // Warm up JVM / PDFBox
        System.out.println("Warming up (" + warmupRuns + " runs)...");
        for (int i = 0; i < warmupRuns; i++) {
            fileParserService.extractText(mockFile);
        }

        // Benchmark
        System.out.println("Benchmarking PDF parsing (" + benchmarkRuns + " runs)...");
        long[] timings = new long[benchmarkRuns];
        String extractedText = null;

        for (int i = 0; i < benchmarkRuns; i++) {
            long start = System.nanoTime();
            extractedText = fileParserService.extractText(mockFile);
            long elapsed = System.nanoTime() - start;
            timings[i] = elapsed;
            System.out.printf("  Run %2d: %6.2f ms%n", i + 1, elapsed / 1_000_000.0);
        }

        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long t : timings) {
            sum += t;
            if (t < min) min = t;
            if (t > max) max = t;
        }
        double avgMs = sum / (double) benchmarkRuns / 1_000_000.0;

        System.out.println();
        System.out.printf("PDF size       : %.2f KB%n", pdfBytes.length / 1024.0);
        System.out.printf("Extracted chars: %d%n", extractedText != null ? extractedText.length() : 0);
        System.out.printf("Min            : %.2f ms%n", min / 1_000_000.0);
        System.out.printf("Max            : %.2f ms%n", max / 1_000_000.0);
        System.out.printf("Avg            : %.2f ms%n", avgMs);

        assertNotNull(extractedText);
        assertFalse(extractedText.isBlank(), "Extracted text should not be blank");
    }
}
