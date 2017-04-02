package com.acme.ticketing.service;

import com.acme.ticketing.entity.*;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Created by PC on 4/2/2017.
 */
@Test(groups = {"unit"})
public class TicketServiceImplTest {
    private final String EMAIL = "test@acme.com";

    @Test
    public void testNumSeatsAvailable() throws Exception {
        // given
        Venue venue = createVenue();

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);
        int numSeatAvailable = ticketService.numSeatsAvailable();

        // then
        assertEquals(numSeatAvailable, venue.getCapacity());
    }

    @Test
    public void testFindAndHoldSeats() throws Exception {
        // given
        Venue venue = createVenue();

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);
        SeatHold seatHold = ticketService.findAndHoldSeats(1, EMAIL);

        // then
        assertEquals(seatHold.getSeats().size(), 1);
        Seat seat = seatHold.getSeats().get(0);
        assertEquals(seat.getRow(), 1);
        assertEquals(seat.getColumn(), 0);
        assertEquals(seat.getType(), SeatType.VIP);
    }

    @Test
    public void testReserveSeatsNotFound() throws Exception {
        // given
        Venue venue = createVenue();

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);

        String confirmation = ticketService.reserveSeats(1, EMAIL);
        assertNull(confirmation);
    }

    @Test
    public void testReserveSeats() throws Exception {
        // given
        int row = 1;
        int column = 2;
        Venue venue = createVenue();
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);
        Map<Integer, SeatHoldOrder> seatHoldOrderById = ticketService.seatHoldOrderById;

        Seat seat = new Seat(row, column, SeatType.VIP);
        SeatHold seatHold = new SeatHold(1, EMAIL, Lists.newArrayList(seat));

        SeatHoldOrder mockSeatHoldOrder = mock(SeatHoldOrder.class);
        when(mockSeatHoldOrder.getId()).thenReturn(1);
        when(mockSeatHoldOrder.getCustomerEmail()).thenReturn(EMAIL);
        when(mockSeatHoldOrder.cancelTimer()).thenReturn(true);
        when(mockSeatHoldOrder.getSeatHold()).thenReturn(seatHold);

        seatHoldOrderById.put(1, mockSeatHoldOrder);

        // when
        String confirmation = ticketService.reserveSeats(1, EMAIL);

        // then
        assertNotNull(confirmation);
    }

    private Venue createVenue() {
        Venue venue = new Venue(3, 3, 1, 2, 0, 3);
        venue.init();
        return venue;
    }
}