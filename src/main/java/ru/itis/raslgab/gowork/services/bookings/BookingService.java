package ru.itis.raslgab.gowork.services.bookings;

import ru.itis.raslgab.gowork.forms.bookings.BookingCreateForm;
import ru.itis.raslgab.gowork.dto.bookings.BookingListItemDto;

import java.util.List;

public interface BookingService {
    Long createBookingRequest(Long roomId, Long renterId, BookingCreateForm form);

    List<BookingListItemDto> getUserBookings(Long renterId);

    void cancelBooking(Long bookingId);
}
