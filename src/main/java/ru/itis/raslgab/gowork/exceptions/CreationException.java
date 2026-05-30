package ru.itis.raslgab.gowork.exceptions;

import org.springframework.http.HttpStatus;

public class CreationException extends ServiceException {
    public CreationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
