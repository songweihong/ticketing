package com.acme.ticketing.entity;

import java.util.List;

/**
 * Created by PC on 3/31/2017.
 */
public final class SeatHold {
    private final int id;
    private final String customerEmail;
    private final List<Seat> seats;

    public SeatHold(int id, String customerEmail, List<Seat> seats) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.seats = seats;
    }

    public int getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeatHold seatHold = (SeatHold) o;

        return id == seatHold.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
