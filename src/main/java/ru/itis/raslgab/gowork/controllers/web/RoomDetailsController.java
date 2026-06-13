package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.itis.raslgab.gowork.forms.BookingCreateForm;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.BookingService;
import ru.itis.raslgab.gowork.services.RoomService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomDetailsController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final UserActionLogService userActionLogService;

    @GetMapping("/{roomId}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       @PathVariable Long roomId,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                       Model model) {
        LocalDate selectedDate = bookingDate == null ? LocalDate.now() : bookingDate;
        addRoomPageAttributes(userDetails, roomId, selectedDate, model);
        if (!model.containsAttribute("bookingForm")) {
            model.addAttribute("bookingForm", BookingCreateForm.builder()
                    .bookingDate(selectedDate)
                    .build());
        }
        userActionLogService.log(userDetails.getUserId(), "ROOM_OPEN", "roomId=" + roomId);
        return "rooms/show";
    }

    @PostMapping("/{roomId}/bookings")
    public String createBooking(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long roomId,
                                @Valid @ModelAttribute("bookingForm") BookingCreateForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        LocalDate selectedDate = form.getBookingDate() == null ? LocalDate.now() : form.getBookingDate();

        if (bindingResult.hasErrors()) {
            addRoomPageAttributes(userDetails, roomId, selectedDate, model);
            userActionLogService.log(userId, "BOOKING_CREATE_FAILED", "roomId=" + roomId + ", validation errors");
            return "rooms/show";
        }

        try {
            Long bookingId = bookingService.createBookingRequest(roomId, userId, form);
            userActionLogService.log(userId, "BOOKING_CREATE_SUCCESS", "roomId=" + roomId + ", bookingId=" + bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка на бронь создана");
            return "redirect:/rooms/" + roomId + "?bookingDate=" + selectedDate;
        } catch (IllegalArgumentException e) {
            bindingResult.reject("booking.invalid", e.getMessage());
            addRoomPageAttributes(userDetails, roomId, selectedDate, model);
            userActionLogService.log(userId, "BOOKING_CREATE_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            return "rooms/show";
        }
    }

    @PostMapping("/{roomId}/images")
    public String addImages(@AuthenticationPrincipal UserDetailsImpl userDetails,
                            @PathVariable Long roomId,
                            @RequestParam("images") List<MultipartFile> images,
                            RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        try {
            roomService.addRoomImages(roomId, userId, userDetails.getUser().getRole(), images);
            userActionLogService.log(userId, "ROOM_IMAGES_UPLOAD_SUCCESS", "roomId=" + roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Фото комнаты добавлены");
        } catch (IllegalArgumentException e) {
            userActionLogService.log(userId, "ROOM_IMAGES_UPLOAD_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            redirectAttributes.addFlashAttribute("imageError", e.getMessage());
        }
        return "redirect:/rooms/" + roomId;
    }

    private void addRoomPageAttributes(UserDetailsImpl userDetails, Long roomId, LocalDate selectedDate, Model model) {
        model.addAttribute("room", roomService.getRoomDetails(roomId));
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("hourSlots", roomService.getHourSlots(roomId, selectedDate));
        model.addAttribute("options", roomService.getOptions(roomId));
        model.addAttribute("similarRooms", roomService.getSimilarRooms(roomId));
        model.addAttribute("canManageRoom", roomService.canManageRoom(roomId, userDetails.getUserId(), userDetails.getUser().getRole()));
        model.addAttribute("roomStatuses", RoomStatus.values());
    }
}
