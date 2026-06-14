package ru.itis.raslgab.gowork.services;

import org.springframework.security.core.Authentication;

public interface ReviewSecurityService {
    boolean canCreate(Long organizationId, Authentication authentication);

    boolean isAuthor(Long reviewId, Authentication authentication);
}
