
package com.jvproject.club_management_system.integration.email;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

@Service
public class EmailTemplateService {

    private final SpringTemplateEngine templateEngine;

    public EmailTemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderOtpVerificationEmail(String name, String otp) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        return templateEngine.process(
                "email/otp-verification",
                context
        );
    }
}
