package ru.itis.raslgab.gowork.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.itis.raslgab.gowork.services.UserSecurityService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth/confirmEmail")
@Slf4j
public class ConfirmEmailController {
    private final UserSecurityService userService;

    @GetMapping
    public String getConfirmEmail(@RequestParam Long id) {
        userService.sendMailToConfirm(id);
        return "registration/confirmEmail";
    }

    @PostMapping("/{id}")
    public String confirmEmail(@PathVariable Long id) {
        userService.confirmMail(id);
        return "redirect:/";
    }
}
