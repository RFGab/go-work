package ru.itis.raslgab.gowork.dto;

import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

public record OrganizationDetailsDto(
        Long id,
        String name,
        String description,
        String cityName,
        String yandexMapLink,
        String contactEmail,
        String contactPhone,
        String ownerFullName,
        String ownerEmail,
        OrganizationStatus status,
        boolean owner,
        boolean admin
) {
    public boolean canManage() {
        return owner || admin;
    }
}
