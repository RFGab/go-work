package ru.itis.raslgab.gowork.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingApprovalForm {
    @NotBlank(message = "Введите код подтверждения")
    @Pattern(regexp = "\\d{4}", message = "Код должен состоять из 4 цифр")
    private String code;
}
