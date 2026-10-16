package ui;

import enums.ReservationStatus;
import exception.ReservationSystemException;
import model.Manager;
import model.Reservation;
import util.Validator;

import java.util.List;

/** Overall record management and cancellation approval for the manager. */
public class ManagerMenu extends EmployeeMenu {

    public ManagerMenu(Manager manager) {
        super(manager);
    }

    @Override
    protected void printOptions() {
        System.out.println("  [1] View all customer records");
        System.out.println("  [2] View all reservations");
        System.out.println("  [3] Search reservation");
        System.out.println("  [4] Sort reservations");
        System.out.println("  [5] Filter reservations");
        System.out.println("  [6] View checked-in / checked-out log");
        System.out.println("  [7] View cancelled reservations");
        System.out.println("  [8] View cancellation requests");
        System.out.println("  [9] Approve or reject a cancellation");
    }

    @Override
    protected int getOptionCount() {
        return 9;
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
                sortReservations();
                break;
            case 5:
                filterReservations();
                break;
            case 6:
                viewStatusLog();
                break;
            case 7:
                viewCancelled();
                break;
            case 8:
                viewCancelRequests();
                break;
            case 9:
                reviewCancellation();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void viewStatusLog() throws ReservationSystemException {
        System.out.println("--- Checked-In / Checked-Out Log (STATUS.txt) ---");
        ReservationPrinter.printStatusLog(reservationService.getStatusLog());
    }

    private void viewCancelled() throws ReservationSystemException {
        System.out.println("--- Cancelled Reservations (CANCELLED.txt) ---");
        List<Reservation> cancelled = reservationService.getCancelledReservations();
        printTable(cancelled);
        if (!cancelled.isEmpty()) {
            System.out.println();
            ReservationPrinter.printPaymentSummary(cancelled);
        }
    }

    private void viewCancelRequests() throws ReservationSystemException {
        System.out.println("--- Cancellation Requests ---");
        printTable(reservationService.getCancelRequests());
    }

    private void reviewCancellation() throws ReservationSystemException {
        System.out.println("--- Approve / Reject Cancellation ---");
        final Reservation[] chosen = new Reservation[1];
        final int[] decision = new int[1];
        final List<Reservation> list = reservationsWhere(r -> r.getStatus() == ReservationStatus.CANCEL_REQUESTED);
        if (list.isEmpty()) {
            System.out.println("  There are no cancellation requests.");
            return;
        }
        StepForm.run(
                () -> {
                    chosen[0] = pickReservation(list, Reservation::ensureCancelRequested);
                    ReservationPrinter.printDetails(chosen[0],
                            reservationService.getCustomerName(chosen[0].getCustomerId()));
                    System.out.println("\n  [1] Approve   [2] Reject");
                },
                () -> decision[0] = ConsoleInput.prompt("Decision: ", Validator.intInRange(1, 2)));

        if (decision[0] == 1) {
            double fee = reservationService.approveCancel(chosen[0].getReservationId());
            System.out.printf("%n  Cancellation approved. Fee charged: PHP %,.2f%n", fee);
            System.out.println("  The reservation was moved to CANCELLED.txt.");
        } else {
            reservationService.rejectCancel(chosen[0].getReservationId());
            System.out.println("\n  Cancellation rejected. The reservation is Confirmed again.");
        }
    }
}
