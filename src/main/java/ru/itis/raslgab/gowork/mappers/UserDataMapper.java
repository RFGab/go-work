package ru.itis.raslgab.gowork.mappers;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.forms.UserRegistrationForm;
import ru.itis.raslgab.gowork.models.User;

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
                .build();
    }
}
