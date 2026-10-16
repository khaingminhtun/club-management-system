
package com.jvproject.club_management_system.integration.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final RestClient sendGridRestClient;
    private final EmailTemplateService emailTemplateService;
    private final String fromEmail;

    public EmailService(
            RestClient sendGridRestClient,
            EmailTemplateService emailTemplateService,
            @Value("${sendgrid.from-email}") String fromEmail) {

        this.sendGridRestClient = sendGridRestClient;
        this.emailTemplateService = emailTemplateService;
        this.fromEmail = fromEmail;
    }

    public void sendOtpVerificationEmail(
            String to,
            String name,
            String otp) {

        String html = emailTemplateService.renderOtpVerificationEmail(
                name,
                otp
        );

        sendHtmlEmail(
                to,
                "Verify Your Email - Club Management System",
                html
        );
    }

    public void sendHtmlEmail(
            String to,
            String subject,
            String html) {

        Map<String, Object> request = Map.of(
                "personalizations", List.of(
                        Map.of(
                                "to", List.of(
                                        Map.of("email", to)
                                )
                        )
                ),
                "from", Map.of("email", fromEmail),
                "subject", subject,
                "content", List.of(
                        Map.of(
                                "type", "text/html",
                                "value", html
                        )
                )
        );

        sendGridRestClient.post()
                .uri("/v3/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
