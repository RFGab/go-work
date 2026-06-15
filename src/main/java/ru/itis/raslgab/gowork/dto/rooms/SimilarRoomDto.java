package ru.itis.raslgab.gowork.dto.rooms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarRoomDto {
    private Long id;
    private String name;
    private String organizationName;
    private Integer peopleCapacity;
    private BigDecimal pricePerHour;
}
