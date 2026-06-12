package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.ReviewItemDto;
import ru.itis.raslgab.gowork.forms.ReviewForm;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Review;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.ReviewRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepo reviewRepo;
    private final BookingRepo bookingRepo;
    private final OrganizationRepo organizationRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewItemDto> getOrganizationReviews(Long organizationId, Long currentUserId) {
        return reviewRepo.findItemsByOrganizationId(organizationId, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateReview(Long organizationId, Long currentUserId) {
        return hasFinishedConfirmedBooking(organizationId, currentUserId);
    }

    @Override
    @Transactional
    public void createReview(Long organizationId, Long currentUserId, ReviewForm form) {
        if (!hasFinishedConfirmedBooking(organizationId, currentUserId)) {
            throw new AccessDeniedException("Отзыв можно оставить только после завершенной подтвержденной брони");
        }
        Organization organization = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
        User author = userRepo.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        reviewRepo.save(Review.builder()
                .organization(organization)
                .author(author)
                .rating(form.getRating())
                .text(form.getText().trim())
                .build());
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, Long currentUserId, ReviewForm form) {
        Review review = findReview(reviewId);
        checkAuthor(review, currentUserId);
        review.setRating(form.getRating());
        review.setText(form.getText().trim());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = findReview(reviewId);
        checkAuthor(review, currentUserId);
        reviewRepo.delete(review);
    }

    private boolean hasFinishedConfirmedBooking(Long organizationId, Long currentUserId) {
        return bookingRepo.existsByRenterIdAndRoomOrganizationIdAndStatusAndTimeFinishBefore(
                currentUserId,
                organizationId,
                BookingStatus.CONFIRMED,
                LocalDateTime.now()
        );
    }

    private Review findReview(Long reviewId) {
        return reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }

    private void checkAuthor(Review review, Long currentUserId) {
        if (review.getAuthor() == null || !review.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Можно редактировать и удалять только свои отзывы");
        }
    }
}
