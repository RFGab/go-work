package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.BookingHourSlotDto;
import ru.itis.raslgab.gowork.dto.PopularRoomDto;
import ru.itis.raslgab.gowork.dto.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.SimilarRoomDto;
import ru.itis.raslgab.gowork.forms.RoomCreateForm;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    List<RoomStatus> getCreateStatuses();

    RoomDetailsDto getRoomDetails(Long roomId);

    List<BookingHourSlotDto> getHourSlots(Long roomId, LocalDate bookingDate);

    List<RoomOptionDto> getOptions(Long roomId);

    List<RoomOptionDto> getAllOptions();

    List<SimilarRoomDto> getSimilarRooms(Long roomId);

    List<PopularRoomDto> getPopularRooms();

    Long createRoom(Long organizationId, Long ownerId, RoomCreateForm form);

    boolean canManageRoom(Long roomId, Long currentUserId, RoleEnum currentUserRole);

    void addRoomImages(Long roomId, Long currentUserId, RoleEnum currentUserRole, List<MultipartFile> images);

    void updateStatus(Long roomId, Long currentUserId, RoleEnum currentUserRole, RoomStatus status);
}
