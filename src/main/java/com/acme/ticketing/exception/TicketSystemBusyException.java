package com.acme.ticketing.exception;

/**
 * Created by Mom and Dad on 3/30/2017.
 */
public class TicketSystemBusyException extends RuntimeException {
    public TicketSystemBusyException(String message) {
        super(message);
    }
}
