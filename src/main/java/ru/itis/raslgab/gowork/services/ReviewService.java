package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.ReviewItemDto;
import ru.itis.raslgab.gowork.forms.ReviewForm;

import java.util.List;

public interface ReviewService {
    List<ReviewItemDto> getOrganizationReviews(Long organizationId, Long currentUserId);

    boolean canCreateReview(Long organizationId, Long currentUserId);

    void createReview(Long organizationId, Long currentUserId, ReviewForm form);

    void updateReview(Long reviewId, Long currentUserId, ReviewForm form);

    void deleteReview(Long reviewId, Long currentUserId);
}
