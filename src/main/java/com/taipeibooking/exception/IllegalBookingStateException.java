package com.taipeibooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.CONFLICT) 
public class IllegalBookingStateException extends RuntimeException {

    private static final long serialVersionUID = 1L; 

    public IllegalBookingStateException(String message) {
        super(message);
    }

    public IllegalBookingStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
