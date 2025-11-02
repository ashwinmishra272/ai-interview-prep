package com.ashwin.aiinterviewprep.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileParserService {

    public String extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName.endsWith(".pdf")) {
                return extractFromPDF(inputStream);
            } else if (fileName.endsWith(".docx")) {
                return extractFromDOCX(inputStream);
            } else if (fileName.endsWith(".doc")) {
                return extractFromDOC(inputStream);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing file " + fileName, e);
        }
    }

    private String extractFromPDF(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDOCX(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            return document.getParagraphs()
                    .stream()
                    .map(p -> p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
        }
    }

    private String extractFromDOC(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}
