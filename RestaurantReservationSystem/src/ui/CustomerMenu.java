package ui;

import exception.InvalidInputException;
import exception.ReservationSystemException;
import model.Customer;
import model.PendingChange;
import model.Reservation;
import service.ReservationService;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

/** Everything a customer can do after logging in. */
public class CustomerMenu extends Menu {

    private final Customer customer;
    private final ReservationService reservationService = new ReservationService();

    public CustomerMenu(Customer customer) {
        super(customer);
        this.customer = customer;
    }

    @Override
    protected void printOptions() {
        System.out.println("  [1] Make a reservation");
        System.out.println("  [2] View my reservations");
        System.out.println("  [3] Update a reservation");
        System.out.println("  [4] Cancel a reservation");
        System.out.println("  [5] View payment status");
        System.out.println("  [6] View my profile");
    }

    @Override
    protected int getOptionCount() {
        return 6;
    }

    @Override
    protected void handleOption(int choice) throws ReservationSystemException {
        switch (choice) {
            case 1:
                makeReservation();
                break;
            case 2:
                viewMyReservations();
                break;
            case 3:
                updateReservation();
                break;
            case 4:
                cancelReservation();
                break;
            case 5:
                viewPaymentStatus();
                break;
            case 6:
                viewProfile();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void makeReservation() throws ReservationSystemException {
        System.out.println("--- Make a Reservation ---");
        ReservationForm form = new ReservationForm();
        StepForm.run(form.steps());
        PendingChange details = form.toChange();

        Reservation r = reservationService.makeReservation(details.getDate(), details.getTime(),
                details.getGuests(), details.getTableType(), details.getNumTables());
        System.out.println("\n  Reservation confirmed!");
        ReservationPrinter.printDetails(r, customer.getFullName());
        System.out.println("\n  Please keep your Reservation ID: " + r.getReservationId());
    }

    private void viewMyReservations() throws ReservationSystemException {
        System.out.println("--- My Reservations ---");
        final List<Reservation> mine = reservationService.getMyReservations();
        ReservationPrinter.printTable(mine, id -> customer.getFullName());
        if (mine.isEmpty()) {
            return;
        }
        System.out.println("\n  For full details type the 6 digits of a reservation ID, or press Enter to finish.");
        Reservation chosen = ConsoleInput.prompt("Reservation ID: RN-", input -> {
            if (input.isEmpty()) {
                return null;
            }
            String id = Validator.reservationNumber(input);
            for (Reservation r : mine) {
                if (r.getReservationId().equals(id)) {
                    return r;
                }
            }
            throw new InvalidInputException("You have no reservation with ID " + id + ".");
        });
        if (chosen != null) {
            System.out.println();
            ReservationPrinter.printDetails(chosen, customer.getFullName());
        }
    }

    private void updateReservation() throws ReservationSystemException {
        System.out.println("--- Update a Reservation ---");
        final Reservation[] chosen = new Reservation[1];
        final boolean[] submit = new boolean[1];
        final ReservationForm form = new ReservationForm();

        List<StepForm.Step> steps = new ArrayList<>();
        steps.add(() -> {
            chosen[0] = promptReservation(id -> {
                Reservation found = reservationService.getMyReservation(id);
                found.ensureCanRequestUpdate();
                return found;
            });
            ReservationPrinter.printDetails(chosen[0], customer.getFullName());
            System.out.println();
            form.setCurrent(chosen[0]);
        });
        steps.addAll(form.steps());
        steps.add(() -> {
            PendingChange change = form.toChange();
            if (change.isSameAs(chosen[0])) {
                System.out.println("\n  Nothing was changed.");
                submit[0] = false;
                return;
            }
            double fee = reservationService.previewUpdateCharge(chosen[0], change);
            if (fee > 0) {
                System.out.printf("%n  Note: changing the date or time adds a PHP %,.2f reschedule fee once approved.%n", fee);
            }
            submit[0] = ConsoleInput.confirm("Submit this update request?");
        });
        StepForm.run(steps);

        if (!submit[0]) {
            System.out.println("  Your reservation was not changed.");
            return;
        }
        reservationService.requestUpdate(chosen[0].getReservationId(), form.toChange());
        System.out.println("\n  Update request sent. Status is now 'Pending Update' until staff approve it.");
    }

    private void cancelReservation() throws ReservationSystemException {
        System.out.println("--- Cancel a Reservation ---");
        final Reservation[] chosen = new Reservation[1];
        final boolean[] confirmed = new boolean[1];

        StepForm.run(
                () -> {
                    chosen[0] = promptReservation(id -> {
                        Reservation found = reservationService.getMyReservation(id);
                        found.ensureCanRequestCancel();
                        return found;
                    });
                    ReservationPrinter.printDetails(chosen[0], customer.getFullName());
                    double fee = reservationService.previewCancellationCharge(chosen[0]);
                    System.out.printf("%n  A cancellation fee of PHP %,.2f applies once the manager approves.%n", fee);
                },
                () -> confirmed[0] = ConsoleInput.confirm("Request cancellation?"));

        if (!confirmed[0]) {
            System.out.println("  Your reservation was not changed.");
            return;
        }
        reservationService.requestCancel(chosen[0].getReservationId());
        System.out.println("\n  Cancellation request sent to the manager for approval.");
    }

    private void viewPaymentStatus() throws ReservationSystemException {
        System.out.println("--- My Payment Status ---");
        ReservationPrinter.printPaymentSummary(reservationService.getMyReservations());
    }

    private void viewProfile() {
        System.out.println("--- My Profile ---");
        System.out.println("  Customer ID : " + customer.getUserId());
        System.out.println("  First name  : " + customer.getFirstName());
        System.out.println("  Last name   : " + customer.getLastName());
        if (customer.getDuplicateNumber() > 0) {
            System.out.println("  Saved as    : " + customer.getFullName());
        }
        System.out.println("  Contact     : " + customer.getContactNumber());
        System.out.println("  Email       : " + customer.getEmail());
    }
}
