package com.acme.ticketing.controller;

import com.acme.ticketing.entity.Invoice;
import com.acme.ticketing.entity.SeatHold;
import com.acme.ticketing.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Mom and Dad on 3/30/2017.
 */
@RestController
@RequestMapping("/ticketing")
public class TicketingController {
    private final TicketService ticketService;

    public TicketingController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/seats/count")
    public ResponseEntity<SeatCount> numSeatsAvailable() {
        return new ResponseEntity<SeatCount>(new SeatCount(ticketService.numSeatsAvailable()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/seats/hold")
    public ResponseEntity<SeatHold> holdSeats(@RequestParam int numOfSeats,
                                              @RequestParam String customerEmail) {

        SeatHold seatHold = ticketService.findAndHoldSeats(numOfSeats, customerEmail);
        return new ResponseEntity(seatHold, seatHold == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/seats/reserve")
    public ResponseEntity<Reservation> reserveSeats(@RequestParam int seatHoldId,
                                                    @RequestParam String customerEmail) {

        String confirmation = ticketService.reserveSeats(seatHoldId, customerEmail);
        Reservation reservation = confirmation != null ? new Reservation(confirmation) : null;
        return new ResponseEntity(reservation, confirmation == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    private static class SeatCount {
        private int seatCount;
        public SeatCount(int seatCount) {
            this.seatCount = seatCount;
        }

        public int getSeatCount() {
            return seatCount;
        }
    }

    private static class Reservation {
        private String confirmation;

        public Reservation(String confirmation) {
            this.confirmation = confirmation;
        }

        public String getConfirmation() {
            return confirmation;
        }
    }
}
