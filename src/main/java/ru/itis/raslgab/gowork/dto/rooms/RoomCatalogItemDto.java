package ru.itis.raslgab.gowork.dto.rooms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCatalogItemDto {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private String cityName;
    private Integer peopleCapacity;
    private BigDecimal pricePerHour;
    private RoomStatus status;
    private BigDecimal availableHoursToday;
    private Integer dayStart;
    private Integer dayEnd;
    private Integer cityUtc;
    private String coverImageFileName;
}
