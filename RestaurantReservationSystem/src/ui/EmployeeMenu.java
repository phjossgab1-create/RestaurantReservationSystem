package ui;

import enums.PaymentStatus;
import enums.ReservationStatus;
import exception.InvalidInputException;
import exception.ReservationSystemException;
import model.Customer;
import model.Employee;
import model.Reservation;
import service.CustomerService;
import service.ReservationService;
import strategy.sort.SortByDate;
import strategy.sort.SortByPaymentStatus;
import util.Validator;

import java.util.List;
import java.util.function.Predicate;

/**
 * Shared actions for Staff and Manager (view, search, sort, filter).
 * Written once here and inherited by StaffMenu and ManagerMenu.
 */
public abstract class EmployeeMenu extends Menu {

    protected final ReservationService reservationService = new ReservationService();
    protected final CustomerService customerService = new CustomerService();

    protected EmployeeMenu(Employee employee) {
        super(employee);
    }

    protected void viewCustomers() throws ReservationSystemException {
        System.out.println("--- Customer Records ---");
        ReservationPrinter.printCustomers(customerService.getAllCustomers());
    }

    protected void viewAllReservations() throws ReservationSystemException {
        System.out.println("--- All Active Reservations ---");
        printTable(reservationService.getAllReservations());
    }

    /** Shows every active reservation as a numbered list; the user picks one by number. */
    protected void searchReservation() throws ReservationSystemException {
        System.out.println("--- Search Reservation ---");
        final List<Reservation> list = reservationsWhere(r -> true);
        if (list.isEmpty()) {
            System.out.println("  There are no reservations yet.");
            return;
        }
        Reservation r = pickReservation(list, found -> { });
        System.out.println();
        ReservationPrinter.printDetails(r, reservationService.getCustomerName(r.getCustomerId()));
    }

    /** A rule the chosen reservation must pass, e.g. Reservation::ensureCanCheckIn. */
    @FunctionalInterface
    protected interface ReservationCheck {
        void check(Reservation reservation) throws ReservationSystemException;
    }

    /** Active reservations that match the condition, earliest date first. */
    protected List<Reservation> reservationsWhere(Predicate<Reservation> condition)
            throws ReservationSystemException {
        return reservationService.filterReservations(condition);
    }

    /**
     * Prints the reservations as a numbered list (name, email, reservation ID)
     * and asks for a number. If the chosen one fails the check, the reason is
     * shown and the user picks again.
     */
    protected Reservation pickReservation(final List<Reservation> list, final ReservationCheck check) {
        printPickList(list);
        return ConsoleInput.prompt("Choose a reservation (1-" + list.size() + "): ", input -> {
            Reservation chosen = list.get(Validator.intInRange(1, list.size()).parse(input) - 1);
            try {
                check.check(chosen);
            } catch (InvalidInputException e) {
                throw e;
            } catch (ReservationSystemException e) {
                throw new InvalidInputException(e.getMessage());
            }
            return chosen;
        });
    }

    private void printPickList(List<Reservation> list) {
        String format = "  %-5s %-24s %-30s %-10s %-10s %s%n";
        System.out.printf(format, "No.", "Customer name", "Email", "Res. ID", "Date", "Status");
        System.out.println("  " + line(95));
        for (int i = 0; i < list.size(); i++) {
            Reservation r = list.get(i);
            Customer c = reservationService.getCustomer(r.getCustomerId());
            String name = c == null ? "(unknown)" : c.getFullName();
            String email = c == null ? "-" : c.getEmail();
            System.out.printf(format, "[" + (i + 1) + "]", name, email, r.getReservationId(),
                    r.getDate(), r.getStatus().getLabel());
        }
        System.out.println();
    }

    private static String line(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    protected void sortReservations() throws ReservationSystemException {
        System.out.println("--- Sort Reservations ---");
        System.out.println("  [1] By date");
        System.out.println("  [2] By payment status (Paid, Partially Paid, Unpaid)");
        int choice = ConsoleInput.prompt("Sort by: ", Validator.intInRange(1, 2));

        // Strategy pattern: pick the sorting algorithm at runtime.
        if (choice == 1) {
            reservationService.setSortStrategy(new SortByDate());
        } else {
            reservationService.setSortStrategy(new SortByPaymentStatus());
        }
        List<Reservation> sorted = reservationService.getSortedReservations();
        System.out.println("\n  Sorted by: " + reservationService.getSortStrategy().getName());
        printTable(sorted);
    }

    protected void filterReservations() throws ReservationSystemException {
        System.out.println("--- Filter Reservations ---");
        System.out.println("  [1] Pending update");
        System.out.println("  [2] Confirmed");
        System.out.println("  [3] Checked-In");
        System.out.println("  [4] Checked-Out");
        System.out.println("  [5] Cancel requested");
        System.out.println("  [6] By payment status");
        final PaymentStatus[] statuses = PaymentStatus.values();
        final int[] choice = new int[1];
        final PaymentStatus[] wanted = new PaymentStatus[1];
        StepForm.run(
                () -> choice[0] = ConsoleInput.prompt("Filter: ", Validator.intInRange(1, 6)),
                () -> {
                    if (choice[0] != 6) {
                        return; // only option 6 has a second question
                    }
                    for (int i = 0; i < statuses.length; i++) {
                        System.out.println("   [" + (i + 1) + "] " + statuses[i].getLabel());
                    }
                    wanted[0] = statuses[ConsoleInput.prompt("Payment status: ",
                            Validator.intInRange(1, statuses.length)) - 1];
                });

        Predicate<Reservation> condition;
        String label;
        switch (choice[0]) {
            case 1:
                condition = byStatus(ReservationStatus.PENDING_UPDATE);
                label = "Pending Update";
                break;
            case 2:
                condition = byStatus(ReservationStatus.CONFIRMED);
                label = "Confirmed";
                break;
            case 3:
                condition = byStatus(ReservationStatus.CHECKED_IN);
                label = "Checked-In";
                break;
            case 4:
                condition = byStatus(ReservationStatus.CHECKED_OUT);
                label = "Checked-Out";
                break;
            case 5:
                condition = byStatus(ReservationStatus.CANCEL_REQUESTED);
                label = "Cancel Requested";
                break;
            default:
                final PaymentStatus status = wanted[0];
                condition = r -> r.getPaymentStatus() == status;
                label = status.getLabel();
        }
        System.out.println("\n  Showing: " + label);
        printTable(reservationService.filterReservations(condition));
    }

    protected void printTable(List<Reservation> reservations) {
        ReservationPrinter.printTable(reservations, reservationService::getCustomerName);
    }

    private static Predicate<Reservation> byStatus(final ReservationStatus status) {
        return r -> r.getStatus() == status;
    }
}
