package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingMailDto {
    private Long bookingId;
    private String renterFullName;
    private String renterEmail;
    private String renterPhone;
    private String renterProfilePhoto;
    private String ownerEmail;
    private String roomName;
    private String organizationName;
    private LocalDateTime timeStart;
    private LocalDateTime timeFinish;
    private Integer numOfPeople;
    private String comment;
    private String approvalCode;
    private String approveUrl;
    private String rejectUrl;
}
