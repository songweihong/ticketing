package com.acme.ticketing.controller;

import com.acme.ticketing.exception.BookingValidationException;
import com.acme.ticketing.exception.TicketSystemBusyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Mom and Dad on 3/30/2017.
 */
@ControllerAdvice
public class WebControllerAdvice {
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler
    public ResponseEntity handleTicketSystemBusyException(TicketSystemBusyException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler
    public ResponseEntity handleIllegalStateException(IllegalStateException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler()
    public ResponseEntity handleValidationException(BookingValidationException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler
    public ResponseEntity handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }


}
