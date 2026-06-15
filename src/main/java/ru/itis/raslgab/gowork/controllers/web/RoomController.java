package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.itis.raslgab.gowork.forms.rooms.RoomCreateForm;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.rooms.RoomService;
import ru.itis.raslgab.gowork.services.logging.UserActionLogService;

@Controller
@RequestMapping("/organizations/{organizationId}/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final UserActionLogService userActionLogService;

    @GetMapping("/new")
    @PreAuthorize("@organizationSecurityService.isOwner(#organizationId, authentication)")
    public String createForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long organizationId,
                             Model model) {
        userActionLogService.log(userDetails.getUserId(), "ROOM_CREATE_OPEN", "organizationId=" + organizationId);

        addCreatePageAttributes(organizationId, model);
        if (!model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", RoomCreateForm.builder()
                    .status(RoomStatus.AVAILABLE)
                    .dayStart(9)
                    .dayEnd(17)
                    .build());
        }
        return "rooms/new";
    }

    @PostMapping
    @PreAuthorize("@organizationSecurityService.isOwner(#organizationId, authentication)")
    public String create(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @PathVariable Long organizationId,
                         @Valid @ModelAttribute("roomForm") RoomCreateForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        if (bindingResult.hasErrors()) {
            addCreatePageAttributes(organizationId, model);
            userActionLogService.log(userId, "ROOM_CREATE_FAILED", "organizationId=" + organizationId + ", validation errors");
            return "rooms/new";
        }

        try {
            Long roomId = roomService.createRoom(organizationId, form);
            userActionLogService.log(userId, "ROOM_CREATE_SUCCESS", "organizationId=" + organizationId + ", roomId=" + roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Комната создана");
            return "redirect:/organizations/" + organizationId;
        } catch (IllegalArgumentException e) {
            bindingResult.reject("room.invalid", e.getMessage());
            addCreatePageAttributes(organizationId, model);
            userActionLogService.log(userId, "ROOM_CREATE_FAILED", "organizationId=" + organizationId + ", " + e.getMessage());
            return "rooms/new";
        }
    }

    private void addCreatePageAttributes(Long organizationId, Model model) {
        model.addAttribute("organizationId", organizationId);
        model.addAttribute("statuses", roomService.getCreateStatuses());
        model.addAttribute("options", roomService.getAllOptions());
    }
}
