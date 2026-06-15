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
public class BookingIntervalDto {
    private Long roomId;
    private LocalDateTime timeStart;
    private LocalDateTime timeFinish;
}
