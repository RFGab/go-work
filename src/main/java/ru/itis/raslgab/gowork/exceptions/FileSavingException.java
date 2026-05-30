package ru.itis.raslgab.gowork.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FileSavingException extends ServiceException {
    public FileSavingException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
