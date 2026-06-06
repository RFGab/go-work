package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.forms.BookingCreateForm;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepo bookingRepo;
    private final RoomRepo roomRepo;
    private final UserRepo userRepo;
    private final BookingApprovalService bookingApprovalService;

    @Override
    @Transactional
    public Long createBookingRequest(Long roomId, Long renterId, BookingCreateForm form) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User renter = userRepo.findById(renterId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        validateRequest(room, form);

        LocalDateTime timeStart = form.getBookingDate().atTime(form.getStartHour(), 0);
        LocalDateTime timeFinish = form.getBookingDate().atTime(form.getFinishHour() == 24 ? 23 : form.getFinishHour(), 0);
        if (form.getFinishHour() == 24) {
            timeFinish = form.getBookingDate().plusDays(1).atStartOfDay();
        }

        boolean overlaps = bookingRepo.existsByRoomIdAndStatusInAndTimeStartLessThanAndTimeFinishGreaterThan(
                roomId,
                BLOCKING_STATUSES,
                timeFinish,
                timeStart
        );
        if (overlaps) {
            throw new IllegalArgumentException("Выбранное время уже занято");
        }

        Booking booking = Booking.builder()
                .room(room)
                .renter(renter)
                .timeStart(timeStart)
                .timeFinish(timeFinish)
                .numOfPeople(form.getNumOfPeople())
                .comment(trimToNull(form.getComment()))
                .status(BookingStatus.PENDING)
                .build();

        Long bookingId = bookingRepo.save(booking).getId();
        bookingApprovalService.sendOwnerApprovalRequest(bookingId);
        return bookingId;
    }

    private void validateRequest(Room room, BookingCreateForm form) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalArgumentException("Комната сейчас недоступна для бронирования");
        }
        if (form.getFinishHour() <= form.getStartHour()) {
            throw new IllegalArgumentException("Время окончания должно быть позже времени начала");
        }
        LocalDateTime timeStart = form.getBookingDate().atTime(form.getStartHour(), 0);
        if (timeStart.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя создать заявку на прошедшее время");
        }
        if (form.getNumOfPeople() > room.getPeopleCapacity()) {
            throw new IllegalArgumentException("Количество людей больше вместимости комнаты");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
