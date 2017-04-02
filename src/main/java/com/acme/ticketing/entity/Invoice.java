package com.acme.ticketing.entity;

import java.util.List;

/**
 * Created by PC on 4/1/2017.
 */
public final class Invoice {
    private final String confirmation;
    private final String customerEmail;
    private final List<Seat> seats;

    public Invoice(String confirmation, String customerEmail, List<Seat> seats) {
        this.confirmation = confirmation;
        this.customerEmail = customerEmail;
        this.seats = seats;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}
