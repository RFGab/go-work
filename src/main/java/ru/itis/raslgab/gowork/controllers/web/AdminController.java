package ru.itis.raslgab.gowork.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.raslgab.gowork.dto.admin.AdminPageDto;
import ru.itis.raslgab.gowork.services.admin.AdminService;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("counts", adminService.getDashboardCounts());
        return "admin/dashboard";
    }

    @GetMapping("/{entity}")
    public String entityPage(@PathVariable String entity,
                             @RequestParam(defaultValue = "0") Integer page,
                             @RequestParam(defaultValue = "10") Integer size,
                             @RequestParam(defaultValue = "id") String sort,
                             @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                             Model model) {
        AdminPageDto adminPage = adminService.getPage(entity, page, size, sort, direction);
        model.addAttribute("adminPage", adminPage);
        model.addAttribute("pageNumbers", getPageNumbers(adminPage.getRows()));
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        return "admin/entity";
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
