package ru.itis.raslgab.gowork.services.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.itis.raslgab.gowork.dto.bookings.BookingMailDto;

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
        Context context = new Context();
        context.setVariable("confirm_code", code);
        String mailText = templateEngine.process("mail/confirm", context);
        javaMailSender.send(getEmail(email, "Регистрация", mailText));
    }

    @Override
    public void sendBookingApprovalRequest(BookingMailDto booking) {
        Context context = new Context();
        context.setVariable("booking", booking);
        String mailText = templateEngine.process("mail/booking-owner-request", context);
        javaMailSender.send(getEmail(booking.getOwnerEmail(), "Новая заявка на бронь", mailText));
    }

    @Override
    public void sendBookingRejected(BookingMailDto booking) {
        Context context = new Context();
        context.setVariable("booking", booking);
        String mailText = templateEngine.process("mail/booking-rejected", context);
        javaMailSender.send(getEmail(booking.getRenterEmail(), "По заявке на бронь есть новости", mailText));
    }

    @Override
    public void sendBookingApproved(BookingMailDto booking, byte[] qrCode) {
        Context context = new Context();
        context.setVariable("booking", booking);
        context.setVariable("hasQr", qrCode != null && qrCode.length > 0);
        String mailText = templateEngine.process("mail/booking-approved", context);
        javaMailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setText(mailText, true);
            helper.setTo(booking.getRenterEmail());
            helper.setFrom(mailFrom);
            helper.setSubject("Бронь подтверждена");
            if (qrCode != null && qrCode.length > 0) {
                helper.addInline("bookingQr", new ByteArrayResource(qrCode), "image/png");
            }
        });
    }

    @Override
    public void sendBookingCancelledToOwner(BookingMailDto booking) {
        Context context = new Context();
        context.setVariable("booking", booking);
        String mailText = templateEngine.process("mail/booking-cancelled-owner", context);
        javaMailSender.send(getEmail(booking.getOwnerEmail(), "Бронь отменена арендатором", mailText));
    }

    private MimeMessagePreparator getEmail(String email, String subject, String mailText) {
        return mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setText(mailText, true);
            helper.setTo(email);
            helper.setFrom(mailFrom);
            helper.setSubject(subject);
        };
    }
}
