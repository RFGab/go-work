package ru.itis.raslgab.gowork.dto;

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

    public RoomDetailsDto(Long id,
                          String name,
                          String description,
                          Long organizationId,
                          String organizationName,
                          Long cityId,
                          String cityName,
                          String organizationContactEmail,
                          String organizationContactPhone,
                          Integer peopleCapacity,
                          BigDecimal pricePerHour,
                          RoomStatus status) {
        this(id, name, description, organizationId, organizationName, cityId, cityName, organizationContactEmail,
                organizationContactPhone, peopleCapacity, pricePerHour, status, null);
    }

    public RoomDetailsDto withAvailableHoursToday(BigDecimal availableHoursToday) {
        return RoomDetailsDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .organizationId(organizationId)
                .organizationName(organizationName)
                .cityId(cityId)
                .cityName(cityName)
                .organizationContactEmail(organizationContactEmail)
                .organizationContactPhone(organizationContactPhone)
                .peopleCapacity(peopleCapacity)
                .pricePerHour(pricePerHour)
                .status(status)
                .availableHoursToday(availableHoursToday)
                .build();
    }
}
