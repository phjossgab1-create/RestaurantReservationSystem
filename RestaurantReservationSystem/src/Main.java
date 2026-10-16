import exception.FileOperationException;
import exception.GoBackException;
import exception.InvalidInputException;
import exception.InvalidLoginException;
import exception.ReservationSystemException;
import model.Customer;
import model.User;
import repository.EmployeeRepository;
import repository.FileManager;
import service.CustomerService;
import service.SessionManager;
import ui.ConsoleInput;
import ui.StepForm;
import util.Validator;

/**
 * Entry point of the Restaurant Reservation System.
 * Run this file. Data is saved in the "data" folder of the project.
 *
 * Default employee accounts (created on first run, stored in data/EMPLOYEE.txt):
 *   Staff   -> username: staff     password: staff123
 *   Manager -> username: manager   password: manager123
 *
 * Type \ at any prompt to go back to the previous menu.
 */
public class Main {

    private static final CustomerService customerService = new CustomerService();
    private static final SessionManager session = SessionManager.getInstance();

    public static void main(String[] args) {
        try {
            FileManager.getInstance().initializeDataFiles();
            new EmployeeRepository().ensureDefaultAccounts();
        } catch (FileOperationException e) {
            System.out.println("Could not start: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("==================================================");
            System.out.println("        RESTAURANT RESERVATION SYSTEM");
            System.out.println("==================================================");
            System.out.println("  [1] Register as a new customer");
            System.out.println("  [2] Customer login");
            System.out.println("  [3] Staff / Manager login");
            System.out.println("  [0] Exit");
            System.out.println("  (Type \\ at any prompt to go back)");

            int choice;
            try {
                choice = ConsoleInput.prompt("Choose an option: ", Validator.intInRange(0, 3));
            } catch (GoBackException e) {
                System.out.println("  You are already at the main menu.");
                continue;
            }
            if (choice == 0) {
                System.out.println("Thank you for using the Restaurant Reservation System. Goodbye!");
                return;
            }
            try {
                switch (choice) {
                    case 1:
                        register();
                        break;
                    case 2:
                        openMenu(customerLogin());
                        break;
                    default:
                        openMenu(employeeLogin());
                }
            } catch (GoBackException e) {
                System.out.println("  Back to the main menu.");
            }
        }
    }

    /**
     * Each field is checked as it is typed; a wrong answer only repeats that field.
     * Typing \ goes back one question (from Full name, back to the main menu).
     */
    private static void register() {
        System.out.println("\n--- Customer Registration ---");
        final String[] answers = new String[4];
        StepForm.run(
                () -> answers[0] = ConsoleInput.prompt("First name (letters only): ", Validator::firstName),
                () -> answers[1] = ConsoleInput.prompt("Last name (letters only): ", Validator::lastName),
                () -> answers[2] = ConsoleInput.prompt("Contact number (09XXXXXXXXX): ",
                        customerService::validateNewContact),
                () -> answers[3] = ConsoleInput.prompt("Email address: ", customerService::validateNewEmail));
        try {
            Customer customer = customerService.register(answers[0], answers[1], answers[2], answers[3]);
            System.out.println("\n  Registration successful!");
            if (customer.getDuplicateNumber() > 0) {
                System.out.println("  The name " + answers[0] + " " + answers[1]
                        + " is already registered, so your name was saved as " + customer.getFullName() + ".");
            }
            System.out.println("  Your Customer ID is: " + customer.getUserId());
            System.out.println("  Log in with your email address: " + customer.getEmail());
        } catch (ReservationSystemException e) {
            System.out.println("\n  [Registration failed] " + e.getMessage());
        }
        ConsoleInput.pause();
    }

    /** Customers log in with their registered email. A wrong email just asks again. */
    private static User customerLogin() {
        System.out.println("\n--- Customer Login ---");
        return ConsoleInput.prompt("Email address: ", input -> {
            String email = Validator.email(input);
            try {
                return session.loginCustomer(email);
            } catch (ReservationSystemException e) {
                throw new InvalidInputException(e.getMessage());
            }
        });
    }

    /** Staff and managers log in with username and password. Wrong details ask again. */
    private static User employeeLogin() {
        System.out.println("\n--- Staff / Manager Login ---");
        final String[] answers = new String[2];
        while (true) {
            StepForm.run(
                    () -> answers[0] = ConsoleInput.prompt("Username: ", Validator::username),
                    () -> answers[1] = ConsoleInput.prompt("Password: ", Validator::password));
            try {
                return session.loginEmployee(answers[0], answers[1]);
            } catch (InvalidLoginException e) {
                System.out.println("  [Invalid] " + e.getMessage() + " Try again.");
            } catch (ReservationSystemException e) {
                System.out.println("  [Error] " + e.getMessage());
            }
        }
    }

    /**
     * POLYMORPHISM: the same call opens a CustomerMenu, StaffMenu or ManagerMenu
     * depending on the actual type of the user object.
     */
    private static void openMenu(User user) {
        System.out.println("\n  Welcome, " + user.getFullName() + "! Logged in as " + user.getRole().getLabel() + ".");
        user.showMenu();
    }
}
