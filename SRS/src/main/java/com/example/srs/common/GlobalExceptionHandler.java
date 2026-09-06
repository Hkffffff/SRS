package com.example.srs.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return Result.error(getFirstFieldError(exception.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException exception) {
        return Result.error(getFirstFieldError(exception.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException exception) {
        return Result.error(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return Result.error("请求体格式错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return Result.error(exception.getMessage());
    }

    private String getFirstFieldError(FieldError fieldError) {
        if (fieldError == null) {
            return "请求参数错误";
        }
        return fieldError.getDefaultMessage();
    }
}
