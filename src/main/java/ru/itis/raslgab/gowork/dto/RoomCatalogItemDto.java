package ru.itis.raslgab.gowork.dto;

import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;

public record RoomCatalogItemDto(
        Long id,
        String name,
        String description,
        Long organizationId,
        String organizationName,
        String cityName,
        Integer peopleCapacity,
        BigDecimal pricePerHour,
        RoomStatus status,
        BigDecimal availableHoursToday
) {
    public RoomCatalogItemDto(Long id,
                              String name,
                              String description,
                              Integer peopleCapacity,
                              BigDecimal pricePerHour,
                              RoomStatus status) {
        this(id, name, description, null, null, null, peopleCapacity, pricePerHour, status, null);
    }

    public RoomCatalogItemDto(Long id,
                              String name,
                              String description,
                              Long organizationId,
                              String organizationName,
                              String cityName,
                              Integer peopleCapacity,
                              BigDecimal pricePerHour,
                              RoomStatus status) {
        this(id, name, description, organizationId, organizationName, cityName, peopleCapacity, pricePerHour, status, null);
    }

    public RoomCatalogItemDto withAvailableHoursToday(BigDecimal availableHoursToday) {
        return new RoomCatalogItemDto(
                id,
                name,
                description,
                organizationId,
                organizationName,
                cityName,
                peopleCapacity,
                pricePerHour,
                status,
                availableHoursToday
        );
    }
}
