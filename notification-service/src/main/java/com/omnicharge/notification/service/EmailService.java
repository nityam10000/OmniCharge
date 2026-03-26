package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, NotificationEvent event) {
        sendEmail(to, event, resolveMessage(event));
    }

    public void sendEmail(String to, NotificationEvent event, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            EmailContent content = resolveEmailContent(event, message);
            String html = renderTemplate(content.templateName, content.variables);

            helper.setTo(to);
            helper.setSubject(content.subject);
            helper.setText(html, true);
            helper.setFrom(fromEmail, "OmniCharge");

            mailSender.send(mimeMessage);
            System.out.println("Email sent to " + to + " for type " + normalizeType(event.getType()));

        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }

    private EmailContent resolveEmailContent(NotificationEvent event, String fallbackMessage) {
        String type = normalizeType(event.getType());
        String message = escapeHtml(resolveMessage(event, fallbackMessage)).replace("\n", "<br/>");

        if (hasValue(event.getRechargeId())) {
            return buildRechargeContent(event, message);
        }
        if ("OTP_LOGIN".equals(type) || "OTP_FORGOT_PASSWORD".equals(type)) {
            return buildOtpContent(event, message, type);
        }
        if ("WELCOME".equals(type) || "REGISTER".equals(type)) {
            return buildRegistrationContent(event, message);
        }
        if ("PAYMENT_REFUND".equals(type)) {
            return buildRefundContent(event, message);
        }
        if ("PAYMENT_SUCCESS".equals(type)) {
            return buildTransactionSuccessContent(event, message);
        }
        if ("PAYMENT_FAILED".equals(type)) {
            return buildTransactionFailedContent(event, message);
        }
        return buildGenericContent(event, message);
    }

    private EmailContent buildOtpContent(NotificationEvent event, String message, String type) {
        Map<String, String> variables = baseVariables(
                "OTP_FORGOT_PASSWORD".equals(type) ? "Password Reset OTP" : "Login OTP",
                "OTP_FORGOT_PASSWORD".equals(type)
                        ? "Use this OTP to reset your OmniCharge password."
                        : "Use this OTP to complete your OmniCharge sign-in.",
                message,
                "#1d4ed8",
                "#2563eb"
        );
        variables.put("card_title", "One-Time Password");
        variables.put("card_bg", "#eff6ff");
        variables.put("card_border", "#93c5fd");
        variables.put("card_text", "#1e3a8a");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "OmniCharge OTP Verification"),
                "templates/otp-email.html",
                variables
        );
    }

    private EmailContent buildRegistrationContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Registration Successful",
                "Your OmniCharge account has been created successfully.",
                message,
                "#0f766e",
                "#22c55e"
        );
        variables.put("card_title", "Welcome");
        variables.put("card_bg", "#ecfdf5");
        variables.put("card_border", "#86efac");
        variables.put("card_text", "#14532d");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Welcome to OmniCharge!"),
                "templates/register-email.html",
                variables
        );
    }

    private EmailContent buildRefundContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Refund Update",
                "A refund has been initiated for your OmniCharge payment.",
                message,
                "#b45309",
                "#f59e0b"
        );
        variables.put("card_title", "Refund Details");
        variables.put("card_bg", "#fffbeb");
        variables.put("card_border", "#fcd34d");
        variables.put("card_text", "#78350f");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Refund Initiated - OmniCharge"),
                "templates/refund-email.html",
                variables
        );
    }

    private EmailContent buildTransactionSuccessContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Transaction Successful",
                "Your OmniCharge transaction was completed successfully.",
                message,
                "#0f766e",
                "#22c55e"
        );
        variables.put("card_title", "Transaction Details");
        variables.put("card_bg", "#ecfdf5");
        variables.put("card_border", "#86efac");
        variables.put("card_text", "#14532d");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Payment Successful - OmniCharge"),
                "templates/transaction-success.html",
                variables
        );
    }

    private EmailContent buildTransactionFailedContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Transaction Failed",
                "Your OmniCharge transaction could not be completed.",
                message,
                "#b91c1c",
                "#ef4444"
        );
        variables.put("card_title", "Failure Details");
        variables.put("card_bg", "#fef2f2");
        variables.put("card_border", "#fca5a5");
        variables.put("card_text", "#7f1d1d");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Payment Failed - OmniCharge"),
                "templates/transaction-failed.html",
                variables
        );
    }

    private EmailContent buildRechargeContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Recharge Successful",
                "Your recharge has been processed successfully. Keep the recharge ID for future reference.",
                message,
                "#0f766e",
                "#16a34a"
        );
        variables.put("recharge_id", escapeHtml(defaultValue(event.getRechargeId())));
        variables.put("mobile", escapeHtml(defaultValue(event.getMobile())));
        variables.put("operator", escapeHtml(defaultValue(event.getOperator())));
        variables.put("amount", escapeHtml(formatAmount(event.getAmount())));
        variables.put("date", escapeHtml(defaultValue(event.getDate())));

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Recharge Successful - OmniCharge"),
                "templates/recharge-success.html",
                variables
        );
    }

    private EmailContent buildGenericContent(NotificationEvent event, String message) {
        Map<String, String> variables = baseVariables(
                "Notification Update",
                "You have received a new notification from OmniCharge.",
                message,
                "#0f766e",
                "#16a34a"
        );
        variables.put("card_title", "Message");
        variables.put("card_bg", "#fff7ed");
        variables.put("card_border", "#fdba74");
        variables.put("card_text", "#7c2d12");

        return new EmailContent(
                firstNonBlank(event.getSubject(), "Notification from OmniCharge"),
                "templates/generic-email.html",
                variables
        );
    }

    private Map<String, String> baseVariables(String title, String intro, String message,
                                              String gradientStart, String gradientEnd) {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("title", escapeHtml(title));
        variables.put("intro", escapeHtml(intro));
        variables.put("message", message);
        variables.put("gradient_start", gradientStart);
        variables.put("gradient_end", gradientEnd);
        return variables;
    }

    private String renderTemplate(String templatePath, Map<String, String> variables) throws IOException {
        String html = readTemplate(templatePath);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    private String readTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String resolveMessage(NotificationEvent event) {
        return resolveMessage(event, event.getMessage());
    }

    private String resolveMessage(NotificationEvent event, String fallbackMessage) {
        if (hasValue(fallbackMessage)) {
            return fallbackMessage;
        }
        if (hasValue(event.getRechargeId())) {
            return "Recharge successful.<br/>Recharge ID: " + escapeHtml(defaultValue(event.getRechargeId()));
        }
        return "You have received a new notification from OmniCharge.";
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String primary, String fallback) {
        return hasValue(primary) ? primary : fallback;
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultValue(String value) {
        return hasValue(value) ? value : "-";
    }

    private String formatAmount(double amount) {
        DecimalFormat formatter = new DecimalFormat("0.00");
        return "Rs. " + formatter.format(amount);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static class EmailContent {
        private final String subject;
        private final String templateName;
        private final Map<String, String> variables;

        private EmailContent(String subject, String templateName, Map<String, String> variables) {
            this.subject = subject;
            this.templateName = templateName;
            this.variables = variables;
        }
    }
}