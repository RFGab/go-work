package ru.itis.raslgab.gowork.services;

import org.springframework.security.core.Authentication;

public interface RoomSecurityService {
    boolean canManage(Long roomId, Authentication authentication);
}
