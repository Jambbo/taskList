package com.example.tasklist.web.controller;

import com.example.tasklist.domain.exception.AccessDeniedException;
import com.example.tasklist.domain.exception.ExceptionBody;
import com.example.tasklist.domain.exception.ResourceMappingException;
import com.example.tasklist.domain.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)// Все ошибки которые выбрасываются мы помечаем @ResponseStatus
    public ExceptionBody handleResourceNotFound(ResourceNotFoundException e){
        return new ExceptionBody(e.getMessage());
    }
    @ExceptionHandler(ResourceMappingException.class)
    //Это когда мы не можем смепить, тобиж проблема на стороне сервера
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// Все ошибки которые выбрасываются мы помечаем @ResponseStatus
    public ExceptionBody handleResourceMapping(ResourceMappingException e){
        return new ExceptionBody(e.getMessage());
    }

    //Обрабатываем IllegalStateException, который мы выбрасываем если у нас не совпадают пароли или если пользователь зарегистрирован
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleIllegalState(IllegalStateException e){
            return new ExceptionBody(e.getMessage());
    }

    //Это исключение будет выбрасываться когда не подходит токен либо не прошла авторизация
    @ExceptionHandler({AccessDeniedException.class,org.springframework.security.access.AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionBody handleAccessDenied(){
        return new ExceptionBody("Access denied.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        ExceptionBody exceptionBody = new ExceptionBody("Validation failed.");
        List<FieldError> errors = e.getBindingResult().getFieldErrors();//То есть ошибки связанные с полями
        exceptionBody.setErrors(errors.stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage)));
        //Тоби ж буде возвращаться сообщение об ошибках и сами поля
        return exceptionBody;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionBody handleConstraintViolation(ConstraintViolationException e){
        ExceptionBody exceptionBody = new ExceptionBody("Validation failed.");
        exceptionBody.setErrors(e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )));
        return exceptionBody;
    }
    //Когда у нас происходит исключение, которое ни одно из тех, что сверху, то будет работать этот обработчик.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionBody handleException(Exception e){
        return new ExceptionBody("Internal error. ");
    }

}
