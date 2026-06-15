package ru.itis.raslgab.gowork.services.bookings;

import ru.itis.raslgab.gowork.dto.bookings.BookingApprovalPageDto;

public interface BookingApprovalService {
    void sendOwnerApprovalRequest(Long bookingId);

    BookingApprovalPageDto getApprovalPage(Long bookingId, String action);

    void confirmDecision(Long bookingId, String action, String code);
}
