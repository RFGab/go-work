package ru.itis.raslgab.gowork.services.bookings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.bookings.BookingListItemDto;
import ru.itis.raslgab.gowork.dto.bookings.BookingMailDto;
import ru.itis.raslgab.gowork.forms.bookings.BookingCreateForm;
import ru.itis.raslgab.gowork.mappers.BookingDataMapper;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;
import ru.itis.raslgab.gowork.services.mail.MailService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
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
    private final MailService mailService;
    private final BookingDataMapper bookingDataMapper;

    @Override
    @Transactional
    public Long createBookingRequest(Long roomId, Long renterId, BookingCreateForm form) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        User renter = userRepo.findById(renterId)
                .orElseThrow(() -> new IllegalStateException("Текущий пользователь не найден"));

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

        Booking booking = bookingDataMapper.mapCreateFormToModel(form, room, renter, timeStart, timeFinish);

        Long bookingId = bookingRepo.save(booking).getId();
        bookingApprovalService.sendOwnerApprovalRequest(bookingId);
        return bookingId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingListItemDto> getUserBookings(Long renterId) {
        return bookingRepo.findUserBookingItems(renterId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepo.findDetailsById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на бронь не найдена"));
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Эту бронь уже нельзя отменить");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        trySendCancellationToOwner(bookingDataMapper.mapToMailDto(booking));
    }

    private void validateRequest(Room room, BookingCreateForm form) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalArgumentException("Комната сейчас недоступна для бронирования");
        }
        if (form.getFinishHour() <= form.getStartHour()) {
            throw new IllegalArgumentException("Время окончания должно быть позже времени начала");
        }
        if (form.getStartHour() < safeDayStart(room.getDayStart()) || form.getFinishHour() > safeDayEnd(room.getDayEnd())) {
            throw new IllegalArgumentException("Выбранное время должно быть внутри рабочего времени комнаты");
        }
        LocalDateTime timeStart = form.getBookingDate().atTime(form.getStartHour(), 0);
        if (timeStart.isBefore(LocalDateTime.now(zoneOffset(room)))) {
            throw new IllegalArgumentException("Нельзя создать заявку на прошедшее время");
        }
        if (form.getNumOfPeople() > room.getPeopleCapacity()) {
            throw new IllegalArgumentException("Количество людей больше вместимости комнаты");
        }
    }

    private int safeDayStart(Integer value) {
        return value == null ? 9 : Math.max(0, Math.min(24, value));
    }

    private int safeDayEnd(Integer value) {
        return value == null ? 17 : Math.max(0, Math.min(24, value));
    }

    private ZoneOffset zoneOffset(Room room) {
        Integer utc = room.getOrganization() == null || room.getOrganization().getCity() == null
                ? null
                : room.getOrganization().getCity().getUtc();
        int offset = utc == null ? 3 : Math.max(-12, Math.min(14, utc));
        return ZoneOffset.ofHours(offset);
    }

    private void trySendCancellationToOwner(BookingMailDto dto) {
        try {
            mailService.sendBookingCancelledToOwner(dto);
        } catch (Exception e) {
            log.warn("Failed to send booking cancellation email for bookingId={}: {}", dto.getBookingId(), e.getMessage());
        }
    }
}
