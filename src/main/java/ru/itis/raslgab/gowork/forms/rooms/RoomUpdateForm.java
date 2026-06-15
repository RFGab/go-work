package ru.itis.raslgab.gowork.forms.rooms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateForm {
    @NotBlank(message = "Название обязательно")
    @Size(max = 30, message = "Название должно быть не длиннее 30 символов")
    private String name;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    @NotNull(message = "Укажите вместимость")
    @Positive(message = "Вместимость должна быть больше 0")
    private Integer peopleCapacity;

    @NotNull(message = "Укажите цену")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal pricePerHour;

    @NotNull(message = "Укажите начало рабочего дня")
    @Min(value = 0, message = "Начало рабочего дня должно быть от 0 до 24")
    @Max(value = 24, message = "Начало рабочего дня должно быть от 0 до 24")
    private Integer dayStart;

    @NotNull(message = "Укажите конец рабочего дня")
    @Min(value = 0, message = "Конец рабочего дня должен быть от 0 до 24")
    @Max(value = 24, message = "Конец рабочего дня должен быть от 0 до 24")
    private Integer dayEnd;

    private List<Long> optionIds;
}
