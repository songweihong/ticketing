package com.acme.ticketing.controller;

import com.acme.ticketing.entity.Seat;
import com.acme.ticketing.entity.SeatHold;
import com.acme.ticketing.entity.SeatType;
import com.acme.ticketing.exception.BookingValidationException;
import com.acme.ticketing.service.TicketService;
import com.google.common.collect.Lists;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by PC on 4/2/2017.
 */
@Test(groups = {"unit"})
public class TicketingControllerTest {
    private static final String EMAIL = "test@acme.com";

    @Mock
    private TicketService ticketService;
    @InjectMocks
    private TicketingController ticketingController;
    private MockMvc mockMvc;

    @BeforeClass
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(ticketingController)
                        .setMessageConverters(new MappingJackson2HttpMessageConverter())
                        .setControllerAdvice(new WebControllerAdvice())
                        .build();
    }

    @BeforeMethod
    public void setupTest() {
        Mockito.reset(ticketService);
    }

    @Test
    public void testNumSeatsAvailable() throws Exception {
        // given
        int numSeatsAvailable = 10;
        when(ticketService.numSeatsAvailable()).thenReturn(numSeatsAvailable);

        // when
        ResultActions actions = mockMvc.perform(get("/ticketing/seats/count"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.seatCount", is(numSeatsAvailable)));
    }

    @Test
    public void testHoldSeatsInvalidSeatNumber() throws Exception {
        // given
        int numSeats = -1;
        doThrow(new BookingValidationException("")).when(ticketService).findAndHoldSeats(eq(numSeats), eq(EMAIL));

        // when
        ResultActions actions = mockMvc.perform(post("/ticketing/seats/hold")
                .param("numOfSeats", String.valueOf(numSeats))
                .param("customerEmail", EMAIL));

        // then
        actions.andExpect(status().isBadRequest());
    }

    @Test
    public void testHoldSeatsInvalidEmail() throws Exception {
        // given
        int numSeats = 1;
        doThrow(new BookingValidationException("")).when(ticketService).findAndHoldSeats(anyInt(), anyString());

        // when
        ResultActions actions = mockMvc.perform(post("/ticketing/seats/hold")
                .param("numOfSeats", String.valueOf(numSeats))
                .param("customerEmail", EMAIL));

        // then
        actions.andExpect(status().isBadRequest());
    }

    @Test
    public void testHoldSeatsNotFound() throws Exception {
        // given
        int numSeats = 1;
        when(ticketService.findAndHoldSeats(eq(numSeats), eq(EMAIL))).thenReturn(null);
        // when
        ResultActions actions = mockMvc.perform(post("/ticketing/seats/hold")
                                                .param("numOfSeats", String.valueOf(numSeats))
                                                .param("customerEmail", EMAIL));

        // then
        actions.andExpect(status().isNotFound());
    }

    @Test
    public void testHoldSeats() throws Exception {
        // given
        int id = 1;
        int numSeats = 1;
        int seatRow = 2;
        int seatColumn = 3;

        Seat seat = new Seat(seatRow, seatColumn, SeatType.VIP);
        SeatHold seatHold = new SeatHold(id, EMAIL, Lists.newArrayList(seat));
        when(ticketService.findAndHoldSeats(eq(numSeats), eq(EMAIL))).thenReturn(seatHold);

        // when
        ResultActions actions = mockMvc.perform(post("/ticketing/seats/hold")
                .param("numOfSeats", String.valueOf(numSeats))
                .param("customerEmail", EMAIL));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.customerEmail", is(EMAIL)))
                .andExpect(jsonPath("$.seats", hasSize(1)))
                .andExpect(jsonPath("$.seats[0].row", is(seatRow)))
                .andExpect(jsonPath("$.seats[0].column", is(seatColumn)))
        ;
    }

    @Test
    public void testReserveSeats() throws Exception {
        // given
        int seatHoldId = 1;
        String confirmation = "confirmation";
        when(ticketService.reserveSeats(eq(seatHoldId), eq(EMAIL))).thenReturn(confirmation);

        // when
        ResultActions actions = mockMvc.perform(post("/ticketing/seats/reserve")
                .param("seatHoldId", String.valueOf(seatHoldId))
                .param("customerEmail", EMAIL));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation", is(confirmation)));
    }
}