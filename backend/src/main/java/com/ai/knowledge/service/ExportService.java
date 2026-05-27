package com.ai.knowledge.service;

import com.ai.knowledge.entity.ChatRecord;
import com.ai.knowledge.repository.ChatRecordRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private ChatRecordRepository chatRecordRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportToExcel(Long userId, boolean isAdmin) throws Exception {
        List<ChatRecord> records;
        if (isAdmin) {
            records = chatRecordRepository.findAll();
        } else {
            records = chatRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("问答记录");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("用户ID");
        headerRow.createCell(2).setCellValue("问题");
        headerRow.createCell(3).setCellValue("回答");
        headerRow.createCell(4).setCellValue("对话类型");
        headerRow.createCell(5).setCellValue("反馈");
        headerRow.createCell(6).setCellValue("创建时间");

        int rowNum = 1;
        for (ChatRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getId());
            row.createCell(1).setCellValue(record.getUserId());
            row.createCell(2).setCellValue(record.getQuestion());
            row.createCell(3).setCellValue(record.getAnswer());
            row.createCell(4).setCellValue(record.getChatType());
            row.createCell(5).setCellValue(getFeedbackText(record.getFeedback()));
            row.createCell(6).setCellValue(record.getCreateTime() != null ? record.getCreateTime().format(DATE_FORMATTER) : "");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public byte[] exportToWord(Long userId, boolean isAdmin) throws Exception {
        List<ChatRecord> records;
        if (isAdmin) {
            records = chatRecordRepository.findAll();
        } else {
            records = chatRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
        }

        XWPFDocument document = new XWPFDocument();

        XWPFParagraph title = document.createParagraph();
        XWPFRun titleRun = title.createRun();
        titleRun.setText("问答记录导出");
        titleRun.setBold(true);
        titleRun.setFontSize(16);

        for (ChatRecord record : records) {
            XWPFParagraph questionPara = document.createParagraph();
            XWPFRun questionRun = questionPara.createRun();
            questionRun.setBold(true);
            questionRun.setText("问题: " + record.getQuestion());

            XWPFParagraph answerPara = document.createParagraph();
            XWPFRun answerRun = answerPara.createRun();
            answerRun.setText("回答: " + record.getAnswer());

            XWPFParagraph timePara = document.createParagraph();
            XWPFRun timeRun = timePara.createRun();
            timeRun.setColor("999999");
            timeRun.setFontSize(10);
            timeRun.setText("时间: " + (record.getCreateTime() != null ? record.getCreateTime().format(DATE_FORMATTER) : ""));

            document.createParagraph();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        return outputStream.toByteArray();
    }

    public byte[] exportToPdf(Long userId, boolean isAdmin) throws Exception {
        List<ChatRecord> records;
        if (isAdmin) {
            records = chatRecordRepository.findAll();
        } else {
            records = chatRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("问答记录导出").setBold().setFontSize(16));

        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 35, 35, 5}))
                .useAllAvailableWidth();

        table.addHeaderCell("ID");
        table.addHeaderCell("类型");
        table.addHeaderCell("问题");
        table.addHeaderCell("回答");
        table.addHeaderCell("时间");

        for (ChatRecord record : records) {
            table.addCell(String.valueOf(record.getId()));
            table.addCell(record.getChatType());
            table.addCell(record.getQuestion() != null ? record.getQuestion().substring(0, Math.min(record.getQuestion().length(), 100)) : "");
            table.addCell(record.getAnswer() != null ? record.getAnswer().substring(0, Math.min(record.getAnswer().length(), 100)) : "");
            table.addCell(record.getCreateTime() != null ? record.getCreateTime().format(DATE_FORMATTER) : "");
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    private String getFeedbackText(Integer feedback) {
        if (feedback == null || feedback == 0) return "无";
        if (feedback == 1) return "点赞";
        if (feedback == 2) return "点踩";
        return "未知";
    }
}
