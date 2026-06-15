package ru.itis.raslgab.gowork.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.raslgab.gowork.services.rooms.RoomService;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {
    private final RoomService roomService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("popularRooms", roomService.getPopularRooms());
        return "index";
    }
}
