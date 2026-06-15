package ru.itis.raslgab.gowork.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFieldDto {
    private String name;
    private String label;
    private String type;
    private List<AdminOptionDto> options;
}
