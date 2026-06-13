package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationCatalogFilterForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.City;
import ru.itis.raslgab.gowork.models.FileInfo;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.repositories.FileInfoRepo;
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
    private final FileInfoRepo fileInfoRepo;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationCatalogItemDto> getCatalog(Long cityId) {
        return organizationRepo.findCatalogItems(cityId, OrganizationStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationCatalogItemDto> getCatalog(OrganizationCatalogFilterForm filter) {
        int page = filter.getPage() == null || filter.getPage() < 0 ? 0 : filter.getPage();
        int size = filter.getSize() == null || filter.getSize() < 1 ? 9 : Math.min(filter.getSize(), 30);
        String namePattern = toLikePattern(filter.getName());
        PageRequest pageRequest = PageRequest.of(page, size);
        if (namePattern == null) {
            return organizationRepo.findCatalogItemsPage(
                    filter.getCityId(),
                    OrganizationStatus.ACTIVE,
                    pageRequest
            );
        }
        return organizationRepo.findCatalogItemsPageByName(
                filter.getCityId(),
                namePattern,
                OrganizationStatus.ACTIVE,
                pageRequest
        );
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
                .orElseThrow(() -> new IllegalStateException("Текущий пользователь не найден"));
        City city = cityRepo.findById(form.getCityId())
                .orElseThrow(() -> new IllegalArgumentException("Город не найден"));

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

        return OrganizationDetailsDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .cityName(organization.getCity() == null ? "Город не указан" : organization.getCity().getName())
                .yandexMapLink(organization.getYandexMapLink())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .logoFileName(organization.getLogo() == null ? null : organization.getLogo().getStorageFileName())
                .ownerFullName(organization.getOwner().getFirstName() + " " + organization.getOwner().getLastName())
                .ownerEmail(organization.getOwner().getEmail())
                .status(organization.getStatus())
                .owner(owner)
                .admin(admin)
                .build();
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
    public void updateLogo(Long organizationId, Long currentUserId, RoleEnum currentUserRole, MultipartFile logo) {
        Organization organization = findOrganization(organizationId);
        checkManager(organization, currentUserId, currentUserRole);
        validateImage(logo, "Выберите файл логотипа");

        String fileName = fileStorageService.saveFile(logo);
        FileInfo fileInfo = fileInfoRepo.findByStorageFileName(fileName);
        organization.setLogo(fileInfo);
        organizationRepo.save(organization);
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
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
    }

    private void checkOwner(Organization organization, Long userId) {
        if (!isOwner(organization, userId)) {
            throw new AccessDeniedException("Редактировать организацию может только владелец");
        }
    }

    private void checkManager(Organization organization, Long userId, RoleEnum role) {
        if (!isOwner(organization, userId) && role != RoleEnum.ADMIN) {
            throw new AccessDeniedException("Управлять организацией может только владелец или администратор");
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

    private String toLikePattern(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : "%" + normalized.toLowerCase() + "%";
    }

    private void validateImage(MultipartFile file, String emptyMessage) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загрузить только изображение");
        }
    }
}
