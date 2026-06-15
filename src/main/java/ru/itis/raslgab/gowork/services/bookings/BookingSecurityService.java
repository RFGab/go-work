package ru.itis.raslgab.gowork.services.bookings;

import org.springframework.security.core.Authentication;

public interface BookingSecurityService {
    boolean isRenter(Long bookingId, Authentication authentication);
}
