package ru.itis.raslgab.gowork.services.organizations;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.dto.cities.CityOptionDto;
import ru.itis.raslgab.gowork.dto.organizations.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.organizations.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.organizations.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.organizations.OrganizationCatalogFilterForm;
import ru.itis.raslgab.gowork.forms.organizations.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.mappers.CityDataMapper;
import ru.itis.raslgab.gowork.mappers.OrganizationDataMapper;
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
import ru.itis.raslgab.gowork.services.files.FileStorageService;

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
    private final OrganizationDataMapper organizationDataMapper;
    private final CityDataMapper cityDataMapper;

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

        Organization organization = organizationDataMapper.mapCreateFormToModel(form, city, owner);

        return organizationRepo.save(organization).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDetailsDto getOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole) {
        Organization organization = findOrganization(organizationId);
        boolean owner = isOwner(organization, currentUserId);
        boolean admin = currentUserRole == RoleEnum.ADMIN;

        return organizationDataMapper.mapToDetailsDto(organization, owner, admin);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationUpdateForm getUpdateForm(Long organizationId) {
        Organization organization = findOrganization(organizationId);

        return organizationDataMapper.mapToUpdateForm(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCatalogItemDto> getRooms(Long organizationId) {
        return roomRepo.findCatalogItemsByOrganizationId(organizationId).stream()
                .peek(room -> {
                    List<String> fileNames = roomRepo.findImageFileNamesOrderById(room.getId(), PageRequest.of(0, 1));
                    room.setCoverImageFileName(fileNames.isEmpty() ? null : fileNames.get(0));
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateOrganization(Long organizationId, OrganizationUpdateForm form) {
        Organization organization = findOrganization(organizationId);

        organizationDataMapper.updateFromForm(organization, form, getOrCreateCity(form.getCityName()));
    }

    @Override
    @Transactional
    public void updateStatus(Long organizationId, OrganizationStatus status) {
        Organization organization = findOrganization(organizationId);
        organization.setStatus(status);
    }

    @Override
    @Transactional
    public void updateLogo(Long organizationId, MultipartFile logo) {
        Organization organization = findOrganization(organizationId);
        validateImage(logo, "Выберите файл логотипа");

        String fileName = fileStorageService.saveFile(logo);
        FileInfo fileInfo = fileInfoRepo.findByStorageFileName(fileName);
        organization.setLogo(fileInfo);
        organizationRepo.save(organization);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long organizationId) {
        Organization organization = findOrganization(organizationId);

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

    private boolean isOwner(Organization organization, Long userId) {
        return organization.getOwner() != null && organization.getOwner().getId().equals(userId);
    }

    private City getOrCreateCity(String cityName) {
        String normalizedCityName = cityName.trim();
        return cityRepo.findByNameIgnoreCase(normalizedCityName)
                .orElseGet(() -> cityRepo.save(cityDataMapper.mapNameToModel(normalizedCityName)));
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
