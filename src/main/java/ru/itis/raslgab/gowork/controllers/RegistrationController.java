package ru.itis.raslgab.gowork.controllers;


import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.raslgab.gowork.exceptions.CreationException;
import ru.itis.raslgab.gowork.forms.UserRegistrationForm;
import ru.itis.raslgab.gowork.services.UserSecurityService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {
    private final UserSecurityService userService;

    @GetMapping("/reg")
    public String registerForm(UserRegistrationForm form,
                               Model model) {
        log.info("Открыта страница регистрации");
        model.addAttribute("userForm", form);
        return "auth/reg";
    }

    @PostMapping("/reg")
    public String register(@Valid @ModelAttribute("userForm") UserRegistrationForm form,
                           BindingResult bindingResult,
                           Model model) {
        log.info("Попытка создать акк");

        if (bindingResult.hasErrors()) {
            log.error("Ошибка при валидации данных");
            model.addAttribute("userForm", form);
            return "auth/reg";
        }


        try {
            userService.register(form);
        } catch (EntityExistsException e) {
            log.error(e.getMessage());
            form.setEmail("");
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("userForm", form);
            return "auth/reg";
        } catch (CreationException e) {
            log.error(e.getMessage());
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("userForm", form);
            return "auth/reg";
        }

        log.info("Акк успешно создан, ждем подтверждения почты");
        //        userService.sendMailToConfirm();

        return "redirect:/auth/confirmEmail";
    }
}
