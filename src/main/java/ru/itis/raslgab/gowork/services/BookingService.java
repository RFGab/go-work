package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.forms.BookingCreateForm;
import ru.itis.raslgab.gowork.dto.BookingListItemDto;

import java.util.List;

public interface BookingService {
    Long createBookingRequest(Long roomId, Long renterId, BookingCreateForm form);

    List<BookingListItemDto> getUserBookings(Long renterId);
}
