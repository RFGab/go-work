package ru.itis.raslgab.gowork.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.raslgab.gowork.dto.AdminActionResponseDto;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.OrganizationService;
import ru.itis.raslgab.gowork.services.RoomService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusApiController {
    private final OrganizationService organizationService;
    private final RoomService roomService;
    private final UserActionLogService userActionLogService;

    @PostMapping("/organizations/{organizationId}")
    public AdminActionResponseDto updateOrganizationStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long organizationId,
                                                           @RequestParam OrganizationStatus status) {
        try {
            organizationService.updateStatus(organizationId, userDetails.getUserId(), userDetails.getUser().getRole(), status);
            userActionLogService.log(userDetails.getUserId(), "ORGANIZATION_STATUS_UPDATE", "organizationId=" + organizationId + ", status=" + status);
            return success("Статус организации обновлен");
        } catch (Exception e) {
            return error(e);
        }
    }

    @PostMapping("/rooms/{roomId}")
    public AdminActionResponseDto updateRoomStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @PathVariable Long roomId,
                                                   @RequestParam RoomStatus status) {
        try {
            roomService.updateStatus(roomId, userDetails.getUserId(), userDetails.getUser().getRole(), status);
            userActionLogService.log(userDetails.getUserId(), "ROOM_STATUS_UPDATE", "roomId=" + roomId + ", status=" + status);
            return success("Статус комнаты обновлен");
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
        return AdminActionResponseDto.builder()
                .success(false)
                .message(e.getMessage() == null ? "Ошибка обновления статуса" : e.getMessage())
                .build();
    }
}
