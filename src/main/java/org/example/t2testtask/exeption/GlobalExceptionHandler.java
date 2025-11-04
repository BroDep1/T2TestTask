package org.example.t2testtask.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    public record AppError(int statusCode, String message){}

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public AppError handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error(e.getMessage(), e);
        return new AppError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(FileParsingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public AppError handleFileParsingException(FileParsingException e) {
        log.error(e.getMessage(), e);
        return new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

}
