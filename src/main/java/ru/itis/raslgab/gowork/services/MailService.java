package ru.itis.raslgab.gowork.services;

public interface MailService {
    void sendEmailForConfirm(String email, String code);
}
