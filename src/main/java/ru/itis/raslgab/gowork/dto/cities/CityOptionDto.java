package ru.itis.raslgab.gowork.dto.cities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityOptionDto {
    private Long id;
    private String name;
}
