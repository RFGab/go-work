package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.BookingIntervalDto;
import ru.itis.raslgab.gowork.dto.BookingHourSlotDto;
import ru.itis.raslgab.gowork.dto.PopularRoomDto;
import ru.itis.raslgab.gowork.dto.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.SimilarRoomDto;
import ru.itis.raslgab.gowork.forms.RoomCreateForm;
import ru.itis.raslgab.gowork.models.FileInfo;
import ru.itis.raslgab.gowork.models.Option;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.FileInfoRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.OptionRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final BigDecimal DAY_HOURS = BigDecimal.valueOf(24);
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

    @Override
    public List<RoomStatus> getCreateStatuses() {
        return CREATE_STATUSES;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDetailsDto getRoomDetails(Long roomId) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<BookingIntervalDto> intervals = bookingRepo.findBlockingIntervals(
                List.of(roomId),
                BLOCKING_STATUSES,
                dayStart,
                dayEnd
        );
        room.setAvailableHoursToday(calculateAvailableHours(room.getStatus(), intervals, dayStart, dayEnd));
        room.setImageFileNames(getRoomImageFileNames(roomId));
        return room;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHourSlotDto> getHourSlots(Long roomId, LocalDate bookingDate) {
        RoomDetailsDto room = roomRepo.findDetailsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
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
    public Long createRoom(Long organizationId, Long ownerId, RoomCreateForm form) {
        Organization organization = organizationRepo.findDetailsById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));

        if (organization.getOwner() == null || !organization.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Создавать комнаты может только владелец организации");
        }

        Set<Option> options = form.getOptionIds() == null || form.getOptionIds().isEmpty()
                ? Set.of()
                : new HashSet<>(optionRepo.findAllById(form.getOptionIds()));

        Room room = Room.builder()
                .name(form.getName().trim())
                .description(trimToNull(form.getDescription()))
                .peopleCapacity(form.getPeopleCapacity())
                .pricePerHour(form.getPricePerHour())
                .status(form.getStatus())
                .organization(organization)
                .options(options)
                .build();

        return roomRepo.save(room).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageRoom(Long roomId, Long currentUserId, RoleEnum currentUserRole) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        return isRoomManager(room, currentUserId, currentUserRole);
    }

    @Override
    @Transactional
    public void addRoomImages(Long roomId, Long currentUserId, RoleEnum currentUserRole, List<MultipartFile> images) {
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        checkRoomManager(room, currentUserId, currentUserRole);

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
    public void updateStatus(Long roomId, Long currentUserId, RoleEnum currentUserRole, RoomStatus status) {
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        checkRoomManager(room, currentUserId, currentUserRole);
        room.setStatus(status);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> getRoomImageFileNames(Long roomId) {
        return roomRepo.findByIdWithImages(roomId)
                .map(room -> room.getImages() == null ? List.<String>of() : room.getImages().stream()
                        .map(FileInfo::getStorageFileName)
                        .toList())
                .orElse(List.of());
    }

    private void checkRoomManager(Room room, Long currentUserId, RoleEnum currentUserRole) {
        if (!isRoomManager(room, currentUserId, currentUserRole)) {
            throw new AccessDeniedException("Управлять фото комнаты может только владелец организации или администратор");
        }
    }

    private boolean isRoomManager(Room room, Long currentUserId, RoleEnum currentUserRole) {
        if (currentUserRole == RoleEnum.ADMIN) {
            return true;
        }
        Organization organization = room.getOrganization();
        return organization != null
                && organization.getOwner() != null
                && organization.getOwner().getId().equals(currentUserId);
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
