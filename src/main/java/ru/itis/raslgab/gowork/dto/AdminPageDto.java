package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPageDto {
    private String entity;
    private String title;
    private boolean canCreate;
    private List<AdminFieldDto> fields;
    private Page<AdminRowDto> rows;
}
