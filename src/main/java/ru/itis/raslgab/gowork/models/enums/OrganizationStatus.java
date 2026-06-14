package ru.itis.raslgab.gowork.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrganizationStatus {
    ACTIVE("Активна"),
    FREEZE("Заморожена"),
    BLOCKED("Заблокирована");

    private final String displayName;
}
