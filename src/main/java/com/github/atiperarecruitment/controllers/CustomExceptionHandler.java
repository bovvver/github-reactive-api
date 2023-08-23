package com.github.atiperarecruitment.controllers;

import com.github.atiperarecruitment.exceptions.UserNotFoundException;
import com.github.atiperarecruitment.exceptions.WrongHeaderException;
import com.github.atiperarecruitment.responsedto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ErrorDTO handleUserNotFound(UserNotFoundException exception){
        return new ErrorDTO(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(WrongHeaderException.class)
    public ErrorDTO handleWrongHeader(WrongHeaderException exception){
        return new ErrorDTO(HttpStatus.NOT_ACCEPTABLE.value(), exception.getMessage());
    }
}