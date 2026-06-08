package ru.itis.raslgab.gowork.controllers.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class AppErrorController {

    @GetMapping
    public String error(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode == null ? 500 : Integer.parseInt(statusCode.toString());
        return fillModel(status, request.getRequestURI(), model);
    }

    @GetMapping("/{status}")
    public String errorByStatus(@PathVariable int status, HttpServletRequest request, Model model) {
        return fillModel(status, request.getRequestURI(), model);
    }

    private String fillModel(int status, String path, Model model) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        int safeStatus = httpStatus == null ? 500 : httpStatus.value();
        model.addAttribute("status", safeStatus);
        model.addAttribute("path", path);

        if (safeStatus == 403) {
            model.addAttribute("title", "Доступ запрещен");
            model.addAttribute("message", "У вас нет прав для просмотра этой страницы.");
        } else if (safeStatus == 404) {
            model.addAttribute("title", "Страница не найдена");
            model.addAttribute("message", "Такой страницы нет или она была перемещена.");
        } else {
            model.addAttribute("title", "Ошибка сервера");
            model.addAttribute("message", "Мы уже знаем, что что-то пошло не так.");
        }

        return "error/error";
    }
}
