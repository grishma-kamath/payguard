package com.grishma.payguard.notification.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateFraudReport(FraudResultEvent event) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document);
            addTransactionDetails(document, event);
            addFraudAssessment(document, event);
            addFooter(document);

            document.close();
            log.info("Generated PDF fraud report for transaction: {}", event.transactionId());
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for transaction: {}", event.transactionId(), e);
            return new byte[0];
        }
    }

    private void addHeader(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("ICICI iMobile Pay - Fraud Alert Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        Paragraph subtitle = new Paragraph("PayGuard Fraud Detection System v3.0", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);
    }

    private void addTransactionDetails(Document document, FraudResultEvent event) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        document.add(new Paragraph("Transaction Details", sectionFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 2});

        addTableRow(table, "Transaction ID", event.transactionId());
        addTableRow(table, "Account ID", event.accountId());
        addTableRow(table, "Amount", String.format("INR %.2f", event.amount()));
        addTableRow(table, "Assessed At", event.assessedAt().format(FORMATTER));

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addFraudAssessment(Document document, FraudResultEvent event) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        document.add(new Paragraph("Fraud Assessment", sectionFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 2});

        addTableRow(table, "Risk Level", event.riskLevel());
        addTableRow(table, "Flag", event.flag());
        addTableRow(table, "Blocked", event.blocked() ? "YES" : "NO");
        addTableRow(table, "Message", event.message());

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
                "This is an auto-generated report by PayGuard Fraud Detection System. " +
                "For queries, contact the fraud operations team.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addTableRow(PdfPTable table, String key, String value) {
        Font keyFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        PdfPCell keyCell = new PdfPCell(new Phrase(key, keyFont));
        keyCell.setPadding(8);
        keyCell.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }
}
