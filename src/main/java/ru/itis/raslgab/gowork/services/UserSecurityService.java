package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.forms.UserRegistrationForm;

public interface UserSecurityService {
    boolean register(UserRegistrationForm form);
    boolean activate(String code);

    void confirmMail(Long id);

    void sendMailToConfirm(Long id);
}
