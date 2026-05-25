package com.ai.knowledge.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentParserService {
    
    public String parseDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = getFileType(fileName);
        
        return switch (fileType.toLowerCase()) {
            case "pdf" -> parsePdf(file.getBytes());
            case "docx", "doc" -> parseWord(file.getInputStream());
            case "xlsx", "xls" -> parseExcel(file.getInputStream(), fileType.toLowerCase());
            case "csv" -> parseTxt(file.getInputStream());
            case "txt" -> parseTxt(file.getInputStream());
            default -> throw new IllegalArgumentException("不支持的文件格式: " + fileType);
        };
    }
    
    private String parsePdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private String parseWord(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }
    
    private String parseExcel(InputStream inputStream, String fileType) throws IOException {
        List<String> allText = new ArrayList<>();
        
        try (Workbook workbook = fileType.equals("xlsx") ? 
                new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream)) {
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                allText.add("=== Sheet: " + sheet.getSheetName() + " ===");
                
                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        rowData.add(getCellValue(cell));
                    }
                    allText.add(String.join("\t", rowData));
                }
            }
        }
        
        return String.join("\n", allText);
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }
    
    private String parseTxt(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes());
    }
    
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
