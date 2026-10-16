package model;

import enums.PaymentStatus;
import enums.ReservationStatus;
import enums.TableType;
import exception.InvalidPaymentException;
import exception.InvalidStatusTransitionException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A restaurant reservation. Saved in RESERVE.txt (or CANCELLED.txt once cancelled).
 *
 * Encapsulation: no public setters for status or money. The only way to change
 * them is through methods like checkIn() or recordPayment(), which enforce the rules:
 *
 *   CONFIRMED --customer edits--> PENDING_UPDATE --staff approves/rejects--> CONFIRMED
 *   CONFIRMED --customer cancels--> CANCEL_REQUESTED --manager approves--> CANCELLED
 *                                                    --manager rejects--> CONFIRMED
 *   CONFIRMED --staff--> CHECKED_IN --staff (fully paid)--> CHECKED_OUT
 */
public class Reservation implements Storable {

    private static final double EPSILON = 0.005;
    private static final String NONE = "-";

    private final String reservationId;
    private final String customerId;
    private LocalDate date;
    private LocalTime time;
    private int guests;
    private TableType tableType;
    private int numTables;
    private ReservationStatus status;
    private double amountPaid;
    private double extraCharges;
    private PendingChange pendingChange;

    /** Creates a brand-new reservation. It starts as CONFIRMED and unpaid. */
    public Reservation(String reservationId, String customerId, LocalDate date, LocalTime time,
                       int guests, TableType tableType, int numTables) {
        this(reservationId, customerId, date, time, guests, tableType, numTables,
                ReservationStatus.CONFIRMED, 0.0, 0.0, null);
    }

