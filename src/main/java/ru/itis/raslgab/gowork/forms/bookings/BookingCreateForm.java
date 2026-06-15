package ru.itis.raslgab.gowork.forms.bookings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateForm {
    @NotNull(message = "Выберите день")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDate;

    @NotNull(message = "Выберите начало")
    @Min(value = 0, message = "Начало должно быть от 0 до 23")
    @Max(value = 23, message = "Начало должно быть от 0 до 23")
    private Integer startHour;

    @NotNull(message = "Выберите конец")
    @Min(value = 1, message = "Конец должен быть от 1 до 24")
    @Max(value = 24, message = "Конец должен быть от 1 до 24")
    private Integer finishHour;

    @NotNull(message = "Укажите количество людей")
    @Positive(message = "Количество людей должно быть больше 0")
    private Integer numOfPeople;

    @Size(max = 1000, message = "Комментарий должен быть не длиннее 1000 символов")
    private String comment;
}
