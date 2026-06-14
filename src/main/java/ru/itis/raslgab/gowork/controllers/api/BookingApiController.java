package ru.itis.raslgab.gowork.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.raslgab.gowork.dto.AdminActionResponseDto;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.BookingService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {
    private final BookingService bookingService;
    private final UserActionLogService userActionLogService;

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("@bookingSecurityService.isRenter(#bookingId, authentication)")
    public AdminActionResponseDto cancel(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            userActionLogService.log(userDetails.getUserId(), "BOOKING_CANCEL_SUCCESS", "bookingId=" + bookingId);
            return response(true, "Бронь отменена");
        } catch (Exception e) {
            userActionLogService.log(userDetails.getUserId(), "BOOKING_CANCEL_FAILED", "bookingId=" + bookingId + ", " + e.getMessage());
            return response(false, e.getMessage() == null ? "Не удалось отменить бронь" : e.getMessage());
        }
    }

    private AdminActionResponseDto response(boolean success, String message) {
        return AdminActionResponseDto.builder()
                .success(success)
                .message(message)
                .build();
    }
}
