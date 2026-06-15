package ru.itis.raslgab.gowork.dto.bookings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingApprovalPageDto {
    private Long bookingId;
    private String action;
    private String actionTitle;
    private String renterFullName;
    private String roomName;
    private String organizationName;
    private LocalDateTime timeStart;
    private LocalDateTime timeFinish;
}
