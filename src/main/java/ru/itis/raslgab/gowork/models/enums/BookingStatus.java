package ru.itis.raslgab.gowork.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтверждена"),
    CANCELLED("Отменена"),
    REJECTED("Отклонена"),
    COMPLETED("Завершена");

    private final String displayName;
}
