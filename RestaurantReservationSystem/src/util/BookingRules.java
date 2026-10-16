package util;

import enums.TableType;

import java.time.LocalDate;
import java.time.LocalTime;

/** The restaurant's booking rules in one place. Change a value here and the whole system follows. */
public final class BookingRules {

    public static final LocalTime OPENING_TIME = LocalTime.of(10, 0);
    public static final LocalTime LAST_BOOKING_TIME = LocalTime.of(21, 0);
    public static final int MAX_TABLES = 10;
    public static final int MAX_YEARS_AHEAD = 1;

    private BookingRules() {
    }

    /** Latest date a reservation can be made for (today + 1 year). */
    public static LocalDate latestBookableDate() {
        return LocalDate.now().plusYears(MAX_YEARS_AHEAD);
    }

    /** Most guests the biggest table type can hold with the maximum number of tables. */
    public static int maxGuests() {
        int biggest = 0;
        for (TableType type : TableType.values()) {
            biggest = Math.max(biggest, type.getSeats());
        }
        return biggest * MAX_TABLES;
    }

    /** Fewest tables of this type needed to seat the guests (rounded up). */
    public static int minTablesFor(int guests, TableType type) {
        return (guests + type.getSeats() - 1) / type.getSeats();
    }

    /** True if bookings for today are still open (current time is before the last booking time). */
    public static boolean isTodayStillOpen() {
        return LocalTime.now().isBefore(LAST_BOOKING_TIME);
    }
}
