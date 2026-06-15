package ru.itis.raslgab.gowork.services.users;

import ru.itis.raslgab.gowork.forms.users.UserRegistrationForm;

public interface UserSecurityService {
    Long register(UserRegistrationForm form);

    String generateEmailConfirmationCode(String email);

    boolean checkEmailConfirmationCode(String email, String code);
}
