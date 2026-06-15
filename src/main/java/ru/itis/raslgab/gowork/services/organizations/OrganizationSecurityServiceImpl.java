package ru.itis.raslgab.gowork.services.organizations;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;

@Service("organizationSecurityService")
@RequiredArgsConstructor
public class OrganizationSecurityServiceImpl implements OrganizationSecurityService {
    private final OrganizationRepo organizationRepo;

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(Long organizationId, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }
        Organization organization = findOrganization(organizationId);
        return organization.getOwner() != null && organization.getOwner().getId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManage(Long organizationId, Authentication authentication) {
        return isAdmin(authentication) || isOwner(organizationId, authentication);
    }

    private Organization findOrganization(Long organizationId) {
        return organizationRepo.findDetailsById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }
        return userDetails.getUserId();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
