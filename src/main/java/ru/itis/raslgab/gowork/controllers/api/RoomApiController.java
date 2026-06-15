package ru.itis.raslgab.gowork.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.raslgab.gowork.dto.api.AdminActionResponseDto;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.rooms.RoomService;
import ru.itis.raslgab.gowork.services.logging.UserActionLogService;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomApiController {
    private final RoomService roomService;
    private final UserActionLogService userActionLogService;

    @DeleteMapping("/{roomId}/images/{fileName:.+}")
    @PreAuthorize("@roomSecurityService.canManage(#roomId, authentication)")
    public AdminActionResponseDto deleteImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable Long roomId,
                                              @PathVariable String fileName) {
        try {
            roomService.deleteRoomImage(roomId, fileName);
            userActionLogService.log(userDetails.getUserId(), "ROOM_IMAGE_DELETE_SUCCESS", "roomId=" + roomId + ", fileName=" + fileName);
            return response(true, "Фото удалено");
        } catch (Exception e) {
            userActionLogService.log(userDetails.getUserId(), "ROOM_IMAGE_DELETE_FAILED", "roomId=" + roomId + ", " + e.getMessage());
            return response(false, e.getMessage() == null ? "Не удалось удалить фото" : e.getMessage());
        }
    }

    private AdminActionResponseDto response(boolean success, String message) {
        return AdminActionResponseDto.builder()
                .success(success)
                .message(message)
                .build();
    }
}
