package ru.itis.raslgab.gowork.services.logging;

public interface UserActionLogService {
    void log(Long userId, String action, String details);

    void logAnonymous(String action, String details);
}
