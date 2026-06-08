package ru.itis.raslgab.gowork.services;

import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.OrganizationProfileItemDto;
import ru.itis.raslgab.gowork.forms.UserProfileForm;

import java.util.List;

public interface ProfileService {
    UserProfileForm getProfileForm(Long userId);

    List<OrganizationProfileItemDto> getUserOrganizations(Long userId);

    boolean isEmailTakenByAnotherUser(String email, Long userId);

    void updateProfile(Long userId, UserProfileForm form);

    void updateAvatar(Long userId, MultipartFile avatar);
}
