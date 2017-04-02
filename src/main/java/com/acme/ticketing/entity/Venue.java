package com.acme.ticketing.entity;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by PC on 3/31/2017.
 */
@Component
public class Venue {
    private static final String INVALID_MAX_ROW = "Invalid seat max row, should be a positive number";
    private static final String INVALID_MAX_COLUMN = "Invalid seat max column, should be a positive number";
    private static final String INVALID_BEST_ROW_START = "Invalid best row start, should be a number between %s and %s";
    private static final String INVALID_BEST_ROW_END =  "Invalid best row end, should be a number between %s and %s";
    private static final String INVALID_BEST_COLUMN_START = "Invalid best column start, should be a number between %s and %s";
    private static final String INVALID_BEST_COLUMN_END =  "Invalid best colu\"Please provide a valid seat count between 1 and \" + maxSeatsmn end, should be a number between %s and %s";

    private final int maxRow;
    private final int maxColumn;
    private final int bestRowStart;
    private final int bestRowEnd;
    private final int bestColumnStart;
    private final int bestColumnEnd;
    private final List<List<Seat>> seats;
    private final int capacity;

    private ReentrantLock lock = new ReentrantLock(true);

    public Venue(@Value("${venue.maxRow:9}") int maxRow,
                 @Value("${venue.maxColumn:33}") int maxColumn,
                 @Value("${venue.bestRow.start:3}") int bestRowStart,
                 @Value("${venue.bestRow.end:5}") int bestRowEnd,
                 @Value("${venue.bestColumn.start:11}") int bestColumnStart,
                 @Value("${venue.bestColumn.end:21}") int bestColumnEnd) {

        Preconditions.checkArgument(maxRow > 0, INVALID_MAX_ROW);
        Preconditions.checkArgument(maxColumn > 0, INVALID_MAX_COLUMN);
        Preconditions.checkArgument(bestRowStart >= 0 && bestRowStart < maxRow, INVALID_BEST_ROW_START, 0, maxRow);
        Preconditions.checkArgument(bestRowEnd >= bestRowStart && bestRowEnd < maxRow, INVALID_BEST_ROW_END, bestRowStart, maxRow);
        Preconditions.checkArgument(bestColumnStart >= 0 && bestColumnStart < maxColumn, INVALID_BEST_COLUMN_START, 0, maxColumn);
        Preconditions.checkArgument(maxRow > 0, INVALID_BEST_COLUMN_END, bestColumnStart, maxColumn);

        this.maxRow = maxRow;
        this.maxColumn = maxColumn;
        this.capacity = maxRow * maxColumn;
        this.bestRowStart = bestRowStart;
        this.bestRowEnd = bestRowEnd;
        this.bestColumnStart = bestColumnStart;
        this.bestColumnEnd = bestColumnEnd;
        seats = Collections.synchronizedList(new ArrayList<>(maxRow));
    }

    @PostConstruct
    public void init() {

        for (int i = 0; i < maxRow; i++) {
            List<Seat> seatRow = new ArrayList<>(maxColumn);
            for (int j = 0; j < maxColumn; j++) {
                SeatType type = i >= bestRowStart && i < bestRowEnd && j >= bestColumnStart && j < bestColumnEnd ?
                        SeatType.VIP : SeatType.NORMAL;

                seatRow.add(new Seat(i, j, type));
            }
            seats.add(seatRow);
        }
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * hold number of seats specified
     * @param number
     * @return
     */
    public List<Seat> holdSeats(int number) {
        try {
            lock.lock();
            // find seats available
            List<Seat> seatsAvailable = seats.stream()
                    .flatMap(Collection::stream)
                    .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                    .collect(Collectors.toList());

            // find the best seats
            List<Seat> seatsSelected = seatsAvailable.stream()
                    .sorted((a, b) -> new CompareToBuilder().append(a.getType(), b.getType())
                            .append(a.getRow(), b.getRow())
                            .append(a.getColumn(), b.getColumn())
                            .build())
                    .collect(Collectors.toList())
                    .subList(0, number);

            // update seat status
            seatsSelected.stream().forEach(seat -> seat.setStatus(SeatStatus.HOLD));

            return seatsSelected;
        } finally {
            lock.unlock();
        }
    }
}
