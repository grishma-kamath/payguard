package com.grishma.payguard.notification.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final PdfGeneratorService pdfGeneratorService;

    public EmailService(JavaMailSender mailSender, PdfGeneratorService pdfGeneratorService) {
        this.mailSender = mailSender;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public void sendFraudAlert(FraudResultEvent event, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject(buildSubject(event));
            helper.setText(buildBody(event), true);

            byte[] pdfReport = pdfGeneratorService.generateFraudReport(event);
            if (pdfReport.length > 0) {
                helper.addAttachment(
                        "fraud-report-" + event.transactionId() + ".pdf",
                        new ByteArrayResource(pdfReport),
                        "application/pdf"
                );
            }

            mailSender.send(message);
            log.info("Fraud alert email sent for transaction: {} to: {}", event.transactionId(), recipientEmail);
        } catch (MessagingException e) {
            log.error("Failed to send fraud alert email for transaction: {}", event.transactionId(), e);
        }
    }

    private String buildSubject(FraudResultEvent event) {
        if (event.blocked()) {
            return "[BLOCKED] Suspicious Transaction Detected - " + event.transactionId();
        }
        return "[FLAGGED] Transaction Under Review - " + event.transactionId();
    }

    private String buildBody(FraudResultEvent event) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: %s;">Fraud Detection Alert</h2>
                    <p>A transaction has been %s by PayGuard Fraud Detection System.</p>
                    <table style="border-collapse: collapse; width: 100%%;">
                        <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Transaction ID</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Account</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Amount</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">INR %.2f</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Risk Level</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                        <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Reason</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                    </table>
                    <p style="color: gray; font-size: 12px;">This is an automated alert from PayGuard v3.0</p>
                </body>
                </html>
                """,
                event.blocked() ? "#cc0000" : "#ff9900",
                event.blocked() ? "BLOCKED" : "FLAGGED",
                event.transactionId(),
                event.accountId(),
                event.amount(),
                event.riskLevel(),
                event.message()
        );
    }
}
