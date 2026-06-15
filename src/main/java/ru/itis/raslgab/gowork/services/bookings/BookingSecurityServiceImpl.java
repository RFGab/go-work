package ru.itis.raslgab.gowork.services.bookings;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;

@Service("bookingSecurityService")
@RequiredArgsConstructor
public class BookingSecurityServiceImpl implements BookingSecurityService {
    private final BookingRepo bookingRepo;

    @Override
    @Transactional(readOnly = true)
    public boolean isRenter(Long bookingId, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }
        Booking booking = bookingRepo.findDetailsById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на бронь не найдена"));
        return booking.getRenter() != null && booking.getRenter().getId().equals(userId);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }
        return userDetails.getUserId();
    }
}
