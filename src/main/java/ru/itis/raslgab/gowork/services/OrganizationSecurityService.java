package ru.itis.raslgab.gowork.services;

import org.springframework.security.core.Authentication;

public interface OrganizationSecurityService {
    boolean isOwner(Long organizationId, Authentication authentication);

    boolean canManage(Long organizationId, Authentication authentication);
}
