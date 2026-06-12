package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListItemDto {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long organizationId;
    private String organizationName;
    private String cityName;
    private LocalDateTime timeStart;
    private LocalDateTime timeFinish;
    private Integer numOfPeople;
    private String comment;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
