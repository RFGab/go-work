package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.ReviewItemDto;
import ru.itis.raslgab.gowork.forms.ReviewForm;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Review;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.ReviewRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepo reviewRepo;
    private final OrganizationRepo organizationRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewItemDto> getOrganizationReviews(Long organizationId, Long currentUserId) {
        return reviewRepo.findItemsByOrganizationId(organizationId, currentUserId);
    }

    @Override
    @Transactional
    public void createReview(Long organizationId, Long currentUserId, ReviewForm form) {
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
    public void updateReview(Long reviewId, ReviewForm form) {
        Review review = findReview(reviewId);
        review.setRating(form.getRating());
        review.setText(form.getText().trim());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = findReview(reviewId);
        reviewRepo.delete(review);
    }

    private Review findReview(Long reviewId) {
        return reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }
}
