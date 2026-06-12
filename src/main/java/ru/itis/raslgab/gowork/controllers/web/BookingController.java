package ru.itis.raslgab.gowork.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.BookingService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final UserActionLogService userActionLogService;

    @GetMapping("/my")
    public String myBookings(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Long userId = userDetails.getUserId();
        model.addAttribute("bookings", bookingService.getUserBookings(userId));
        userActionLogService.log(userId, "BOOKINGS_MY_OPEN", "User opened own bookings");
        return "bookings/my";
    }
}
