package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.BookingMailDto;

public interface QrCodeService {
    byte[] generateBookingQr(BookingMailDto booking);
}
