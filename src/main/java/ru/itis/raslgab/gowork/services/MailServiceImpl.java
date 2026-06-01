package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    @Override
    public void sendEmailForConfirm(String email, String code) {
        String mailText = getEmailText(code);
        MimeMessagePreparator messagePreparator = getEmail(email, mailText);
        javaMailSender.send(messagePreparator);
    }

    private String getEmailText(String code) {
        Context context = new Context();
        context.setVariable("confirm_code", code);
        return templateEngine.process("mail/confirm", context);
    }

    private MimeMessagePreparator getEmail(String email, String mailText) {
        return mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setText(mailText, true); // HTML
            helper.setTo(email);
            helper.setFrom(mailFrom);
            helper.setSubject("Регистрация");
        };
    }
}
