package ru.itis.raslgab.gowork.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUpdateForm {
    private Long id;

    @NotBlank(message = "Название обязательно")
    @Size(max = 100, message = "Название должно быть не длиннее 100 символов")
    private String name;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    @NotBlank(message = "Город обязателен")
    private String cityName;

    private String yandexMapLink;

    @Email(message = "Некорректный email")
    private String contactEmail;

    private String contactPhone;
}
