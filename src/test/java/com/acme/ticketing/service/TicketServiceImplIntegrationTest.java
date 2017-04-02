package com.acme.ticketing.service;

import com.acme.ticketing.entity.*;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.log4testng.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

/**
 * Created by PC on 4/2/2017.
 */
@Test(groups = {"integration"})
public class TicketServiceImplIntegrationTest {
    private static Logger LOG = Logger.getLogger(TicketServiceImplIntegrationTest.class);
    private String EMAIL = "test@acme.com";

    @Test
    public void testMultipleGet() {
        // given
        Venue venue = createVenue();

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<ListenableFuture<Integer>> futures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            futures.add(service.submit(() -> ticketService.numSeatsAvailable()));
        }

        // then
        futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .forEach(seatCount -> assertEquals(seatCount.intValue(), venue.getCapacity()));
    }

    @Test
    public void testMultipleFindAndHold() {
        // given
        Venue venue = createVenue();
        Random random = new Random(System.currentTimeMillis());

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 60);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<ListenableFuture<SeatHold>> futures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(random.nextInt(100));
            } catch (Exception ex) {}
            futures.add(service.submit(() -> ticketService.findAndHoldSeats(1, EMAIL)));
        }

        // then
        ImmutableList<ListenableFuture<SeatHold>> listenableFutures = Futures.inCompletionOrder(futures);

        List<SeatHold> seats = listenableFutures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception ex) {
                        LOG.error("Failed to hold seat", ex);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertEquals(seats.size(), venue.getCapacity());

        verifySeatHold(seats.get(0), 1, 0, SeatType.VIP);
        verifySeatHold(seats.get(1), 1, 1, SeatType.VIP);
        verifySeatHold(seats.get(2), 1, 2, SeatType.VIP);
        verifySeatHold(seats.get(3), 0, 0, SeatType.NORMAL);
        verifySeatHold(seats.get(4), 0, 1, SeatType.NORMAL);
        verifySeatHold(seats.get(5), 0, 2, SeatType.NORMAL);
        verifySeatHold(seats.get(6), 2, 0, SeatType.NORMAL);
        verifySeatHold(seats.get(7), 2, 1, SeatType.NORMAL);
        verifySeatHold(seats.get(8), 2, 2, SeatType.NORMAL);
    }

    @Test
    public void testFindAndHoldTimeout() {
        // given
        Venue venue = createVenue();
        Random random = new Random(System.currentTimeMillis());
        long expiration = 20;

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 5);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<SeatHold> seatHolds = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(random.nextInt(10));
            } catch (Exception ex) {
            }
            try {
                ListenableFuture<SeatHold> holdFuture = service.submit(() -> ticketService.findAndHoldSeats(1, EMAIL));
                SeatHold seatHold = holdFuture.get();
                if (seatHold == null) {
                    continue;
                } else {
                    seatHolds.add(seatHold);
                }

            } catch (Exception ex) {
                LOG.error("Failed to hold seat", ex);
            }
        }

        // then
        assertEquals(seatHolds.size(), venue.getCapacity());

        try {
            Thread.sleep((expiration + 5)* 1000);
        } catch (Exception ex) {}

        assertEquals(ticketService.numSeatsAvailable(), venue.getCapacity());
    }

    @Test
    public void testFindAndHoldThenReserve() {
        // given
        Venue venue = createVenue();
        Random random = new Random(System.currentTimeMillis());

        // when
        TicketServiceImpl ticketService = new TicketServiceImpl(venue, 60);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<ListenableFuture<String>> reservationFutures = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(random.nextInt(100));
            } catch (Exception ex) {}
            try {
                ListenableFuture<SeatHold> holdFuture = service.submit(() -> ticketService.findAndHoldSeats(1, EMAIL));
                SeatHold seatHold = holdFuture.get();
                if (seatHold == null) {
                    continue;
                }
                ListenableFuture<String> reserveFuture = service.submit(() -> ticketService.reserveSeats(seatHold.getId(), EMAIL));
                reservationFutures.add(reserveFuture);
            } catch (Exception ex) {
                LOG.error("Failed to reserve seat", ex);
            }
        }

        // then
        ImmutableList<ListenableFuture<String>> reservationListenableFutures = Futures.inCompletionOrder(reservationFutures);

        List<String> confirmations = reservationListenableFutures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception ex) {
                        LOG.error("Failed to hold seat", ex);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertEquals(confirmations.size(), venue.getCapacity());
    }

    private void verifySeatHold(SeatHold seatHold, int row, int column, SeatType seatType) {
        verifySeat(seatHold.getSeats().get(0), row, column, seatType);
    }

    private void verifySeat(Seat seat, int row, int column, SeatType seatType) {
        assertEquals(seat.getRow(), row);
        assertEquals(seat.getColumn(), column);
        assertEquals(seat.getType(), seatType);
    }

    private Venue createVenue() {
        Venue venue = new Venue(3, 3, 1, 2, 0, 3);
        venue.init();
        return venue;
    }
}
