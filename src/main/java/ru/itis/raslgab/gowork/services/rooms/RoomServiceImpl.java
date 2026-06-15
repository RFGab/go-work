package ru.itis.raslgab.gowork.services.rooms;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.bookings.BookingIntervalDto;
import ru.itis.raslgab.gowork.dto.bookings.BookingHourSlotDto;
import ru.itis.raslgab.gowork.dto.rooms.PopularRoomDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.rooms.SimilarRoomDto;
import ru.itis.raslgab.gowork.forms.rooms.RoomCreateForm;
import ru.itis.raslgab.gowork.forms.rooms.RoomUpdateForm;
import ru.itis.raslgab.gowork.mappers.RoomDataMapper;
import ru.itis.raslgab.gowork.models.FileInfo;
import ru.itis.raslgab.gowork.models.Option;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.FileInfoRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.OptionRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.services.files.FileStorageService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );
    private static final List<RoomStatus> CREATE_STATUSES = List.of(
            RoomStatus.AVAILABLE,
            RoomStatus.UNAVAILABLE,
            RoomStatus.MAINTENANCE
    );

    private final RoomRepo roomRepo;
    private final OrganizationRepo organizationRepo;
    private final BookingRepo bookingRepo;
    private final FileInfoRepo fileInfoRepo;
    private final FileStorageService fileStorageService;
    private final OptionRepo optionRepo;
    private final RoomDataMapper roomDataMapper;

    @Override
    public List<RoomStatus> getCreateStatuses() {
        return CREATE_STATUSES;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDetailsDto getRoomDetails(Long roomId) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        LocalDate today = LocalDate.now(zoneOffset(room.getCityUtc()));
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<BookingIntervalDto> intervals = bookingRepo.findBlockingIntervals(
                List.of(roomId),
                BLOCKING_STATUSES,
                dayStart,
                dayEnd
        );
        room.setAvailableHoursToday(calculateAvailableHours(room.getStatus(), intervals, workStart(room, today), workEnd(room, today)));
        room.setImageFileNames(getRoomImageFileNames(roomId));
        return room;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHourSlotDto> getHourSlots(Long roomId, LocalDate bookingDate) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        LocalDate selectedDate = bookingDate == null ? LocalDate.now(zoneOffset(room.getCityUtc())) : bookingDate;
        LocalDateTime dayStart = selectedDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime now = LocalDateTime.now(zoneOffset(room.getCityUtc()));
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
                            && hour >= safeDayStart(room.getDayStart())
                            && hour < safeDayEnd(room.getDayEnd())
                            && slotEnd.isAfter(now)
                            && intervals.stream().noneMatch(interval -> overlaps(
                            slotStart,
                            slotEnd,
                            interval.getTimeStart(),
                            interval.getTimeFinish()
                    ));
                    return roomDataMapper.mapToHourSlot(hour, available);
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
    public List<RoomOptionDto> getAllOptions() {
        return optionRepo.findAllOptions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimilarRoomDto> getSimilarRooms(Long roomId) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        if (room.getCityId() == null) {
            return List.of();
        }
        return roomRepo.findSimilarRooms(roomId, room.getCityId(), room.getPeopleCapacity(), OrganizationStatus.ACTIVE)
                .stream()
                .limit(3)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularRoomDto> getPopularRooms() {
        return roomRepo.findPopularRooms(OrganizationStatus.ACTIVE, PageRequest.of(0, 3));
    }

    @Override
    @Transactional
    public Long createRoom(Long organizationId, RoomCreateForm form) {
        Organization organization = organizationRepo.findDetailsById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));

        Set<Option> options = form.getOptionIds() == null || form.getOptionIds().isEmpty()
                ? Set.of()
                : new HashSet<>(optionRepo.findAllById(form.getOptionIds()));

        Room room = roomDataMapper.mapCreateFormToModel(form, organization, options);

        validateWorkingHours(room.getDayStart(), room.getDayEnd());
        return roomRepo.save(room).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomUpdateForm getUpdateForm(Long roomId) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        return roomDataMapper.mapToUpdateForm(room);
    }

    @Override
    @Transactional
    public void updateRoom(Long roomId, RoomUpdateForm form) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        validateWorkingHours(form.getDayStart(), form.getDayEnd());

        Set<Option> options = form.getOptionIds() == null || form.getOptionIds().isEmpty()
                ? Set.of()
                : new HashSet<>(optionRepo.findAllById(form.getOptionIds()));

        roomDataMapper.updateFromForm(room, form, options);
    }

    @Override
    @Transactional
    public void addRoomImages(Long roomId, List<MultipartFile> images) {
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        List<MultipartFile> validImages = images == null ? List.of() : images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (validImages.isEmpty()) {
            throw new IllegalArgumentException("Выберите хотя бы одно фото комнаты");
        }

        Set<FileInfo> roomImages = room.getImages() == null ? new HashSet<>() : new HashSet<>(room.getImages());
        for (MultipartFile image : validImages) {
            validateImage(image);
            String fileName = fileStorageService.saveFile(image);
            roomImages.add(fileInfoRepo.findByStorageFileName(fileName));
        }
        room.setImages(roomImages);
        roomRepo.save(room);
    }

    @Override
    @Transactional
    public void deleteRoomImage(Long roomId, String fileName) {
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Фото не выбрано");
        }
        Set<FileInfo> images = room.getImages() == null ? new HashSet<>() : new HashSet<>(room.getImages());
        boolean removed = images.removeIf(image -> fileName.equals(image.getStorageFileName()));
        if (!removed) {
            throw new IllegalArgumentException("Фото комнаты не найдено");
        }
        room.setImages(images);
        roomRepo.save(room);
    }

    @Override
    @Transactional
    public void updateStatus(Long roomId, RoomStatus status) {
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        room.setStatus(status);
    }

    private List<String> getRoomImageFileNames(Long roomId) {
        return roomRepo.findByIdWithImages(roomId)
                .map(room -> room.getImages() == null ? List.<String>of() : room.getImages().stream()
                        .map(FileInfo::getStorageFileName)
                        .toList())
                .orElse(List.of());
    }

    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загрузить только изображения");
        }
    }

    private boolean overlaps(LocalDateTime firstStart,
                             LocalDateTime firstFinish,
                             LocalDateTime secondStart,
                             LocalDateTime secondFinish) {
        return firstStart.isBefore(secondFinish) && firstFinish.isAfter(secondStart);
    }

    private BigDecimal calculateAvailableHours(RoomStatus status,
                                               List<BookingIntervalDto> intervals,
                                               LocalDateTime workStart,
                                               LocalDateTime workEnd) {
        if (status != RoomStatus.AVAILABLE) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long workMinutes = Duration.between(workStart, workEnd).toMinutes();
        if (workMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long busyMinutes = mergeAndCountBusyMinutes(intervals, workStart, workEnd);
        BigDecimal busyHours = BigDecimal.valueOf(busyMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal workHours = BigDecimal.valueOf(workMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal available = workHours.subtract(busyHours);
        return available.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime workStart(RoomDetailsDto room, LocalDate date) {
        return atHour(date, safeDayStart(room.getDayStart()));
    }

    private LocalDateTime workEnd(RoomDetailsDto room, LocalDate date) {
        return atHour(date, safeDayEnd(room.getDayEnd()));
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

    private void validateWorkingHours(Integer dayStart, Integer dayEnd) {
        if (dayStart == null || dayEnd == null) {
            throw new IllegalArgumentException("Укажите рабочие часы комнаты");
        }
        if (dayStart < 0 || dayStart > 24 || dayEnd < 0 || dayEnd > 24) {
            throw new IllegalArgumentException("Рабочие часы должны быть в промежутке от 0 до 24");
        }
        if (dayEnd <= dayStart) {
            throw new IllegalArgumentException("Конец рабочего дня должен быть позже начала");
        }
    }

    private LocalDateTime atHour(LocalDate date, int hour) {
        return hour == 24 ? date.plusDays(1).atStartOfDay() : date.atTime(hour, 0);
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
