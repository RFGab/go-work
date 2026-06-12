package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCatalogItemDto {
    private Long id;
    private String name;
    private String description;
    private String cityName;
    private String contactEmail;
    private String contactPhone;
    private String logoFileName;
    private Long roomCount;
}
