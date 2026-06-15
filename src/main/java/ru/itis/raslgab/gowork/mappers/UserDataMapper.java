package ru.itis.raslgab.gowork.mappers;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.dto.users.UserProfileDto;
import ru.itis.raslgab.gowork.forms.users.UserRegistrationForm;
import ru.itis.raslgab.gowork.forms.users.UserProfileForm;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;

@Component
@RequiredArgsConstructor
public class UserDataMapper {
    private final PasswordEncoder passwordEncoder;

    public User mapToModel(UserRegistrationForm form) {
        return User.builder()
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .phone(form.getPhone())
                .isBlocked(false)
                .isConfirmed(true)
                .role(RoleEnum.USER)
                .build();
    }

    public UserProfileDto mapToUserProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarFileName(user.getAvatarFileName())
                .build();
    }

    public UserProfileForm mapToProfileForm(User user) {
        return UserProfileForm.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarFileName(user.getAvatarFileName())
                .build();
    }
}
