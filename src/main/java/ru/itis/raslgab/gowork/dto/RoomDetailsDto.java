package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailsDto {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private Long cityId;
    private String cityName;
    private String organizationContactEmail;
    private String organizationContactPhone;
    private Integer peopleCapacity;
    private BigDecimal pricePerHour;
    private RoomStatus status;
    private BigDecimal availableHoursToday;
    private List<String> imageFileNames;
    private boolean canManage;
    private Integer dayStart;
    private Integer dayEnd;
    private Integer cityUtc;
}
