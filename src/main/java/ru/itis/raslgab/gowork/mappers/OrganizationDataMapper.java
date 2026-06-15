package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.dto.organizations.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.forms.organizations.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.organizations.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.City;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

@Component
public class OrganizationDataMapper {

    public Organization mapCreateFormToModel(OrganizationCreateForm form, City city, User owner) {
        return Organization.builder()
                .name(form.getName().trim())
                .description(trimToNull(form.getDescription()))
                .city(city)
                .yandexMapLink(trimToNull(form.getYandexMapLink()))
                .contactEmail(normalizeEmail(form.getContactEmail()))
                .contactPhone(trimToNull(form.getContactPhone()))
                .owner(owner)
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    public Organization mapAdminFormToModel(AdminEntityForm form, City city, User owner) {
        return Organization.builder()
                .name(required(form.getName()))
                .description(trimToNull(form.getDescription()))
                .city(city)
                .contactEmail(trimToNull(form.getContactEmail()))
                .contactPhone(trimToNull(form.getContactPhone()))
                .owner(owner)
                .status(form.getOrganizationStatus() == null ? OrganizationStatus.ACTIVE : form.getOrganizationStatus())
                .build();
    }

    public OrganizationDetailsDto mapToDetailsDto(Organization organization, boolean owner, boolean admin) {
        User organizationOwner = organization.getOwner();
        return OrganizationDetailsDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .cityName(organization.getCity() == null ? "Город не указан" : organization.getCity().getName())
                .yandexMapLink(organization.getYandexMapLink())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .logoFileName(organization.getLogo() == null ? null : organization.getLogo().getStorageFileName())
                .ownerFullName(organizationOwner == null ? null : organizationOwner.getFirstName() + " " + organizationOwner.getLastName())
                .ownerEmail(organizationOwner == null ? null : organizationOwner.getEmail())
                .status(organization.getStatus())
                .owner(owner)
                .admin(admin)
                .build();
    }

    public OrganizationUpdateForm mapToUpdateForm(Organization organization) {
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

    public void updateFromForm(Organization organization, OrganizationUpdateForm form, City city) {
        organization.setName(form.getName().trim());
        organization.setDescription(trimToNull(form.getDescription()));
        organization.setCity(city);
        organization.setYandexMapLink(trimToNull(form.getYandexMapLink()));
        organization.setContactEmail(normalizeEmail(form.getContactEmail()));
        organization.setContactPhone(trimToNull(form.getContactPhone()));
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }
}
