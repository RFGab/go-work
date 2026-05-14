package ru.itis.raslgab.gowork.models.enums;

// для статуса комнаты
public enum RoomStatus {
    AVAILABLE,
    BOOKED,           // забронена
    UNAVAILABLE,      // владелец вручную закрыл
    MAINTENANCE
}
