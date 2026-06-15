package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.models.City;

@Component
public class CityDataMapper {

    public City mapAdminFormToModel(AdminEntityForm form) {
        return City.builder()
                .name(required(form.getName()))
                .utc(form.getUtc() == null ? 3 : form.getUtc())
                .build();
    }

    public City mapNameToModel(String name) {
        return City.builder()
                .name(required(name))
                .utc(3)
                .build();
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }
}
