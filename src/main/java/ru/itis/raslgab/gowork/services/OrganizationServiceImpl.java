package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.City;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.repositories.CityRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepo organizationRepo;
    private final CityRepo cityRepo;
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationCatalogItemDto> getCatalog(Long cityId) {
        return organizationRepo.findCatalogItems(cityId, OrganizationStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityOptionDto> getCityOptions() {
        return cityRepo.findOptions();
    }

    @Override
    @Transactional
    public Long createOrganization(Long ownerId, OrganizationCreateForm form) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        City city = getOrCreateCity(form.getCityName());

        Organization organization = Organization.builder()
                .name(form.getName().trim())
                .description(trimToNull(form.getDescription()))
                .city(city)
                .yandexMapLink(trimToNull(form.getYandexMapLink()))
                .contactEmail(normalizeEmail(form.getContactEmail()))
                .contactPhone(trimToNull(form.getContactPhone()))
                .owner(owner)
                .status(OrganizationStatus.ACTIVE)
                .build();

        return organizationRepo.save(organization).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDetailsDto getOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole) {
        Organization organization = findOrganization(organizationId);
        boolean owner = isOwner(organization, currentUserId);
        boolean admin = currentUserRole == RoleEnum.ADMIN;

        return new OrganizationDetailsDto(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getCity() == null ? "Город не указан" : organization.getCity().getName(),
                organization.getYandexMapLink(),
                organization.getContactEmail(),
                organization.getContactPhone(),
                organization.getOwner().getFirstName() + " " + organization.getOwner().getLastName(),
                organization.getOwner().getEmail(),
                organization.getStatus(),
                owner,
                admin
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationUpdateForm getUpdateForm(Long organizationId, Long currentUserId) {
        Organization organization = findOrganization(organizationId);
        checkOwner(organization, currentUserId);

        return OrganizationUpdateForm.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .cityName(organization.getCity() == null ? null : organization.getCity().getName())
                .yandexMapLink(organization.getYandexMapLink())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCatalogItemDto> getRooms(Long organizationId) {
        return roomRepo.findCatalogItemsByOrganizationId(organizationId);
    }

    @Override
    @Transactional
    public void updateOrganization(Long organizationId, Long ownerId, OrganizationUpdateForm form) {
        Organization organization = findOrganization(organizationId);
        checkOwner(organization, ownerId);

        organization.setName(form.getName().trim());
        organization.setDescription(trimToNull(form.getDescription()));
        organization.setCity(getOrCreateCity(form.getCityName()));
        organization.setYandexMapLink(trimToNull(form.getYandexMapLink()));
        organization.setContactEmail(normalizeEmail(form.getContactEmail()));
        organization.setContactPhone(trimToNull(form.getContactPhone()));
    }

    @Override
    @Transactional
    public void updateStatus(Long organizationId, Long currentUserId, RoleEnum currentUserRole, OrganizationStatus status) {
        Organization organization = findOrganization(organizationId);
        checkManager(organization, currentUserId, currentUserRole);
        organization.setStatus(status);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole) {
        Organization organization = findOrganization(organizationId);
        checkManager(organization, currentUserId, currentUserRole);

        try {
            organizationRepo.delete(organization);
            organizationRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Нельзя удалить организацию, пока к ней привязаны комнаты, бронирования или отзывы", e);
        }
    }

    private Organization findOrganization(Long organizationId) {
        return organizationRepo.findDetailsById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
    }

    private void checkOwner(Organization organization, Long userId) {
        if (!isOwner(organization, userId)) {
            throw new AccessDeniedException("Only organization owner can edit organization info");
        }
    }

    private void checkManager(Organization organization, Long userId, RoleEnum role) {
        if (!isOwner(organization, userId) && role != RoleEnum.ADMIN) {
            throw new AccessDeniedException("Only organization owner or admin can manage organization");
        }
    }

    private boolean isOwner(Organization organization, Long userId) {
        return organization.getOwner() != null && organization.getOwner().getId().equals(userId);
    }

    private City getOrCreateCity(String cityName) {
        String normalizedCityName = cityName.trim();
        return cityRepo.findByNameIgnoreCase(normalizedCityName)
                .orElseGet(() -> cityRepo.save(City.builder()
                        .name(normalizedCityName)
                        .build()));
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
