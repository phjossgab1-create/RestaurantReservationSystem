package ui;

import enums.ReservationStatus;
import exception.ReservationSystemException;
import model.Reservation;
import model.Staff;
import util.Validator;

import java.util.List;

/** Day-to-day reservation handling for staff. */
public class StaffMenu extends EmployeeMenu {

    public StaffMenu(Staff staff) {
        super(staff);
    }

    @Override
    protected void printOptions() {
        System.out.println("  [1]  View customer information");
        System.out.println("  [2]  View all reservations");
        System.out.println("  [3]  Search / verify reservation");
        System.out.println("  [4]  View pending update requests");
        System.out.println("  [5]  Approve or reject an update request");
        System.out.println("  [6]  Check in customer");
        System.out.println("  [7]  Check out customer");
        System.out.println("  [8]  Record payment");
        System.out.println("  [9]  Sort reservations");
        System.out.println("  [10] Filter reservations");
    }

    @Override
    protected int getOptionCount() {
        return 10;
    }

    @Override
    protected void handleOption(int choice) throws ReservationSystemException {
        switch (choice) {
            case 1:
                viewCustomers();
                break;
            case 2:
                viewAllReservations();
                break;
            case 3:
                searchReservation();
                break;
            case 4:
                viewPendingUpdates();
                break;
            case 5:
                reviewUpdateRequest();
                break;
            case 6:
                checkIn();
                break;
            case 7:
                checkOut();
                break;
            case 8:
                recordPayment();
                break;
            case 9:
                sortReservations();
                break;
            case 10:
                filterReservations();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void viewPendingUpdates() throws ReservationSystemException {
        System.out.println("--- Pending Update Requests ---");
        List<Reservation> pending = reservationService.getPendingUpdates();
        printTable(pending);
        for (Reservation r : pending) {
            System.out.println();
            ReservationPrinter.printDetails(r, reservationService.getCustomerName(r.getCustomerId()));
        }
    }

    private void reviewUpdateRequest() throws ReservationSystemException {
        System.out.println("--- Approve / Reject Update ---");
        final Reservation[] chosen = new Reservation[1];
        final int[] decision = new int[1];
        final List<Reservation> list = reservationsWhere(r -> r.getStatus() == ReservationStatus.PENDING_UPDATE);
        if (list.isEmpty()) {
            System.out.println("  There are no update requests waiting for approval.");
            return;
        }
        StepForm.run(
                () -> {
                    chosen[0] = pickReservation(list, Reservation::ensurePendingUpdate);
                    ReservationPrinter.printDetails(chosen[0],
                            reservationService.getCustomerName(chosen[0].getCustomerId()));
                    System.out.println("\n  [1] Approve   [2] Reject");
                },
                () -> decision[0] = ConsoleInput.prompt("Decision: ", Validator.intInRange(1, 2)));

        if (decision[0] == 1) {
            double fee = reservationService.approveUpdate(chosen[0].getReservationId());
            System.out.println("\n  Update approved and saved.");
            if (fee > 0) {
                System.out.printf("  A reschedule fee of PHP %,.2f was added.%n", fee);
            }
        } else {
            reservationService.rejectUpdate(chosen[0].getReservationId());
            System.out.println("\n  Update rejected. The original details are kept.");
        }
    }

    private void checkIn() throws ReservationSystemException {
        System.out.println("--- Check In Customer ---");
        final Reservation[] chosen = new Reservation[1];
        final boolean[] confirmed = new boolean[1];
        final List<Reservation> list = reservationsWhere(r -> r.getStatus() == ReservationStatus.CONFIRMED);
        if (list.isEmpty()) {
            System.out.println("  There are no confirmed reservations waiting for check-in.");
            return;
        }
        StepForm.run(
                () -> {
                    chosen[0] = pickReservation(list, Reservation::ensureCanCheckIn);
                    ReservationPrinter.printDetails(chosen[0],
                            reservationService.getCustomerName(chosen[0].getCustomerId()));
                },
                () -> confirmed[0] = ConsoleInput.confirm("\nCheck in this customer?"));

        if (confirmed[0]) {
            reservationService.checkIn(chosen[0].getReservationId());
            System.out.println("  Customer checked in. Recorded in STATUS.txt.");
        } else {
            System.out.println("  Not checked in.");
        }
    }

    private void checkOut() throws ReservationSystemException {
        System.out.println("--- Check Out Customer ---");
        final Reservation[] chosen = new Reservation[1];
        final boolean[] confirmed = new boolean[1];
        final List<Reservation> list = reservationsWhere(r -> r.getStatus() == ReservationStatus.CHECKED_IN);
        if (list.isEmpty()) {
            System.out.println("  No customers are checked in right now.");
            return;
        }
        StepForm.run(
                () -> {
                    chosen[0] = pickReservation(list, Reservation::ensureCanCheckOut);
                    ReservationPrinter.printDetails(chosen[0],
                            reservationService.getCustomerName(chosen[0].getCustomerId()));
                },
                () -> confirmed[0] = ConsoleInput.confirm("\nCheck out this customer?"));

        if (confirmed[0]) {
            reservationService.checkOut(chosen[0].getReservationId());
            System.out.println("  Customer checked out. Recorded in STATUS.txt.");
        } else {
            System.out.println("  Not checked out.");
        }
    }

    private void recordPayment() throws ReservationSystemException {
        System.out.println("--- Record Payment ---");
        final Reservation[] chosen = new Reservation[1];
        final double[] amount = new double[1];
        final List<Reservation> list = reservationsWhere(r -> r.getBalance() > 0.005);
        if (list.isEmpty()) {
            System.out.println("  There are no reservations with an unpaid balance.");
            return;
        }
        StepForm.run(
                () -> {
                    chosen[0] = pickReservation(list, Reservation::ensureCanPay);
                    ReservationPrinter.printMoney(chosen[0]);
                },
                () -> amount[0] = ConsoleInput.prompt("\nAmount received (PHP): ",
                        Validator.paymentAmount(chosen[0].getBalance())));

        Reservation updated = reservationService.recordPayment(chosen[0].getReservationId(), amount[0]);
        System.out.println("\n  Payment recorded.");
        ReservationPrinter.printMoney(updated);
    }
}
