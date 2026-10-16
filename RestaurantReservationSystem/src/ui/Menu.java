package ui;

import exception.GoBackException;
import exception.InvalidInputException;
import exception.ReservationSystemException;
import model.Reservation;
import model.User;
import service.SessionManager;
import util.Validator;

/**
 * Abstract menu (Template Method style).
 * run() holds the loop, the back (\) handling and the error handling that every menu shares.
 * Subclasses only fill in printOptions(), getOptionCount() and handleOption()  (Polymorphism).
 */
public abstract class Menu {

    private final User user;

    protected Menu(User user) {
        this.user = user;
    }

    protected User getUser() {
        return user;
    }

    public final void run() {
        String menuName = user.getRole().getLabel().toUpperCase() + " MENU";
        while (true) {
            System.out.println();
            System.out.println("==================================================");
            System.out.println("  " + menuName + "  -  " + user.getFullName());
            System.out.println("==================================================");
            printOptions();
            System.out.println("  [0] Logout");
            System.out.println("  (Type \\ at any prompt to go back)");

            int choice;
            try {
                choice = ConsoleInput.prompt("Choose an option: ", Validator.intInRange(0, getOptionCount()));
            } catch (GoBackException e) {
                break; // \ on this menu goes back to the main menu
            }
            if (choice == 0) {
                break;
            }
            System.out.println();
            try {
                handleOption(choice);
                ConsoleInput.pause();
            } catch (GoBackException e) {
                System.out.println("  Back to the " + menuName.toLowerCase() + ".");
            } catch (ReservationSystemException e) {
                System.out.println("\n  [Error] " + e.getMessage());
                ConsoleInput.pause();
            }
        }
        SessionManager.getInstance().logout();
        System.out.println("You have been logged out.");
    }

    protected abstract void printOptions();

    protected abstract int getOptionCount();

    protected abstract void handleOption(int choice) throws ReservationSystemException;

    /** Finds a reservation and checks it can be used for the current action. */
    @FunctionalInterface
    protected interface ReservationLookup {
        Reservation find(String reservationId) throws ReservationSystemException;
    }

    /**
     * Asks for a reservation ID (6 digits only; "RN-" is shown and added automatically).
     * If the ID doesn't exist or can't be used for this action, the reason is shown
     * and the user types another ID.
     */
    protected Reservation promptReservation(final ReservationLookup lookup) {
        return ConsoleInput.prompt("Reservation ID: RN-", input -> {
            String id = Validator.reservationNumber(input);
            try {
                return lookup.find(id);
            } catch (InvalidInputException e) {
                throw e;
            } catch (ReservationSystemException e) {
                throw new InvalidInputException(e.getMessage());
            }
        });
    }
}
