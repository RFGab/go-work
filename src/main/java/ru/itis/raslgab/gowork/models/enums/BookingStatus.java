package ru.itis.raslgab.gowork.models.enums;

// для статуса брони
public enum BookingStatus {
    PENDING, // бронь ожидает подтверждения
    CONFIRMED, // бронь подтверждена
    CANCELLED, // отменена
    REJECTED,
    COMPLETED,
}
