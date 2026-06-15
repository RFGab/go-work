package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.dto.bookings.BookingHourSlotDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.forms.rooms.RoomCreateForm;
import ru.itis.raslgab.gowork.forms.rooms.RoomUpdateForm;
import ru.itis.raslgab.gowork.models.Option;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.util.List;
import java.util.Set;

@Component
public class RoomDataMapper {

    public BookingHourSlotDto mapToHourSlot(int hour, boolean available) {
        return BookingHourSlotDto.builder()
                .hour(hour)
                .label(String.format("%02d:00", hour))
                .available(available)
                .build();
    }

    public Room mapCreateFormToModel(RoomCreateForm form, Organization organization, Set<Option> options) {
        return Room.builder()
                .name(form.getName().trim())
                .description(trimToNull(form.getDescription()))
                .peopleCapacity(form.getPeopleCapacity())
                .pricePerHour(form.getPricePerHour())
                .status(form.getStatus())
                .dayStart(form.getDayStart())
                .dayEnd(form.getDayEnd())
                .organization(organization)
                .options(options)
                .build();
    }

    public Room mapAdminFormToModel(AdminEntityForm form, Organization organization, int dayStart, int dayEnd) {
        return Room.builder()
                .name(required(form.getName()))
                .description(trimToNull(form.getDescription()))
                .peopleCapacity(form.getPeopleCapacity())
                .pricePerHour(form.getPricePerHour())
                .organization(organization)
                .dayStart(dayStart)
                .dayEnd(dayEnd)
                .status(form.getRoomStatus() == null ? RoomStatus.AVAILABLE : form.getRoomStatus())
                .build();
    }

    public RoomUpdateForm mapToUpdateForm(Room room) {
        return RoomUpdateForm.builder()
                .name(room.getName())
                .description(room.getDescription())
                .peopleCapacity(room.getPeopleCapacity())
                .pricePerHour(room.getPricePerHour())
                .dayStart(room.getDayStart())
                .dayEnd(room.getDayEnd())
                .optionIds(room.getOptions() == null
                        ? List.of()
                        : room.getOptions().stream()
                        .map(Option::getId)
                        .toList())
                .build();
    }

    public void updateFromForm(Room room, RoomUpdateForm form, Set<Option> options) {
        room.setName(form.getName().trim());
        room.setDescription(trimToNull(form.getDescription()));
        room.setPeopleCapacity(form.getPeopleCapacity());
        room.setPricePerHour(form.getPricePerHour());
        room.setDayStart(form.getDayStart());
        room.setDayEnd(form.getDayEnd());
        room.setOptions(options);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }
}
