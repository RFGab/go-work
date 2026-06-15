package ru.itis.raslgab.gowork.controllers.web;

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
import ru.itis.raslgab.gowork.forms.users.UserRegistrationForm;
import ru.itis.raslgab.gowork.services.logging.UserActionLogService;
import ru.itis.raslgab.gowork.services.users.UserSecurityService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {
    private final UserSecurityService userService;
    private final UserActionLogService userActionLogService;

    @GetMapping("/register")
    public String registerForm(UserRegistrationForm form, Model model) {
        log.info("Opened registration page");
        userActionLogService.logAnonymous("REGISTER_PAGE_OPEN", "Registration page opened");
        model.addAttribute("userForm", form);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userForm") UserRegistrationForm form,
                           BindingResult bindingResult,
                           Model model) {
        log.info("Registration attempt for email={}", form.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed for email={}", form.getEmail());
            userActionLogService.logAnonymous("REGISTER_FAILED", "Validation errors for email=" + form.getEmail());
            model.addAttribute("userForm", form);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "auth/register";
        }

        try {
            Long id = userService.register(form);
            log.info("Account created with id={}", id);
            userActionLogService.log(id, "REGISTER_SUCCESS", "Account created");
        } catch (EntityExistsException e) {
            log.warn("Registration failed: email already exists");
            userActionLogService.logAnonymous("REGISTER_FAILED", "Email already exists: " + form.getEmail());
            form.setEmail("");
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("userForm", form);
            return "auth/register";
        } catch (CreationException e) {
            log.warn("Registration failed: {}", e.getMessage());
            userActionLogService.logAnonymous("REGISTER_FAILED", e.getMessage());
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("userForm", form);
            return "auth/register";
        }

        return "redirect:/auth/login?registered";
    }
}
