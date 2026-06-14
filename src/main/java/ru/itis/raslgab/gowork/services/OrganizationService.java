package ru.itis.raslgab.gowork.services;

import org.springframework.data.domain.Page;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationCatalogFilterForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrganizationService {
    List<OrganizationCatalogItemDto> getCatalog(Long cityId);

    Page<OrganizationCatalogItemDto> getCatalog(OrganizationCatalogFilterForm filter);

    List<CityOptionDto> getCityOptions();

    Long createOrganization(Long ownerId, OrganizationCreateForm form);

    OrganizationDetailsDto getOrganization(Long organizationId, Long currentUserId, RoleEnum currentUserRole);

    OrganizationUpdateForm getUpdateForm(Long organizationId);

    List<RoomCatalogItemDto> getRooms(Long organizationId);

    void updateOrganization(Long organizationId, OrganizationUpdateForm form);

    void updateStatus(Long organizationId, OrganizationStatus status);

    void updateLogo(Long organizationId, MultipartFile logo);

    void deleteOrganization(Long organizationId);
}
