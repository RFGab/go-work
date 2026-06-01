package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.itis.raslgab.gowork.forms.UserProfileForm;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.ProfileService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final UserActionLogService userActionLogService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Long userId = userDetails.getUserId();
        userActionLogService.log(userId, "PROFILE_OPEN", "Profile page opened");
        addProfileModel(model, userId, profileService.getProfileForm(userId));
        return "profile";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @Valid @ModelAttribute("profileForm") UserProfileForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        form.setId(userId);

        if (profileService.isEmailTakenByAnotherUser(form.getEmail(), userId)) {
            bindingResult.rejectValue("email", "email.exists", "Пользователь с таким email уже существует");
            userActionLogService.log(userId, "PROFILE_UPDATE_FAILED", "Email already exists");
        }

        if (bindingResult.hasErrors()) {
            userActionLogService.log(userId, "PROFILE_UPDATE_FAILED", "Validation errors");
            addProfileModel(model, userId, form);
            return "profile";
        }

        profileService.updateProfile(userId, form);
        userActionLogService.log(userId, "PROFILE_UPDATE_SUCCESS", "Profile updated");
        redirectAttributes.addFlashAttribute("successMessage", "Профиль обновлен");
        return "redirect:/profile";
    }

    private void addProfileModel(Model model, Long userId, UserProfileForm form) {
        model.addAttribute("profileForm", form);
        model.addAttribute("organizations", profileService.getUserOrganizations(userId));
    }
}
