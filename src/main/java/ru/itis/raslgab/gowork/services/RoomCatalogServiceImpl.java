package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.BookingIntervalDto;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.RoomCatalogFilterForm;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.CityRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCatalogServiceImpl implements RoomCatalogService {
    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );
    private static final BigDecimal DAY_HOURS = BigDecimal.valueOf(24);

    private final RoomRepo roomRepo;
    private final BookingRepo bookingRepo;
    private final CityRepo cityRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomCatalogItemDto> getCatalog(RoomCatalogFilterForm filter) {
        RoomCatalogFilterForm safeFilter = filter == null ? new RoomCatalogFilterForm() : filter;
        Pageable pageable = PageRequest.of(
                safeFilter.safePage(),
                safeFilter.safeSize(),
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<RoomCatalogItemDto> roomsPage = roomRepo.findRoomCatalogBaseItems(
                safeFilter.getCityId(),
                safeFilter.getMinCapacity(),
                OrganizationStatus.ACTIVE,
                safeFilter.isAvailableTodaySelected(),
                RoomStatus.AVAILABLE,
                pageable
        );
        List<RoomCatalogItemDto> rooms = roomsPage.getContent();

        if (rooms.isEmpty()) {
            return roomsPage;
        }

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Long> roomIds = rooms.stream()
                .map(RoomCatalogItemDto::id)
                .toList();
        Map<Long, List<BookingIntervalDto>> intervalsByRoom = bookingRepo.findBlockingIntervals(
                        roomIds,
                        BLOCKING_STATUSES,
                        dayStart,
                        dayEnd
                ).stream()
                .collect(Collectors.groupingBy(BookingIntervalDto::getRoomId));

        List<RoomCatalogItemDto> enrichedRooms = rooms.stream()
                .map(room -> room.withAvailableHoursToday(calculateAvailableHours(room, intervalsByRoom.get(room.id()), dayStart, dayEnd)))
                .toList();

        return new PageImpl<>(
                enrichedRooms,
                pageable,
                roomsPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityOptionDto> getCityOptions() {
        return cityRepo.findOptions();
    }

    private BigDecimal calculateAvailableHours(RoomCatalogItemDto room,
                                               List<BookingIntervalDto> intervals,
                                               LocalDateTime dayStart,
                                               LocalDateTime dayEnd) {
        if (room.status() != RoomStatus.AVAILABLE) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long busyMinutes = mergeAndCountBusyMinutes(intervals == null ? List.of() : intervals, dayStart, dayEnd);
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
