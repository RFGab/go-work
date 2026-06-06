package ru.itis.raslgab.gowork.dto;

public record OrganizationCatalogItemDto(
        Long id,
        String name,
        String description,
        String cityName,
        String contactEmail,
        String contactPhone,
        Long roomCount
) {
}
