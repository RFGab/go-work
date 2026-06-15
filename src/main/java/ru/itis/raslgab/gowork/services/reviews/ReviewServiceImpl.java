package ru.itis.raslgab.gowork.services.reviews;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.reviews.ReviewItemDto;
import ru.itis.raslgab.gowork.forms.reviews.ReviewForm;
import ru.itis.raslgab.gowork.mappers.ReviewDataMapper;
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
    private final ReviewDataMapper reviewDataMapper;

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

        reviewRepo.save(reviewDataMapper.mapCreateFormToModel(form, organization, author));
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, ReviewForm form) {
        Review review = findReview(reviewId);
        reviewDataMapper.updateFromForm(review, form);
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
