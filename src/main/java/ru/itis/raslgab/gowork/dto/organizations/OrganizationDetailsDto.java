package ru.itis.raslgab.gowork.dto.organizations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDetailsDto {
    private Long id;
    private String name;
    private String description;
    private String cityName;
    private String yandexMapLink;
    private String contactEmail;
    private String contactPhone;
    private String logoFileName;
    private String ownerFullName;
    private String ownerEmail;
    private OrganizationStatus status;
    private boolean owner;
    private boolean admin;
}
