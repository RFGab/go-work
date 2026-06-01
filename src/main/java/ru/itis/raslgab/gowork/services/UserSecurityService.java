package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.forms.UserRegistrationForm;

public interface UserSecurityService {
    Long register(UserRegistrationForm form);

    String generateEmailConfirmationCode(String email);

    boolean checkEmailConfirmationCode(String email, String code);
}
