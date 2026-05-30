package ru.itis.raslgab.gowork.services;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itis.raslgab.gowork.exceptions.CreationException;
import ru.itis.raslgab.gowork.forms.UserRegistrationForm;
import ru.itis.raslgab.gowork.mappers.UserDataMapper;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.repositories.UserRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserRepo userRepo;
    private final UserDataMapper userDataMapper;

    @Override
    public void register(UserRegistrationForm form) {

        log.info("Попытка создать юзера с email = {}", form.getEmail());

        // валидация данных
        if (userRepo.findByEmail(form.getEmail()).isPresent()) {
            log.error("Пользак с таким email уже существует");
            throw new EntityExistsException("Пользователь с таким email уже существует");
        }
        if (! form.getPassword().equals(form.getConfirmPassword())) {
            log.error("Пользак не повторил пароль");
            throw new CreationException("Пароли не совпадают");
        }

        // сохранение
        User user = userDataMapper.mapToModel(form);
        user.setRole(RoleEnum.USER);

        try {
            User newUser = userRepo.save(user);
            log.info("Создан пользак с id:{}", newUser.getId());
        } catch (Exception e) {
            log.error("Ошибка при создании пользака:\n{}", e.getMessage());
            throw new CreationException("Ошибка при создании аккаунта");
        }
    }

    @Override
    public void activate(String code) {

    }

    @Override
    public void confirmMail(Long id) {

    }

    @Override
    public void sendMailToConfirm(Long id) {

    }
}
