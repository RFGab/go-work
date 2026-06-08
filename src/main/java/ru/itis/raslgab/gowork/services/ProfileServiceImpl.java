package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.OrganizationProfileItemDto;
import ru.itis.raslgab.gowork.forms.UserProfileForm;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepo userRepo;
    private final OrganizationRepo organizationRepo;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileForm getProfileForm(Long userId) {
        User user = getUser(userId);
        return UserProfileForm.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarFileName(user.getAvatarFileName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationProfileItemDto> getUserOrganizations(Long userId) {
        return organizationRepo.findProfileItemsByOwnerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTakenByAnotherUser(String email, Long userId) {
        return userRepo.existsByEmailAndIdNot(normalizeEmail(email), userId);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserProfileForm form) {
        User user = getUser(userId);
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setPhone(form.getPhone().trim());
        userRepo.save(user);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Выберите файл аватарки");
        }
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("В качестве аватарки можно загрузить только изображение");
        }

        User user = getUser(userId);
        String fileName = fileStorageService.saveFile(avatar);
        user.setAvatarFileName(fileName);
        userRepo.save(user);
    }

    private User getUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Текущий пользователь не найден"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
