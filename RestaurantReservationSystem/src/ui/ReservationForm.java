package ui;

import enums.TableType;
import model.PendingChange;
import model.Reservation;
import util.BookingRules;
import util.Validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * The five reservation questions (date, time, guests, table type, tables).
 * Each answer is checked as soon as it is typed; a wrong answer only repeats that question.
 * steps() returns the questions so a StepForm can run them, which lets \ go back one question.
 *
 * Used for a new reservation (current = null) and for an update, where Enter keeps the current value.
 */
public class ReservationForm {

    private Reservation current;
    private LocalDate date;
    private LocalTime time;
    private int guests;
    private TableType tableType;
    private int numTables;

    /** Set the reservation being updated (leave unset for a new reservation). */
    public void setCurrent(Reservation current) {
        this.current = current;
    }

    public List<StepForm.Step> steps() {
        return Arrays.<StepForm.Step>asList(this::askDate, this::askTime, this::askGuests,
                this::askTableType, this::askTables);
    }

    public PendingChange toChange() {
        return new PendingChange(date, time, guests, tableType, numTables);
    }

    private boolean updating() {
        return current != null;
    }

    private void askDate() {
        System.out.println("  Booking hours: " + BookingRules.OPENING_TIME + " to " + BookingRules.LAST_BOOKING_TIME
                + "   Latest date: " + BookingRules.latestBookableDate());
        if (updating()) {
            System.out.println("  Press Enter to keep the value in [brackets].");
            String now = current.getDate().toString();
            date = ConsoleInput.promptOrKeep("Reservation date (YYYY-MM-DD)", now, now, Validator::reservationDate);
        } else {
            date = ConsoleInput.prompt("Reservation date (YYYY-MM-DD): ", Validator::reservationDate);
        }
    }

    private void askTime() {
        if (updating()) {
            String now = current.getTime().toString();
            time = ConsoleInput.promptOrKeep("Reservation time, 24-hour (HH:mm)", now, now,
                    Validator.reservationTime(date));
        } else {
            time = ConsoleInput.prompt("Reservation time, 24-hour (HH:mm, e.g. 18:30): ",
                    Validator.reservationTime(date));
        }
    }

    private void askGuests() {
        int max = BookingRules.maxGuests();
        String label = "Number of guests (1-" + max + ")";
        if (updating()) {
            String now = String.valueOf(current.getGuests());
            guests = ConsoleInput.promptOrKeep(label, now, now, Validator.intInRange(1, max));
        } else {
            guests = ConsoleInput.prompt(label + ": ", Validator.intInRange(1, max));
        }
    }

    private void askTableType() {
        printTableTypes();
        if (updating()) {
            tableType = ConsoleInput.promptOrKeep("Choose table type", current.getTableType().getLabel(),
                    String.valueOf(current.getTableType().ordinal() + 1), Validator.tableTypeFor(guests));
        } else {
            tableType = ConsoleInput.prompt("Choose table type: ", Validator.tableTypeFor(guests));
        }
    }

    private void askTables() {
        int min = BookingRules.minTablesFor(guests, tableType);
        String label = "Number of tables (" + min + "-" + BookingRules.MAX_TABLES + ")";
        if (updating()) {
            String now = String.valueOf(current.getNumTables());
            numTables = ConsoleInput.promptOrKeep(label, now, now, Validator.tableCountFor(guests, tableType));
        } else {
            numTables = ConsoleInput.prompt(label + ": ", Validator.tableCountFor(guests, tableType));
        }
    }

    private void printTableTypes() {
        TableType[] types = TableType.values();
        System.out.println("  Table types:");
        for (int i = 0; i < types.length; i++) {
            TableType t = types[i];
            System.out.printf("   [%d] %-8s %d seats per table   PHP %,.2f per table   (you need %d)%n",
                    i + 1, t.getLabel(), t.getSeats(), t.getPricePerTable(), BookingRules.minTablesFor(guests, t));
        }
    }
}
