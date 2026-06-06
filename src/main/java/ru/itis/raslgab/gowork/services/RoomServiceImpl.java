package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.BookingIntervalDto;
import ru.itis.raslgab.gowork.dto.BookingHourSlotDto;
import ru.itis.raslgab.gowork.dto.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.SimilarRoomDto;
import ru.itis.raslgab.gowork.forms.RoomCreateForm;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );
    private static final BigDecimal DAY_HOURS = BigDecimal.valueOf(24);
    private static final List<RoomStatus> CREATE_STATUSES = List.of(
            RoomStatus.AVAILABLE,
            RoomStatus.UNAVAILABLE,
            RoomStatus.MAINTENANCE
    );

    private final RoomRepo roomRepo;
    private final OrganizationRepo organizationRepo;
    private final BookingRepo bookingRepo;

    @Override
    public List<RoomStatus> getCreateStatuses() {
        return CREATE_STATUSES;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDetailsDto getRoomDetails(Long roomId) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<BookingIntervalDto> intervals = bookingRepo.findBlockingIntervals(
                List.of(roomId),
                BLOCKING_STATUSES,
                dayStart,
                dayEnd
        );
        return room.withAvailableHoursToday(calculateAvailableHours(room.getStatus(), intervals, dayStart, dayEnd));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHourSlotDto> getHourSlots(Long roomId, LocalDate bookingDate) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        LocalDate selectedDate = bookingDate == null ? LocalDate.now() : bookingDate;
        LocalDateTime dayStart = selectedDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<BookingIntervalDto> intervals = bookingRepo.findBlockingIntervals(
                List.of(roomId),
                BLOCKING_STATUSES,
                dayStart,
                dayEnd
        );

        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> {
                    LocalDateTime slotStart = selectedDate.atTime(hour, 0);
                    LocalDateTime slotEnd = slotStart.plusHours(1);
                    boolean available = room.getStatus() == RoomStatus.AVAILABLE
                            && slotEnd.isAfter(LocalDateTime.now())
                            && intervals.stream().noneMatch(interval -> overlaps(
                            slotStart,
                            slotEnd,
                            interval.getTimeStart(),
                            interval.getTimeFinish()
                    ));
                    return BookingHourSlotDto.builder()
                            .hour(hour)
                            .label(String.format("%02d:00", hour))
                            .available(available)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomOptionDto> getOptions(Long roomId) {
        return roomRepo.findOptionsByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimilarRoomDto> getSimilarRooms(Long roomId) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        if (room.getCityId() == null) {
            return List.of();
        }
        return roomRepo.findSimilarRooms(roomId, room.getCityId(), room.getPeopleCapacity(), OrganizationStatus.ACTIVE)
                .stream()
                .limit(3)
                .toList();
    }

    @Override
    @Transactional
    public Long createRoom(Long organizationId, Long ownerId, RoomCreateForm form) {
        Organization organization = organizationRepo.findDetailsById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        if (organization.getOwner() == null || !organization.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Only organization owner can create rooms");
        }

        Room room = Room.builder()
                .name(form.getName().trim())
                .description(trimToNull(form.getDescription()))
                .peopleCapacity(form.getPeopleCapacity())
                .pricePerHour(form.getPricePerHour())
                .status(form.getStatus())
                .organization(organization)
                .build();

        return roomRepo.save(room).getId();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean overlaps(LocalDateTime firstStart,
                             LocalDateTime firstFinish,
                             LocalDateTime secondStart,
                             LocalDateTime secondFinish) {
        return firstStart.isBefore(secondFinish) && firstFinish.isAfter(secondStart);
    }

    private BigDecimal calculateAvailableHours(RoomStatus status,
                                               List<BookingIntervalDto> intervals,
                                               LocalDateTime dayStart,
                                               LocalDateTime dayEnd) {
        if (status != RoomStatus.AVAILABLE) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long busyMinutes = mergeAndCountBusyMinutes(intervals, dayStart, dayEnd);
        BigDecimal busyHours = BigDecimal.valueOf(busyMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal available = DAY_HOURS.subtract(busyHours);
        return available.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private long mergeAndCountBusyMinutes(List<BookingIntervalDto> intervals,
                                          LocalDateTime dayStart,
                                          LocalDateTime dayEnd) {
        List<TimeRange> ranges = intervals.stream()
                .map(interval -> new TimeRange(
                        interval.getTimeStart().isBefore(dayStart) ? dayStart : interval.getTimeStart(),
                        interval.getTimeFinish().isAfter(dayEnd) ? dayEnd : interval.getTimeFinish()
                ))
                .filter(range -> range.start().isBefore(range.end()))
                .sorted(Comparator.comparing(TimeRange::start))
                .toList();

        if (ranges.isEmpty()) {
            return 0;
        }

        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = ranges.get(0);
        for (int i = 1; i < ranges.size(); i++) {
            TimeRange next = ranges.get(i);
            if (!next.start().isAfter(current.end())) {
                current = new TimeRange(current.start(), max(current.end(), next.end()));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged.stream()
                .mapToLong(range -> Duration.between(range.start(), range.end()).toMinutes())
                .sum();
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
