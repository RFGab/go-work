package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.RoomCatalogFilterForm;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.RoomCatalogService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomCatalogController {
    private final RoomCatalogService roomCatalogService;
    private final UserActionLogService userActionLogService;

    @GetMapping
    public String catalog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @Valid @ModelAttribute("filter") RoomCatalogFilterForm filter,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            filter.setMinCapacity(null);
        }

        Page<RoomCatalogItemDto> roomsPage = roomCatalogService.getCatalog(filter);
        model.addAttribute("roomsPage", roomsPage);
        model.addAttribute("rooms", roomsPage.getContent());
        model.addAttribute("cities", roomCatalogService.getCityOptions());
        model.addAttribute("pageNumbers", getPageNumbers(roomsPage));

        userActionLogService.log(
                userDetails.getUserId(),
                "ROOM_CATALOG_OPEN",
                "cityId=" + filter.getCityId()
                        + ", availableToday=" + filter.getAvailableToday()
                        + ", minCapacity=" + filter.getMinCapacity()
                        + ", page=" + filter.safePage()
                        + ", size=" + filter.safeSize()
        );
        return "rooms/catalog";
    }

    private List<Integer> getPageNumbers(Page<?> page) {
        if (page.getTotalPages() == 0) {
            return List.of();
        }
        return IntStream.range(0, page.getTotalPages())
                .boxed()
                .toList();
    }
}
