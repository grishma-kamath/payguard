package com.grishma.payguard.notification.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorServiceTest {

    private final PdfGeneratorService pdfGeneratorService = new PdfGeneratorService();

    @Test
    void shouldGeneratePdfForBlockedTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN001", "ACC123", 60000.0, "HIGH", "AMOUNT_THRESHOLD",
                true, "Blocked for review", LocalDateTime.now()
        );

        byte[] pdf = pdfGeneratorService.generateFraudReport(event);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));
    }

    @Test
    void shouldGeneratePdfForFlaggedTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN002", "ACC456", 25000.0, "MEDIUM", "LARGE_AMOUNT",
                false, "Flagged for review", LocalDateTime.now()
        );

        byte[] pdf = pdfGeneratorService.generateFraudReport(event);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void shouldGeneratePdfForSafeTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN003", "ACC789", 5000.0, "LOW", "NONE",
                false, "Transaction looks safe", LocalDateTime.now()
        );

        byte[] pdf = pdfGeneratorService.generateFraudReport(event);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
