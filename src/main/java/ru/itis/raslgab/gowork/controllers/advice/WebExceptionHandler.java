package ru.itis.raslgab.gowork.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.itis.raslgab.gowork.exceptions.ServiceException;

@Slf4j
@ControllerAdvice(basePackages = "ru.itis.raslgab.gowork.controllers.web")
public class WebExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return view(HttpStatus.FORBIDDEN, "Доступ запрещен", "У вас нет прав для этого действия.", request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        return view(HttpStatus.NOT_FOUND, "Страница не найдена", "Такой страницы нет или она была перемещена.", request.getRequestURI());
    }

    @ExceptionHandler(ServiceException.class)
    public ModelAndView handleService(ServiceException e, HttpServletRequest request) {
        return view(e.getStatus(), e.getStatus().getReasonPhrase(), e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = isNotFound(e) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String title = status == HttpStatus.NOT_FOUND ? "Страница не найдена" : "Некорректный запрос";
        return view(status, title, e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleInternal(Exception e, HttpServletRequest request) {
        log.error("Unhandled web error for {}", request.getRequestURI(), e);
        return view(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сервера", "Мы уже знаем, что что-то пошло не так.", request.getRequestURI());
    }

    private boolean isNotFound(IllegalArgumentException e) {
        if (e.getMessage() == null) {
            return false;
        }
        String message = e.getMessage().toLowerCase();
        return message.contains("not found") || message.contains("не найден");
    }

    private ModelAndView view(HttpStatus status, String title, String message, String path) {
        ModelAndView modelAndView = new ModelAndView("error/error");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("title", title);
        modelAndView.addObject("message", message);
        modelAndView.addObject("path", path);
        return modelAndView;
    }
}
