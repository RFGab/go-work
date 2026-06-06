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

    public boolean isAvailableTodaySelected() {
        return Boolean.TRUE.equals(availableToday);
    }

    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 9;
        }
        return Math.min(size, 30);
    }
}
