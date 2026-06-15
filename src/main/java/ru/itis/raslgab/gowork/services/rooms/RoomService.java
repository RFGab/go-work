package ru.itis.raslgab.gowork.services.rooms;

import ru.itis.raslgab.gowork.dto.bookings.BookingHourSlotDto;
import ru.itis.raslgab.gowork.dto.rooms.PopularRoomDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.rooms.SimilarRoomDto;
import ru.itis.raslgab.gowork.forms.rooms.RoomCreateForm;
import ru.itis.raslgab.gowork.forms.rooms.RoomUpdateForm;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
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

    Long createRoom(Long organizationId, RoomCreateForm form);

    RoomUpdateForm getUpdateForm(Long roomId);

    void updateRoom(Long roomId, RoomUpdateForm form);

    void addRoomImages(Long roomId, List<MultipartFile> images);

    void deleteRoomImage(Long roomId, String fileName);

    void updateStatus(Long roomId, RoomStatus status);
}
