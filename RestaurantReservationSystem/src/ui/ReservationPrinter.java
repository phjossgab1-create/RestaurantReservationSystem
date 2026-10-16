package ui;

import enums.ReservationStatus;
import model.Customer;
import model.PendingChange;
import model.Reservation;

import java.util.List;
import java.util.function.Function;

/** Prints reservations and customers as neat console tables. */
public final class ReservationPrinter {

    private static final String RES_FORMAT = "%-10s %-18s %-10s %-5s %6s %-7s %4s %-16s %-14s%n";
    private static final String CUST_FORMAT = "%-12s %-18s %-22s %-12s %s%n";

    private ReservationPrinter() {
    }

    /** nameLookup turns a Customer ID into a customer name. */
    public static void printTable(List<Reservation> reservations, Function<String, String> nameLookup) {
        if (reservations.isEmpty()) {
            System.out.println("  No reservations to show.");
            return;
        }
        System.out.printf(RES_FORMAT, "Res. ID", "Customer", "Date", "Time", "Guests",
                "Table", "Qty", "Status", "Payment");
        System.out.println(line(100));
        for (Reservation r : reservations) {
            System.out.printf(RES_FORMAT,
                    r.getReservationId(),
                    shorten(nameLookup.apply(r.getCustomerId()), 18),
                    r.getDate(),
                    r.getTime(),
                    r.getGuests(),
                    r.getTableType().getLabel(),
                    r.getNumTables(),
                    r.getStatus().getLabel(),
                    r.getPaymentStatus().getLabel());
        }
        System.out.println("  Total: " + reservations.size() + " reservation(s)");
    }

    public static void printDetails(Reservation r, String customerName) {
        System.out.println("  Reservation ID : " + r.getReservationId());
        System.out.println("  Customer       : " + customerName + " (" + r.getCustomerId() + ")");
        System.out.println("  Date / Time    : " + r.getDate() + " at " + r.getTime());
        System.out.println("  Guests         : " + r.getGuests());
        System.out.println("  Tables         : " + r.getNumTables() + " x " + r.getTableType().getLabel()
                + " (seats " + r.getTableType().getSeats() * r.getNumTables() + ")");
        System.out.println("  Status         : " + r.getStatus().getLabel());
        printMoney(r);
        PendingChange p = r.getPendingChange();
        if (p != null) {
            System.out.println("  -- Requested update (waiting for staff approval) --");
            System.out.println("     Date / Time : " + p.getDate() + " at " + p.getTime());
            System.out.println("     Guests      : " + p.getGuests());
            System.out.println("     Tables      : " + p.getNumTables() + " x " + p.getTableType().getLabel());
            System.out.println("     Reschedule  : " + (p.changesSchedule(r) ? "Yes (fee applies)" : "No"));
        }
    }

    public static void printMoney(Reservation r) {
        boolean cancelled = r.getStatus() == ReservationStatus.CANCELLED;
        System.out.printf("  Table cost     : PHP %,.2f%s%n", r.getBaseAmount(), cancelled ? " (waived, cancelled)" : "");
        System.out.printf("  Extra charges  : PHP %,.2f%n", r.getExtraCharges());
        System.out.printf("  Total amount   : PHP %,.2f%n", r.getTotalAmount());
        System.out.printf("  Amount paid    : PHP %,.2f%n", r.getAmountPaid());
        System.out.printf("  Balance        : PHP %,.2f%n", r.getBalance());
        if (r.getRefundDue() > 0.005) {
            System.out.printf("  Refund due     : PHP %,.2f%n", r.getRefundDue());
        }
        System.out.println("  Payment status : " + r.getPaymentStatus().getLabel());
    }

    public static void printPaymentSummary(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            System.out.println("  No reservations to show.");
            return;
        }
        String format = "%-10s %-10s %-16s %14s %14s %14s  %-14s%n";
        System.out.printf(format, "Res. ID", "Date", "Status", "Total", "Paid", "Balance", "Payment");
        System.out.println(line(100));
        for (Reservation r : reservations) {
            System.out.printf(format,
                    r.getReservationId(),
                    r.getDate(),
                    r.getStatus().getLabel(),
                    String.format("%,.2f", r.getTotalAmount()),
                    String.format("%,.2f", r.getAmountPaid()),
                    String.format("%,.2f", r.getBalance()),
                    r.getPaymentStatus().getLabel());
        }
    }

    public static void printCustomers(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("  No customers registered yet.");
            return;
        }
        System.out.printf(CUST_FORMAT, "Customer ID", "First name", "Last name", "Contact", "Email");
        System.out.println(line(100));
        for (Customer c : customers) {
            System.out.printf(CUST_FORMAT,
                    c.getUserId(),
                    shorten(firstNameShown(c), 18),
                    shorten(lastNameShown(c), 22),
                    c.getContactNumber(),
                    c.getEmail());
        }
        System.out.println("  Total: " + customers.size() + " customer(s)");
    }

    public static void printStatusLog(List<String[]> entries) {
        if (entries.isEmpty()) {
            System.out.println("  No check-ins or check-outs recorded yet.");
            return;
        }
        String format = "%-17s %-10s %-12s %-22s %s%n";
        System.out.printf(format, "Timestamp", "Res. ID", "Customer ID", "Customer", "Status");
        System.out.println(line(80));
        for (String[] e : entries) {
            System.out.printf(format, e[0], e[1], e[2], shorten(e[3], 22), e[4]);
        }
    }

    /** A row of dashes (written by hand because String.repeat() needs Java 11). */
    private static String line(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    /** The (1), (2) ... marker goes after the last name, or after the first name for old records without one. */
    private static String firstNameShown(Customer c) {
        return c.getLastName().isEmpty() ? c.getFullName() : c.getFirstName();
    }

    private static String lastNameShown(Customer c) {
        if (c.getLastName().isEmpty()) {
            return "-";
        }
        return c.getDuplicateNumber() > 0 ? c.getLastName() + "(" + c.getDuplicateNumber() + ")" : c.getLastName();
    }

    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 2) + "..";
    }
}
