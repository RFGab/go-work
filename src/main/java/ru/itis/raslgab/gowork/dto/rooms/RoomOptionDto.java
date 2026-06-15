package ru.itis.raslgab.gowork.dto.rooms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomOptionDto {
    private Long id;
    private String name;
    private OptionCategory category;
}
