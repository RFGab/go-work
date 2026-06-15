package ru.itis.raslgab.gowork.forms.organizations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCatalogFilterForm {
    private Long cityId;
    private String name;
    private Integer page;
    private Integer size;
}
