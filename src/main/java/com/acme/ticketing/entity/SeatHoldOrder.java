package com.acme.ticketing.entity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by PC on 4/1/2017.
 */
public class SeatHoldOrder {
    private final int id;
    private final String customerEmail;
    private final List<Seat> seats;
    private final long expireInMillis;
    private final TimerTask timerTask;
    private Timer timer;

    public SeatHoldOrder(int id, String customerEmail, List<Seat> seats, long expireInMillis, TimerTask timerTask) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.seats = seats;
        this.expireInMillis = expireInMillis;
        this.timerTask = timerTask;
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

    public SeatHold getSeatHold() {
        return new SeatHold(id, customerEmail, seats);
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(timerTask, expireInMillis);
    }

    public boolean cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            return true;
        }

        return false;
    }

    public boolean isCanceled() {
        return timer == null;
    }
}
