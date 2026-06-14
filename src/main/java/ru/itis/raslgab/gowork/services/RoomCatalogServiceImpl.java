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
import java.time.ZoneOffset;
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
    private final RoomRepo roomRepo;
    private final BookingRepo bookingRepo;
    private final CityRepo cityRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomCatalogItemDto> getCatalog(RoomCatalogFilterForm filter) {
        RoomCatalogFilterForm safeFilter = filter == null ? RoomCatalogFilterForm.builder().build() : filter;
        Pageable pageable = PageRequest.of(
                safePage(safeFilter.getPage()),
                safeSize(safeFilter.getSize()),
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<RoomCatalogItemDto> roomsPage = roomRepo.findRoomCatalogBaseItems(
                safeFilter.getCityId(),
                safeFilter.getMinCapacity(),
                OrganizationStatus.ACTIVE,
                Boolean.TRUE.equals(safeFilter.getAvailableToday()),
                RoomStatus.AVAILABLE,
                pageable
        );
        List<RoomCatalogItemDto> rooms = roomsPage.getContent();

        if (rooms.isEmpty()) {
            return roomsPage;
        }

        List<Long> roomIds = rooms.stream()
                .map(RoomCatalogItemDto::getId)
                .toList();
        Map<Long, List<BookingIntervalDto>> intervalsByRoom = bookingRepo.findBlockingIntervals(
                        roomIds,
                        BLOCKING_STATUSES,
                        LocalDate.now().minusDays(1).atStartOfDay(),
                        LocalDate.now().plusDays(2).atStartOfDay()
                ).stream()
                .collect(Collectors.groupingBy(BookingIntervalDto::getRoomId));

        List<RoomCatalogItemDto> enrichedRooms = rooms.stream()
                .peek(room -> {
                    room.setAvailableHoursToday(calculateAvailableHours(room, intervalsByRoom.get(room.getId())));
                    room.setCoverImageFileName(getCoverImageFileName(room.getId()));
                })
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
                                               List<BookingIntervalDto> intervals) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        LocalDate today = LocalDate.now(zoneOffset(room.getCityUtc()));
        LocalDateTime workStart = atHour(today, safeDayStart(room.getDayStart()));
        LocalDateTime workEnd = workEnd(today, room.getDayEnd());
        long workMinutes = Duration.between(workStart, workEnd).toMinutes();
        if (workMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long busyMinutes = mergeAndCountBusyMinutes(intervals == null ? List.of() : intervals, workStart, workEnd);
        BigDecimal busyHours = BigDecimal.valueOf(busyMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal workHours = BigDecimal.valueOf(workMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal available = workHours.subtract(busyHours);
        return available.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime workEnd(LocalDate date, Integer dayEnd) {
        return atHour(date, safeDayEnd(dayEnd));
    }

    private int safeDayStart(Integer value) {
        return value == null ? 9 : Math.max(0, Math.min(24, value));
    }

    private int safeDayEnd(Integer value) {
        return value == null ? 17 : Math.max(0, Math.min(24, value));
    }

    private ZoneOffset zoneOffset(Integer utc) {
        int offset = utc == null ? 3 : Math.max(-12, Math.min(14, utc));
        return ZoneOffset.ofHours(offset);
    }

    private LocalDateTime atHour(LocalDate date, int hour) {
        return hour == 24 ? date.plusDays(1).atStartOfDay() : date.atTime(hour, 0);
    }

    private String getCoverImageFileName(Long roomId) {
        List<String> fileNames = roomRepo.findImageFileNamesOrderById(roomId, PageRequest.of(0, 1));
        return fileNames.isEmpty() ? null : fileNames.get(0);
    }

    private long mergeAndCountBusyMinutes(List<BookingIntervalDto> intervals,
                                          LocalDateTime dayStart,
                                          LocalDateTime dayEnd) {
        List<TimeRange> ranges = intervals.stream()
                .map(interval -> new TimeRange(
                        interval.getTimeStart().isBefore(dayStart) ? dayStart : interval.getTimeStart(),
                        interval.getTimeFinish().isAfter(dayEnd) ? dayEnd : interval.getTimeFinish()
                ))
                .filter(range -> range.getStart().isBefore(range.getEnd()))
                .sorted(Comparator.comparing(TimeRange::getStart))
                .toList();

        if (ranges.isEmpty()) {
            return 0;
        }

        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = ranges.get(0);
        for (int i = 1; i < ranges.size(); i++) {
            TimeRange next = ranges.get(i);
            if (!next.getStart().isAfter(current.getEnd())) {
                current = new TimeRange(current.getStart(), max(current.getEnd(), next.getEnd()));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged.stream()
                .mapToLong(range -> Duration.between(range.getStart(), range.getEnd()).toMinutes())
                .sum();
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 9;
        }
        return Math.min(size, 30);
    }

    private static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        private LocalDateTime getStart() {
            return start;
        }

        private LocalDateTime getEnd() {
            return end;
        }
    }
}
