package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHourSlotDto {
    private Integer hour;
    private String label;
    private boolean available;
}
