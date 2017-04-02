package com.acme.ticketing.entity;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by PC on 4/2/2017.
 */
@Test(groups = {"unit"})
public class VenueTest {
    @Test
    public void testHoldSeats() throws Exception {

        // given
        Venue venue = new Venue(3, 3, 1, 2, 0, 3);
        venue.init();

        // when
        List<Seat> seats = venue.holdSeats(venue.getCapacity());

        // then
        assertEquals(seats.size(), venue.getCapacity());
        verifySeat(seats.get(0), 1, 0, SeatType.VIP);
        verifySeat(seats.get(1), 1, 1, SeatType.VIP);
        verifySeat(seats.get(2), 1, 2, SeatType.VIP);
        verifySeat(seats.get(3), 0, 0, SeatType.NORMAL);
        verifySeat(seats.get(4), 0, 1, SeatType.NORMAL);
        verifySeat(seats.get(5), 0, 2, SeatType.NORMAL);
        verifySeat(seats.get(6), 2, 0, SeatType.NORMAL);
        verifySeat(seats.get(7), 2, 1, SeatType.NORMAL);
        verifySeat(seats.get(8), 2, 2, SeatType.NORMAL);
    }

    private void verifySeat(Seat seat, int row, int column, SeatType seatType) {
        assertEquals(seat.getRow(), row);
        assertEquals(seat.getColumn(), column);
        assertEquals(seat.getType(), seatType);
    }
}