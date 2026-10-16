package model;

import enums.TableType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The new details a customer asked for when updating a reservation.
 * It waits inside the Reservation until a staff member approves or rejects it.
 * Immutable: all fields are final.
 */
public class PendingChange {

    private final LocalDate date;
    private final LocalTime time;
    private final int guests;
    private final TableType tableType;
    private final int numTables;

    public PendingChange(LocalDate date, LocalTime time, int guests,
                         TableType tableType, int numTables) {
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.tableType = tableType;
        this.numTables = numTables;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getGuests() {
        return guests;
    }

    public TableType getTableType() {
        return tableType;
    }

    public int getNumTables() {
        return numTables;
    }

    /** True if the date or time differs from the reservation (this triggers the reschedule fee). */
    public boolean changesSchedule(Reservation reservation) {
        return !date.equals(reservation.getDate()) || !time.equals(reservation.getTime());
    }

    /** True if nothing is different from the current reservation. */
    public boolean isSameAs(Reservation reservation) {
        return !changesSchedule(reservation)
                && guests == reservation.getGuests()
                && tableType == reservation.getTableType()
                && numTables == reservation.getNumTables();
    }
}
