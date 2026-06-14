package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;

@Service("roomSecurityService")
@RequiredArgsConstructor
public class RoomSecurityServiceImpl implements RoomSecurityService {
    private final RoomRepo roomRepo;

    @Override
    @Transactional(readOnly = true)
    public boolean canManage(Long roomId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }
        Room room = roomRepo.findByIdWithImages(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
        Organization organization = room.getOrganization();
        return organization != null
                && organization.getOwner() != null
                && organization.getOwner().getId().equals(userId);
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
