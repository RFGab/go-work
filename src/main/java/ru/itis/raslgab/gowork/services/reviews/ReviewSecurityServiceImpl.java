package ru.itis.raslgab.gowork.services.reviews;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.models.Review;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.ReviewRepo;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;

import java.time.LocalDateTime;

@Service("reviewSecurityService")
@RequiredArgsConstructor
public class ReviewSecurityServiceImpl implements ReviewSecurityService {
    private final BookingRepo bookingRepo;
    private final ReviewRepo reviewRepo;

    @Override
    @Transactional(readOnly = true)
    public boolean canCreate(Long organizationId, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }
        return bookingRepo.existsByRenterIdAndRoomOrganizationIdAndStatusAndTimeFinishBefore(
                userId,
                organizationId,
                BookingStatus.CONFIRMED,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAuthor(Long reviewId, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        return review.getAuthor() != null && review.getAuthor().getId().equals(userId);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }
        return userDetails.getUserId();
    }
}
