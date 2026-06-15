package ru.itis.raslgab.gowork.dto.organizations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationProfileItemDto {
    private Long id;
    private String name;
    private String cityName;
    private Long roomCount;
}
