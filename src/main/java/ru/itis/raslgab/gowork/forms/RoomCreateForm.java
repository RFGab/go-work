package ru.itis.raslgab.gowork.forms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateForm {
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

    @NotNull(message = "Выберите статус")
    private RoomStatus status;

    private List<Long> optionIds;
}
