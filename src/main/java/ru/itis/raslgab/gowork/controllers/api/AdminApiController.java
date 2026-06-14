package ru.itis.raslgab.gowork.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.raslgab.gowork.dto.AdminActionResponseDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.AdminService;

@RestController
@RequestMapping("/admin/api")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminApiController {
    private final AdminService adminService;

    @PostMapping("/{entity}")
    public AdminActionResponseDto create(@PathVariable String entity,
                                         @ModelAttribute AdminEntityForm form) {
        try {
            adminService.create(entity, form);
            return success("Объект создан");
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/{entity}/{id}")
    public AdminActionResponseDto update(@PathVariable String entity,
                                         @PathVariable Long id,
                                         @ModelAttribute AdminEntityForm form) {
        try {
            adminService.update(entity, id, form);
            return success("Изменения сохранены");
        } catch (Exception e) {
            return error(e);
        }
    }

    @DeleteMapping("/{entity}/{id}")
    public AdminActionResponseDto delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable String entity,
                                         @PathVariable Long id) {
        try {
            adminService.delete(entity, id, userDetails.getUserId());
            return success("Объект удален");
        } catch (Exception e) {
            return error(e);
        }
    }

    private AdminActionResponseDto success(String message) {
        return AdminActionResponseDto.builder()
                .success(true)
                .message(message)
                .build();
    }

    private AdminActionResponseDto error(Exception e) {
        String message = e.getMessage();
        if (e instanceof DataIntegrityViolationException) {
            message = "Нельзя удалить объект, пока на него есть ссылки";
        }
        if (e instanceof AccessDeniedException) {
            message = e.getMessage();
        }
        return AdminActionResponseDto.builder()
                .success(false)
                .message(message == null ? "Ошибка выполнения операции" : message)
                .build();
    }
}
