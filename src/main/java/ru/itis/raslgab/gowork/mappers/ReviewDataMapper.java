package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.forms.reviews.ReviewForm;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Review;
import ru.itis.raslgab.gowork.models.User;

@Component
public class ReviewDataMapper {

    public Review mapCreateFormToModel(ReviewForm form, Organization organization, User author) {
        return Review.builder()
                .organization(organization)
                .author(author)
                .rating(form.getRating())
                .text(form.getText().trim())
                .build();
    }

    public Review mapAdminFormToModel(AdminEntityForm form, Organization organization, User author) {
        return Review.builder()
                .organization(organization)
                .author(author)
                .rating(form.getRating())
                .text(required(form.getText()))
                .build();
    }

    public void updateFromForm(Review review, ReviewForm form) {
        review.setRating(form.getRating());
        review.setText(form.getText().trim());
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }
}
