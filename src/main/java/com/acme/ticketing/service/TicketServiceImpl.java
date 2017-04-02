package com.acme.ticketing.service;

import com.acme.ticketing.entity.*;
import com.acme.ticketing.exception.BookingValidationException;
import com.google.common.collect.Maps;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Mom and Dad on 3/30/2017.
 */
@Service
public class TicketServiceImpl implements TicketService {

    private static final String INVALID_SEAT_INPUT = "Invalid seat count, please provide a number between % and %s";
    private static final String INVALID_EMAIL_INPUT = "Invalid email";

    private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final Venue venue;
    private volatile int numSeatOnHold;
    private volatile int numSeatSold;
    Map<Integer, SeatHoldOrder> seatHoldOrderById = Maps.newConcurrentMap();
    private AtomicInteger idGen = new AtomicInteger();

    private ReentrantReadWriteLock onHoldLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock reservationLock = new ReentrantReadWriteLock(true);
    private final long seatHoldTTL;

    @Autowired
    public TicketServiceImpl(Venue venue, @Value("${order.hold.ttl:86400}") long seatHoldTTL ) {
        this.venue = venue;
        this.seatHoldTTL = seatHoldTTL;
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    @Override
    public int numSeatsAvailable() {
        LOG.info("Checking number of seats available ...");

        try {
            onHoldLock.readLock().lock();
            reservationLock.readLock().lock();
            int seatsAvailable = venue.getCapacity() - numSeatOnHold - numSeatSold;
            LOG.info("Found {} seats available", seatsAvailable);
            return seatsAvailable;
        } finally {
            onHoldLock.readLock().unlock();
            reservationLock.readLock().unlock();
        }
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        LOG.info("Finding and holding {} seats for customer {}", numSeats, customerEmail);

        validateBookingSeatNumber(numSeats);
        validateCustomerEmail(customerEmail);

        // check availability
        if (venue.getCapacity() - numSeatOnHold - numSeatSold - numSeats < 0) {
            LOG.warn("Not enough seats available, please try another showtime");
            return null;
        }

        try {
            // holding seats
            onHoldLock.writeLock().lock();
            // check again
            if (venue.getCapacity() - numSeatOnHold - numSeatSold - numSeats < 0) {
                LOG.warn("Not enough seats available, please try another showtime");
                return null;
            }
            // update seatOnHold count
            numSeatOnHold += numSeats;
            LOG.debug("Total seats on hold: {}", numSeatOnHold);

            // holding seats in venue
            LOG.debug("Holding seats in venue");
            final List<Seat> seatsOnHold = venue.holdSeats(numSeats);
            int id = idGen.addAndGet(1);

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    LOG.debug("On hold expired, resetting seat status, id {}, customer {}", id, customerEmail);
                    onHoldLock.writeLock().lock();
                    seatsOnHold.stream().forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE));
                    if (seatHoldOrderById.remove(id) != null) {
                        numSeatOnHold -= seatsOnHold.size();
                    }
                    onHoldLock.writeLock().unlock();
                    LOG.debug("Seat statuses reset for id {}, customer {}", id, customerEmail);
                }
            };
            SeatHoldOrder seatHoldOrder = new SeatHoldOrder(id, customerEmail, seatsOnHold,seatHoldTTL * 1000, timerTask);
            seatHoldOrderById.put(id, seatHoldOrder);
            seatHoldOrder.startTimer();
            return seatHoldOrder.getSeatHold();
        } finally {
            onHoldLock.writeLock().unlock();
        }
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        LOG.info("Reserving seat(s) for id {}, customer: {}", seatHoldId, customerEmail);

        validateCustomerEmail(customerEmail);
        SeatHoldOrder seatHoldOrder;

        try {
            // find seat hold order
            onHoldLock.readLock().lock();
            seatHoldOrder = seatHoldOrderById.get(seatHoldId);

            if (seatHoldOrder == null || !seatHoldOrder.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
                LOG.debug("Seat(s) Hold not found for id {}, customer: {}", seatHoldId, customerEmail);
                return null;
            }
        } finally {
            onHoldLock.readLock().unlock();
        }

        // stop the expiration timer
        if (!seatHoldOrder.cancelTimer()) {
            // timer expired
            return null;
        }

        try {
            onHoldLock.writeLock().lock();
            reservationLock.writeLock().lock();
            // get seats to be reserved
            List<Seat> seats = seatHoldOrder.getSeatHold().getSeats();
            // update seats on hold count and seats sold count
            numSeatSold += seats.size();
            numSeatOnHold -= seats.size();
        } finally {
            onHoldLock.writeLock().unlock();
            reservationLock.writeLock().unlock();
        }

        Invoice invoice = reserveSeats(seatHoldOrder.getSeatHold());
        LOG.info("Seat(s) reserved for customer {}, confirmation number: {}", customerEmail, invoice.getConfirmation());
        return invoice.getConfirmation();
    }

    private Invoice reserveSeats(SeatHold seatHold) {
        String confirmation = UUID.randomUUID().toString();
        List<Seat> seats = seatHold.getSeats();
        seats.forEach(seat -> seat.setStatus(SeatStatus.SOLD));

        return new Invoice(confirmation, seatHold.getCustomerEmail(), seats);
    }

    private void validateBookingSeatNumber(int numSeats) {
        if (numSeats <= 0 || numSeats > venue.getCapacity()) {
            throw new BookingValidationException(String.format(INVALID_SEAT_INPUT, 1, venue.getCapacity()));
        }
    }

    private void validateCustomerEmail(String customerEmail) {
        if (!EmailValidator.getInstance().isValid(customerEmail)) {
            throw new BookingValidationException(INVALID_EMAIL_INPUT);
        }
    }
}