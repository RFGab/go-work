package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.forms.BookingCreateForm;

public interface BookingService {
    Long createBookingRequest(Long roomId, Long renterId, BookingCreateForm form);
}
