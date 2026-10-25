package com.attendex.service;

import com.attendex.entity.AttendanceRecord;
import com.attendex.entity.AttendanceStatus;
import com.attendex.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending SMS notifications to parents when a student is marked absent.
 *
 * In DEVELOPMENT mode (default): logs the SMS to the console.
 * In PRODUCTION mode: sends SMS via a configurable HTTP-based SMS gateway API.
 *
 * To enable real SMS, set these properties in application.properties:
 *   attendex.sms.enabled=true
 *   attendex.sms.api-url=https://your-sms-provider.com/api/send
 *   attendex.sms.api-key=your-api-key
 *   attendex.sms.sender-id=ATTNDX
 *
 * Supports most Indian bulk SMS providers (Fast2SMS, MSG91, TextLocal, etc.)
 * that offer HTTP GET/POST APIs.
 */
@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Value("${attendex.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${attendex.sms.api-url:}")
    private String apiUrl;

    @Value("${attendex.sms.api-key:}")
    private String apiKey;

    @Value("${attendex.sms.sender-id:ATTNDX}")
    private String senderId;

    @Value("${attendex.college-name:Attendex Institution}")
    private String collegeName;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Send absence notification SMS to parent for each ABSENT record.
     * This method is @Async so it won't block the attendance marking flow.
     */
    @Async
    public void sendAbsenceAlert(AttendanceRecord record) {
        if (record.getStatus() != AttendanceStatus.ABSENT) {
            return; // Only send for absent students
        }

        Student student = record.getStudent();
        String parentPhone = student.getParentPhone();
        String parentName = student.getParentName();
        String studentName = student.getUser().getFullName();
        String subjectName = record.getAttendanceSession().getSubject().getName();
        String sessionDate = record.getAttendanceSession().getSessionDate()
                .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        int period = record.getAttendanceSession().getSessionNumber();

        // Build SMS message
        String message = String.format(
                "Dear %s, your ward %s (Roll: %s) was marked ABSENT for %s (Period %d) on %s. " +
                "Please contact the class teacher for details. - %s",
                parentName != null && !parentName.isEmpty() ? parentName : "Parent",
                studentName,
                student.getRollNumber(),
                subjectName,
                period,
                sessionDate,
                collegeName
        );

        if (parentPhone == null || parentPhone.trim().isEmpty()) {
            log.warn("SMS SKIPPED — No parent phone number for student: {} ({})",
                    studentName, student.getRollNumber());
            return;
        }

        if (!smsEnabled) {
            // ============ DEVELOPMENT MODE — Log to console ============
            log.info("╔══════════════════════════════════════════════════════════════╗");
            log.info("║  📱 SMS NOTIFICATION (Dev Mode — not actually sent)         ║");
            log.info("╠══════════════════════════════════════════════════════════════╣");
            log.info("║  To:      {}",  parentPhone);
            log.info("║  Student: {} ({})", studentName, student.getRollNumber());
            log.info("║  Subject: {} — Period {}", subjectName, period);
            log.info("║  Date:    {}", sessionDate);
            log.info("║  Message: {}", message);
            log.info("╚══════════════════════════════════════════════════════════════╝");
            return;
        }

        // ============ PRODUCTION MODE — Send via SMS Gateway API ============
        try {
            sendViaSmsGateway(parentPhone, message);
            log.info("SMS SENT to {} for student {} — absent from {} on {}",
                    parentPhone, studentName, subjectName, sessionDate);
        } catch (Exception e) {
            log.error("SMS FAILED to {} for student {}: {}",
                    parentPhone, studentName, e.getMessage(), e);
        }
    }

    /**
     * Send SMS via HTTP-based SMS gateway.
     * Compatible with most Indian bulk SMS providers (Fast2SMS, MSG91, TextLocal).
     */
    private void sendViaSmsGateway(String phone, String message) throws Exception {
        if (apiUrl == null || apiUrl.isEmpty()) {
            log.error("SMS API URL not configured. Set attendex.sms.api-url in application.properties");
            return;
        }

        // Clean the phone number (remove spaces, dashes, +91 prefix)
        String cleanPhone = phone.replaceAll("[\\s\\-+]", "");
        if (cleanPhone.startsWith("91") && cleanPhone.length() == 12) {
            cleanPhone = cleanPhone.substring(2);
        }

        // Build the request URL (works with most SMS APIs that accept GET params)
        String url = String.format("%s?authorization=%s&sender_id=%s&message=%s&numbers=%s&route=q",
                apiUrl,
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
                URLEncoder.encode(senderId, StandardCharsets.UTF_8),
                URLEncoder.encode(message, StandardCharsets.UTF_8),
                URLEncoder.encode(cleanPhone, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("SMS API returned status " + response.statusCode() +
                    ": " + response.body());
        }

        log.debug("SMS API response: {}", response.body());
    }
}
