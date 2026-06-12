package ru.itis.raslgab.gowork.controllers.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class AppErrorController implements ErrorController {

    @RequestMapping
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode == null ? 500 : Integer.parseInt(statusCode.toString());
        return fillModel(status, errorPath(request), response, model);
    }

    @RequestMapping("/{status}")
    public String errorByStatus(@PathVariable int status, HttpServletRequest request, HttpServletResponse response, Model model) {
        return fillModel(status, request.getRequestURI(), response, model);
    }

    private String fillModel(int status, String path, HttpServletResponse response, Model model) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        int safeStatus = httpStatus == null ? 500 : httpStatus.value();
        response.setStatus(safeStatus);
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

    private String errorPath(HttpServletRequest request) {
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return path == null ? request.getRequestURI() : path.toString();
    }
}
