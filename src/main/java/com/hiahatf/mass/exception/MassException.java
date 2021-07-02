package com.hiahatf.mass.exception;

/**
 * Class for handling application exceptions
 */
public class MassException extends Exception {
    
    private String message;

    public MassException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
