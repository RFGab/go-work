package ru.itis.raslgab.gowork.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.raslgab.gowork.dto.AdminActionResponseDto;
import ru.itis.raslgab.gowork.forms.ReviewForm;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.ReviewService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewApiController {
    private final ReviewService reviewService;

    @PostMapping("/organizations/{organizationId}/reviews")
    public AdminActionResponseDto create(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long organizationId,
                                         @Valid @ModelAttribute ReviewForm form,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return error(bindingResult);
        }
        try {
            reviewService.createReview(organizationId, userDetails.getUserId(), form);
            return success("Отзыв добавлен");
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/reviews/{reviewId}")
    public AdminActionResponseDto update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long reviewId,
                                         @Valid @ModelAttribute ReviewForm form,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return error(bindingResult);
        }
        try {
            reviewService.updateReview(reviewId, userDetails.getUserId(), form);
            return success("Отзыв обновлен");
        } catch (Exception e) {
            return error(e);
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public AdminActionResponseDto delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId, userDetails.getUserId());
            return success("Отзыв удален");
        } catch (Exception e) {
            return error(e);
        }
    }

    private AdminActionResponseDto success(String message) {
        return AdminActionResponseDto.builder()
                .success(true)
                .message(message)
                .build();
    }

    private AdminActionResponseDto error(BindingResult bindingResult) {
        String message = bindingResult.getFieldErrors().isEmpty()
                ? "Проверьте данные отзыва"
                : bindingResult.getFieldErrors().get(0).getDefaultMessage();
        return AdminActionResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    private AdminActionResponseDto error(Exception e) {
        return AdminActionResponseDto.builder()
                .success(false)
                .message(e.getMessage() == null ? "Ошибка выполнения операции" : e.getMessage())
                .build();
    }
}
