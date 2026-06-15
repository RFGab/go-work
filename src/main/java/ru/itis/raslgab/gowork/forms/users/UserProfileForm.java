package ru.itis.raslgab.gowork.forms.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileForm {
    private Long id;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    private String avatarFileName;
}
