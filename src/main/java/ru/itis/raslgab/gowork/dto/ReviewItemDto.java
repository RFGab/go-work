package ru.itis.raslgab.gowork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDto {
    private Long id;
    private Long authorId;
    private String authorFullName;
    private Integer rating;
    private String text;
    private LocalDateTime createdAt;
    private boolean own;
}
