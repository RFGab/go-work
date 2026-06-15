package ru.itis.raslgab.gowork.dto.rooms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularRoomDto {
    private Long id;
    private String name;
    private String organizationName;
    private String cityName;
    private Integer peopleCapacity;
    private BigDecimal pricePerHour;
    private Long bookingCount;
}
