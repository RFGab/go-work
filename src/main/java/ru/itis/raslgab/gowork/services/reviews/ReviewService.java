package ru.itis.raslgab.gowork.services.reviews;

import ru.itis.raslgab.gowork.dto.reviews.ReviewItemDto;
import ru.itis.raslgab.gowork.forms.reviews.ReviewForm;

import java.util.List;

public interface ReviewService {
    List<ReviewItemDto> getOrganizationReviews(Long organizationId, Long currentUserId);

    void createReview(Long organizationId, Long currentUserId, ReviewForm form);

    void updateReview(Long reviewId, ReviewForm form);

    void deleteReview(Long reviewId);
}
