package ru.itis.raslgab.gowork.services.users;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.itis.raslgab.gowork.exceptions.CreationException;
import ru.itis.raslgab.gowork.forms.users.UserRegistrationForm;
import ru.itis.raslgab.gowork.mappers.UserDataMapper;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.repositories.UserRepo;
import ru.itis.raslgab.gowork.util.CodeGenerator;

import java.time.Duration;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserRepo userRepo;
    private final UserDataMapper userDataMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Long register(UserRegistrationForm form) {

        form.setEmail(normalizeEmail(form.getEmail()));
        form.setEmailConfirmationCode(normalizeCode(form.getEmailConfirmationCode()));
        log.info("Попытка создать юзера с email = {}", form.getEmail());

        // валидация данных, которую нельзя проверить с помощью валид
        // проверка почты на существование
        if (userRepo.findByEmail(form.getEmail()).isPresent()) {
            log.error("Пользак с таким email уже существует");
            throw new EntityExistsException("Пользователь с таким email уже существует");
        }
        // проверка пароля, что пользак не ошибся пока вводил
        if (! form.getPassword().equals(form.getConfirmPassword())) {
            log.error("Пользак не повторил пароль");
            throw new CreationException("Пароли не совпадают");
        }
        // проверка кода подтверждения на почту
        if (! checkEmailConfirmationCode(form.getEmail(),form.getEmailConfirmationCode())) {
            log.error("Код для подтверждения не правильный");
            throw new CreationException("Код подтверждения почты неправильный");
        }

        User user = userDataMapper.mapToModel(form);

        try {
            User newUser = userRepo.save(user);
            deleteEmailConfirmationCode(form.getEmail());
            log.info("Создан пользак с id:{}", newUser.getId());
            return newUser.getId();
        } catch (Exception e) {
            log.error("Ошибка при создании пользака:\n{}", e.getMessage());
            throw new CreationException("Ошибка при создании аккаунта");
        }
    }

    @Override
    public String generateEmailConfirmationCode(String email) {
        email = normalizeEmail(email);
        String code = CodeGenerator.generate6Char();
        String redisKey = "confirm:email:" + email;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofHours(6));
        log.info("Код подтверждения сохранён в Redis для email={}", email);
        return code;
    }

    @Override
    public boolean checkEmailConfirmationCode(String email, String code) {
        String redisKey = "confirm:email:" + normalizeEmail(email);
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        return code != null && normalizeCode(code).equals(redisValue);
    }

    private void deleteEmailConfirmationCode(String email) {
        redisTemplate.delete("confirm:email:" + normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

}
