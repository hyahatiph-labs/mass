package com.hiahatf.mass.controllers;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
 
    @ExceptionHandler(MassException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMassException(MassException e) {
        return ErrorResponse.builder().message(e.getMessage()).build();
    }

}
