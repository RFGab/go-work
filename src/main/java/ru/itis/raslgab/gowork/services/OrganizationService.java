package ru.itis.raslgab.gowork.services;

import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrganizationService {
    List<OrganizationCatalogItemDto> getCatalog(Long cityId);

    List<CityOptionDto> getCityOptions();

    Long createOrganization(Long ownerId, OrganizationCreateForm form);

    OrganizationDetailsDto getOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole);

    OrganizationUpdateForm getUpdateForm(Long organizationId, Long currentUserId);

    List<RoomCatalogItemDto> getRooms(Long organizationId);

    void updateOrganization(Long organizationId, Long ownerId, OrganizationUpdateForm form);

    void updateStatus(Long organizationId, Long currentUserId, RoleEnum currentUserRole, OrganizationStatus status);

    void updateLogo(Long organizationId, Long currentUserId, RoleEnum currentUserRole, MultipartFile logo);

    void deleteOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole);
}
