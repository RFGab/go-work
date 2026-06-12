package ru.itis.raslgab.gowork.forms;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCatalogFilterForm {
    private Long cityId;

    @Builder.Default
    private Boolean availableToday = false;

    @Min(value = 1, message = "Вместимость должна быть больше 0")
    private Integer minCapacity;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 9;
}
