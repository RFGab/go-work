package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.dto.bookings.BookingApprovalPageDto;
import ru.itis.raslgab.gowork.dto.bookings.BookingMailDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.forms.bookings.BookingCreateForm;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;

import java.time.LocalDateTime;

@Component
public class BookingDataMapper {

    public Booking mapCreateFormToModel(BookingCreateForm form,
                                        Room room,
                                        User renter,
                                        LocalDateTime timeStart,
                                        LocalDateTime timeFinish) {
        return Booking.builder()
                .room(room)
                .renter(renter)
                .timeStart(timeStart)
                .timeFinish(timeFinish)
                .numOfPeople(form.getNumOfPeople())
                .comment(trimToNull(form.getComment()))
                .status(BookingStatus.PENDING)
                .build();
    }

    public Booking mapAdminFormToModel(AdminEntityForm form, Room room, User renter) {
        return Booking.builder()
                .room(room)
                .renter(renter)
                .timeStart(form.getTimeStart())
                .timeFinish(form.getTimeFinish())
                .numOfPeople(form.getNumOfPeople())
                .comment(trimToNull(form.getComment()))
                .status(form.getBookingStatus() == null ? BookingStatus.PENDING : form.getBookingStatus())
                .build();
    }

    public BookingApprovalPageDto mapToApprovalPageDto(Booking booking, String action, String actionTitle) {
        return BookingApprovalPageDto.builder()
                .bookingId(booking.getId())
                .action(action)
                .actionTitle(actionTitle)
                .renterFullName(fullName(booking.getRenter()))
                .roomName(booking.getRoom().getName())
                .organizationName(booking.getRoom().getOrganization().getName())
                .timeStart(booking.getTimeStart())
                .timeFinish(booking.getTimeFinish())
                .build();
    }

    public BookingMailDto mapToMailDto(Booking booking) {
        User renter = booking.getRenter();
        User owner = booking.getRoom().getOrganization().getOwner();
        return BookingMailDto.builder()
                .bookingId(booking.getId())
                .renterFullName(fullName(renter))
                .renterEmail(renter.getEmail())
                .renterPhone(renter.getPhone())
                .renterProfilePhoto(renter.getAvatarFileName() == null ? "не указано" : renter.getAvatarFileName())
                .ownerEmail(owner.getEmail())
                .roomName(booking.getRoom().getName())
                .organizationName(booking.getRoom().getOrganization().getName())
                .timeStart(booking.getTimeStart())
                .timeFinish(booking.getTimeFinish())
                .numOfPeople(booking.getNumOfPeople())
                .comment(booking.getComment())
                .build();
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
