package com.acme.ticketing.entity;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;
import java.util.TimerTask;

import static org.testng.Assert.*;

/**
 * Created by PC on 4/1/2017.
 */
@Test(groups = {"unit"})
public class SeatHoldOrderTest {
    private final int id = 1;
    private final String customerEmail = "test@acme.come";
    private final List<Seat> seats = Lists.newArrayList(new Seat(0, 0, SeatType.NORMAL));
    private final long expireInMillis = 5000;

    @Test
    public void testStartTimer() throws Exception {
        // when
        SeatHoldOrder order = createOrder();
        order.startTimer();

        // then
        assertFalse(order.isCanceled());
    }

    @Test
    public void testCancelTimer() throws Exception {
        // when
        SeatHoldOrder order = createOrder();
        order.startTimer();
        order.cancelTimer();

        // then
        assertTrue(order.isCanceled());
    }

    private SeatHoldOrder createOrder() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            }
        };
        return new SeatHoldOrder(id, customerEmail, seats, expireInMillis, timerTask);
    }
}