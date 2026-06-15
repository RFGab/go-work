package ru.itis.raslgab.gowork.services.qr;

import ru.itis.raslgab.gowork.dto.bookings.BookingMailDto;

public interface QrCodeService {
    byte[] generateBookingQr(BookingMailDto booking);
}
