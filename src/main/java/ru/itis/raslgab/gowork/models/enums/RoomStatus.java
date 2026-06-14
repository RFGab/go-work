package ru.itis.raslgab.gowork.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    AVAILABLE("Доступна"),
    BOOKED("Забронирована"),
    UNAVAILABLE("Недоступна"),
    MAINTENANCE("На обслуживании");

    private final String displayName;
}
