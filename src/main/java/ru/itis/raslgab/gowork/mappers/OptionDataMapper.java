package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.models.Option;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;

@Component
public class OptionDataMapper {

    public Option mapAdminFormToModel(AdminEntityForm form) {
        return Option.builder()
                .name(required(form.getName()))
                .category(form.getCategory() == null ? OptionCategory.OFFICE_TOOLS : form.getCategory())
                .build();
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }
}
