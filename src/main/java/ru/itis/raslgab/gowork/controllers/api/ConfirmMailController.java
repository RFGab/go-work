package ru.itis.raslgab.gowork.controllers.api;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.itis.raslgab.gowork.services.MailService;
import ru.itis.raslgab.gowork.services.UserSecurityService;

@RestController
@RequestMapping("auth/confirmMail")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ConfirmMailController {
    private final UserSecurityService userSecurityService;
    private final MailService mailService;

    @PostMapping("/{email}")
    public ResponseEntity<?> sendMailToConfirm(@PathVariable @Email @NotBlank String email) {
        String code = userSecurityService.generateEmailConfirmationCode(email);
        try {
            mailService.sendEmailForConfirm(email, code);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}", email, e);
            return ResponseEntity.internalServerError().body("Failed to send confirmation email");
        }
    }
}
