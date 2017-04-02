package com.acme.ticketing.exception;

/**
 * Created by Mom and Dad on 3/30/2017.
 */
public class BookingValidationException extends RuntimeException {
    public BookingValidationException(String message) {
        super(message);
    }
}
