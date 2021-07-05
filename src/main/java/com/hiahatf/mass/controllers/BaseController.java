package com.hiahatf.mass.controllers;

import javax.net.ssl.SSLException;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Using a base controller for global error handling
 */
@RestController
public class BaseController {
 
    /**
     * Handle application exceptions and bad data
     * @param e
     * @return HttpStatus 400
     */
    @ExceptionHandler(MassException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMassException(MassException e) {
        return ErrorResponse.builder().message(e.getMessage()).build();
    }

    /**
     * Handle ssl exception
     * @param e
     * @return HttpStatus 503
     */
    @ExceptionHandler(SSLException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleSslException(SSLException e) {
        return ErrorResponse.builder().message(e.getMessage()).build();
    }

    /**
     * Handle anything that was missed
     * @param e
     * @return HttpStatus 503
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleException(Exception e) {
        return ErrorResponse.builder().message(e.getMessage()).build();
    }

}
