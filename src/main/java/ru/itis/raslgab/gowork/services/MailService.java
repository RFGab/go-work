package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.BookingMailDto;

public interface MailService {
    void sendEmailForConfirm(String email, String code);

    void sendBookingApprovalRequest(BookingMailDto booking);

    void sendBookingRejected(BookingMailDto booking);

    void sendBookingApproved(BookingMailDto booking, byte[] qrCode);
}
