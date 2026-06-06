package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.BookingApprovalPageDto;

public interface BookingApprovalService {
    void sendOwnerApprovalRequest(Long bookingId);

    BookingApprovalPageDto getApprovalPage(Long bookingId, String action);

    void confirmDecision(Long bookingId, String action, String code);
}
