package org.hiahatf.mass.controllers;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.ErrorResponse;
import org.hiahatf.mass.services.monero.QuoteService;
import org.hiahatf.mass.services.monero.SwapService;
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
        // this is bad, but need a temporary fix for wallet control lock out
        SwapService.isWalletOpen = false;
        QuoteService.isWalletOpen = false;
        return ErrorResponse.builder().message(e.getMessage()).build();
    }

    /**
     * Handle server failures
     * @param e
     * @return HttpStatus 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        // this is bad, but need a temporary fix for wallet control lock out
        SwapService.isWalletOpen = false;
        QuoteService.isWalletOpen = false;
        // stray null pointers that haven't been fixed yet check (T_T)
        String msg = e.getMessage() == null ? "Internal server failure" : e.getMessage();
        return ErrorResponse.builder().message(msg).build();
    }

}
