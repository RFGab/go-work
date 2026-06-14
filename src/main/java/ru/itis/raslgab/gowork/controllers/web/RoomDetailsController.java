package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import ru.itis.raslgab.gowork.forms.RoomUpdateForm;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.BookingService;
import ru.itis.raslgab.gowork.services.RoomService;
import ru.itis.raslgab.gowork.services.RoomSecurityService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomDetailsController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final RoomSecurityService roomSecurityService;
    private final UserActionLogService userActionLogService;

    @GetMapping("/{roomId}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       Authentication authentication,
                       @PathVariable Long roomId,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                       Model model) {
        LocalDate selectedDate = bookingDate == null ? currentRoomDate(roomId) : bookingDate;
        addRoomPageAttributes(authentication, roomId, selectedDate, model);
        if (!model.containsAttribute("bookingForm")) {
            model.addAttribute("bookingForm", BookingCreateForm.builder()
                    .bookingDate(selectedDate)
                    .build());
        }
        logIfAuthenticated(userDetails, "ROOM_OPEN", "roomId=" + roomId);
        return "rooms/show";
    }

    @PostMapping("/{roomId}/bookings")
    @PreAuthorize("isAuthenticated()")
    public String createBooking(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                Authentication authentication,
                                @PathVariable Long roomId,
                                @Valid @ModelAttribute("bookingForm") BookingCreateForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        LocalDate selectedDate = form.getBookingDate() == null ? LocalDate.now() : form.getBookingDate();

        if (bindingResult.hasErrors()) {
            addRoomPageAttributes(authentication, roomId, selectedDate, model);
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
            addRoomPageAttributes(authentication, roomId, selectedDate, model);
            userActionLogService.log(userId, "BOOKING_CREATE_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            return "rooms/show";
        }
    }

    @PostMapping("/{roomId}")
    @PreAuthorize("@roomSecurityService.canManage(#roomId, authentication)")
    public String updateRoom(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             Authentication authentication,
                             @PathVariable Long roomId,
                             @Valid @ModelAttribute("roomForm") RoomUpdateForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        LocalDate selectedDate = currentRoomDate(roomId);

        if (bindingResult.hasErrors()) {
            addRoomPageAttributes(authentication, roomId, selectedDate, model);
            userActionLogService.log(userId, "ROOM_UPDATE_FAILED", "roomId=" + roomId + ", validation errors");
            return "rooms/show";
        }

        try {
            roomService.updateRoom(roomId, form);
            userActionLogService.log(userId, "ROOM_UPDATE_SUCCESS", "roomId=" + roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Комната обновлена");
            return "redirect:/rooms/" + roomId;
        } catch (IllegalArgumentException e) {
            bindingResult.reject("room.invalid", e.getMessage());
            addRoomPageAttributes(authentication, roomId, selectedDate, model);
            userActionLogService.log(userId, "ROOM_UPDATE_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            return "rooms/show";
        }
    }

    @PostMapping("/{roomId}/images")
    @PreAuthorize("@roomSecurityService.canManage(#roomId, authentication)")
    public String addImages(@AuthenticationPrincipal UserDetailsImpl userDetails,
                            @PathVariable Long roomId,
                            @RequestParam("images") List<MultipartFile> images,
                            RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        try {
            roomService.addRoomImages(roomId, images);
            userActionLogService.log(userId, "ROOM_IMAGES_UPLOAD_SUCCESS", "roomId=" + roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Фото комнаты добавлены");
        } catch (IllegalArgumentException e) {
            userActionLogService.log(userId, "ROOM_IMAGES_UPLOAD_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            redirectAttributes.addFlashAttribute("imageError", e.getMessage());
        }
        return "redirect:/rooms/" + roomId;
    }

    private void addRoomPageAttributes(Authentication authentication, Long roomId, LocalDate selectedDate, Model model) {
        var room = roomService.getRoomDetails(roomId);
        model.addAttribute("room", room);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("hourSlots", roomService.getHourSlots(roomId, selectedDate));
        model.addAttribute("options", roomService.getOptions(roomId));
        model.addAttribute("similarRooms", roomService.getSimilarRooms(roomId));
        boolean canManageRoom = roomSecurityService.canManage(roomId, authentication);
        model.addAttribute("canManageRoom", canManageRoom);
        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("allOptions", roomService.getAllOptions());
        if (canManageRoom && !model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", roomService.getUpdateForm(roomId));
        }
    }

    private void logIfAuthenticated(UserDetailsImpl userDetails, String action, String details) {
        if (userDetails != null) {
            userActionLogService.log(userDetails.getUserId(), action, details);
        }
    }

    private LocalDate currentRoomDate(Long roomId) {
        Integer utc = roomService.getRoomDetails(roomId).getCityUtc();
        int offset = utc == null ? 3 : Math.max(-12, Math.min(14, utc));
        return LocalDate.now(ZoneOffset.ofHours(offset));
    }
}
