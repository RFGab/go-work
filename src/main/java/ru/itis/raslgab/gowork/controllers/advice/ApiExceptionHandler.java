package ru.itis.raslgab.gowork.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.itis.raslgab.gowork.dto.api.ApiErrorDto;
import ru.itis.raslgab.gowork.exceptions.ServiceException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice(basePackages = "ru.itis.raslgab.gowork.controllers.api")
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Доступ запрещен", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Ресурс не найден", request);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiErrorDto> handleService(ServiceException e, HttpServletRequest request) {
        return build(e.getStatus(), e.getMessage(), request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ApiErrorDto> handleBadRequest(Exception e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleInternal(Exception e, HttpServletRequest request) {
        log.error("Unhandled API error for {}", request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request);
    }

    private ResponseEntity<ApiErrorDto> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build());
    }
}
