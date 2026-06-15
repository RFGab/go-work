package ru.itis.raslgab.gowork.services.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserActionLogServiceImpl implements UserActionLogService {
    private static final Logger USER_ACTIONS_LOG = LoggerFactory.getLogger("USER_ACTIONS");

    @Override
    public void log(Long userId, String action, String details) {
        USER_ACTIONS_LOG.info("userId={} action={} details={}", userId, action, details);
    }

    @Override
    public void logAnonymous(String action, String details) {
        USER_ACTIONS_LOG.info("userId=? action={} details={}", action, details);
    }
}