    /** Full constructor, used when loading from a file. */
    private Reservation(String reservationId, String customerId, LocalDate date, LocalTime time,
                        int guests, TableType tableType, int numTables, ReservationStatus status,
                        double amountPaid, double extraCharges, PendingChange pendingChange) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.tableType = tableType;
        this.numTables = numTables;
        this.status = status;
        this.amountPaid = amountPaid;
        this.extraCharges = extraCharges;
        this.pendingChange = pendingChange;
    }

    // ---------------------------------------------------------------- getters

    public String getReservationId() {
        return reservationId;
    }

    public String getCustomerId() {
        return customerId;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getExtraCharges() {
        return extraCharges;
    }

    public PendingChange getPendingChange() {
        return pendingChange;
    }

    // ------------------------------------------------- computed money values

    /** Table price x number of tables. */
    public double getBaseAmount() {
        return tableType.getPricePerTable() * numTables;
    }

    /**
     * Base amount plus any reschedule/cancellation fees.
     * A cancelled reservation no longer owes the table cost, only its fees.
     */
    public double getTotalAmount() {
        if (status == ReservationStatus.CANCELLED) {
            return extraCharges;
        }
        return getBaseAmount() + extraCharges;
    }

    /** Money to return if the customer paid more than a cancelled reservation now costs. */
    public double getRefundDue() {
        return Math.max(0.0, amountPaid - getTotalAmount());
    }

    /** What the customer still owes. Calculated, never stored, so it can't go out of sync. */
    public double getBalance() {
        return Math.max(0.0, getTotalAmount() - amountPaid);
    }

    /** Payment status is derived from the amounts, so it is always correct. */
    public PaymentStatus getPaymentStatus() {
        if (amountPaid < EPSILON) {
            return PaymentStatus.UNPAID;
        }
        if (getBalance() < EPSILON) {
            return PaymentStatus.FULLY_PAID;
        }
        return PaymentStatus.PARTIALLY_PAID;
    }

    // ------------------------------------------------------ business actions

    // ------------------------------------------------------------- checks
    // These "ensure" methods let the menus check a reservation right after
    // its ID is typed, so the user can type a different ID on the spot.

    public void ensureCanPay() throws InvalidStatusTransitionException, InvalidPaymentException {
        if (status == ReservationStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot record a payment: reservation " + reservationId + " is cancelled.");
        }
        if (getBalance() < EPSILON) {
            throw new InvalidPaymentException("Reservation " + reservationId + " is already fully paid.");
        }
    }

    public void ensureCanCheckIn() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.CONFIRMED, "check in");
    }

    public void ensureCanCheckOut() throws InvalidStatusTransitionException, InvalidPaymentException {
        requireStatus(ReservationStatus.CHECKED_IN, "check out");
        if (getBalance() >= EPSILON) {
            throw new InvalidPaymentException(String.format(
                    "Cannot check out %s: the customer still owes PHP %,.2f. Record the payment first.",
                    reservationId, getBalance()));
        }
    }

    public void ensurePendingUpdate() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.PENDING_UPDATE, "review an update");
    }

    public void ensureCancelRequested() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.CANCEL_REQUESTED, "review a cancellation");
    }

    public void recordPayment(double amount)
            throws InvalidPaymentException, InvalidStatusTransitionException {
        ensureCanPay();
        if (amount <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero.");
        }
        if (amount > getBalance() + EPSILON) {
            throw new InvalidPaymentException(String.format(
                    "Payment of PHP %,.2f is more than the remaining balance of PHP %,.2f.",
                    amount, getBalance()));
        }
        amountPaid += amount;
    }

    public void addCharge(double charge) {
        if (charge < 0) {
            throw new IllegalArgumentException("Charge cannot be negative.");
        }
        extraCharges += charge;
    }

    /** Lets the menu check early, before asking the customer for new details. */
    public void ensureCanRequestUpdate() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.CONFIRMED, "request an update");
    }

    public void ensureCanRequestCancel() throws InvalidStatusTransitionException {
        if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.PENDING_UPDATE) {
            throw new InvalidStatusTransitionException("Cannot request a cancellation: reservation "
                    + reservationId + " is " + status.getLabel() + ".");
        }
    }

    public void requestUpdate(PendingChange change) throws InvalidStatusTransitionException {
        ensureCanRequestUpdate();
        this.pendingChange = change;
        this.status = ReservationStatus.PENDING_UPDATE;
    }

    public void applyPendingChange() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.PENDING_UPDATE, "approve an update");
        this.date = pendingChange.getDate();
        this.time = pendingChange.getTime();
        this.guests = pendingChange.getGuests();
        this.tableType = pendingChange.getTableType();
        this.numTables = pendingChange.getNumTables();
        this.pendingChange = null;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void rejectPendingChange() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.PENDING_UPDATE, "reject an update");
        this.pendingChange = null;
        this.status = ReservationStatus.CONFIRMED;
    }

    /** A customer may cancel a confirmed reservation, or one that is waiting for an update. */
    public void requestCancel() throws InvalidStatusTransitionException {
        ensureCanRequestCancel();
        this.pendingChange = null;
        this.status = ReservationStatus.CANCEL_REQUESTED;
    }

    public void rejectCancel() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.CANCEL_REQUESTED, "reject a cancellation");
        this.status = ReservationStatus.CONFIRMED;
    }

    public void markCancelled() throws InvalidStatusTransitionException {
        requireStatus(ReservationStatus.CANCEL_REQUESTED, "approve a cancellation");
        this.status = ReservationStatus.CANCELLED;
    }

    public void checkIn() throws InvalidStatusTransitionException {
        ensureCanCheckIn();
        this.status = ReservationStatus.CHECKED_IN;
    }

    public void checkOut() throws InvalidStatusTransitionException, InvalidPaymentException {
        ensureCanCheckOut();
        this.status = ReservationStatus.CHECKED_OUT;
    }

    private void requireStatus(ReservationStatus expected, String action)
            throws InvalidStatusTransitionException {
        if (status != expected) {
            throw new InvalidStatusTransitionException("Cannot " + action + ": reservation "
                    + reservationId + " is " + status.getLabel()
                    + " (it must be " + expected.getLabel() + ").");
        }
    }

    // ----------------------------------------------------------- file format

    /**
     * Format (16 fields):
     * id|customerId|date|time|guests|tableType|numTables|status|paymentStatus|amountPaid|extraCharges|
     * pendingDate|pendingTime|pendingGuests|pendingTableType|pendingNumTables
     * The pending fields are "-" when there is no pending update.
     */
    @Override
    public String toFileString() {
        String pending;
        if (pendingChange == null) {
            pending = String.join(DELIMITER, NONE, NONE, NONE, NONE, NONE);
        } else {
            pending = String.join(DELIMITER,
                    pendingChange.getDate().toString(),
                    pendingChange.getTime().toString(),
                    String.valueOf(pendingChange.getGuests()),
                    pendingChange.getTableType().name(),
                    String.valueOf(pendingChange.getNumTables()));
        }
        return String.join(DELIMITER,
                reservationId,
                customerId,
                date.toString(),
                time.toString(),
                String.valueOf(guests),
                tableType.name(),
                String.valueOf(numTables),
                status.name(),
                getPaymentStatus().name(),
                String.format("%.2f", amountPaid),
                String.format("%.2f", extraCharges),
                pending);
    }

    /** Rebuilds a Reservation from one line of RESERVE.txt or CANCELLED.txt. */
    public static Reservation fromFileString(String line) {
        String[] p = line.split(SPLIT_REGEX, -1);
        if (p.length != 16) {
            throw new IllegalArgumentException("expected 16 fields but found " + p.length);
        }
        PendingChange pending = null;
        if (!NONE.equals(p[11])) {
            pending = new PendingChange(
                    LocalDate.parse(p[11]),
                    LocalTime.parse(p[12]),
                    Integer.parseInt(p[13]),
                    TableType.valueOf(p[14]),
                    Integer.parseInt(p[15]));
        }
        // p[8] (payment status) is only written for readability; it is recalculated from the amounts.
        return new Reservation(
                p[0],
                p[1],
                LocalDate.parse(p[2]),
                LocalTime.parse(p[3]),
                Integer.parseInt(p[4]),
                TableType.valueOf(p[5]),
                Integer.parseInt(p[6]),
                ReservationStatus.valueOf(p[7]),
                Double.parseDouble(p[9]),
                Double.parseDouble(p[10]),
                pending);
    }
}
