package util;

import enums.TableType;
import exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Every input rule in the system. Each method either returns the checked value
 * or throws InvalidInputException with a message telling the user what to type.
 *
 * Inputs are checked exactly as typed: extra spaces, leading zeros (like 01)
 * or other extra characters are rejected.
 */
public final class Validator {

    private static final String WHOLE_NUMBER = "0|[1-9][0-9]*";
    private static final String NAME = "\\p{L}+( \\p{L}+)*";
    private static final String EMAIL =
            "[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}";
    private static final String PH_MOBILE = "09[0-9]{9}";
    private static final String DATE = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
    private static final String TIME = "([01][0-9]|2[0-3]):[0-5][0-9]";
    private static final String RESERVATION_DIGITS = "[0-9]{6}";
    private static final String AMOUNT = "(0|[1-9][0-9]*)(\\.[0-9]{1,2})?";
    private static final int MAX_NAME_LENGTH = 30;
    private static final int MAX_EMAIL_LENGTH = 100;

    private Validator() {
    }

    // ------------------------------------------------------------- numbers

    /** A whole number from min to max, typed exactly (no "01", no spaces). */
    public static InputParser<Integer> intInRange(final int min, final int max) {
        return input -> {
            if (!input.matches(WHOLE_NUMBER) || input.length() > 9) {
                throw new InvalidInputException("Enter a number from " + min + " to " + max
                        + " (digits only, no spaces or leading zeros).");
            }
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                throw new InvalidInputException("Enter a number from " + min + " to " + max + ".");
            }
            return value;
        };
    }

    /** A peso amount greater than zero and not more than the balance. */
    public static InputParser<Double> paymentAmount(final double balance) {
        return input -> {
            if (!input.matches(AMOUNT) || input.length() > 12) {
                throw new InvalidInputException("Enter an amount like 500 or 750.50 (digits only, up to 2 decimals).");
            }
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                throw new InvalidInputException("Amount must be greater than zero.");
            }
            if (amount > balance + 0.005) {
                throw new InvalidInputException(String.format(
                        "Amount is more than the remaining balance of PHP %,.2f.", balance));
            }
            return amount;
        };
    }

    // ------------------------------------------------------- customer info

    public static String firstName(String input) throws InvalidInputException {
        return personName("First name", "Juan or Maria Clara", input);
    }

    public static String lastName(String input) throws InvalidInputException {
        return personName("Last name", "Santos or Dela Cruz", input);
    }

    /** Letters only; one space allowed between words. */
    private static String personName(String field, String example, String input) throws InvalidInputException {
        if (input.isEmpty()) {
            throw new InvalidInputException(field + " is required.");
        }
        if (input.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(field + " must be " + MAX_NAME_LENGTH + " characters or fewer.");
        }
        if (!input.matches(NAME)) {
            throw new InvalidInputException(field
                    + ": use letters only, with one space between words (e.g. " + example + ").");
        }
        return input;
    }

    public static String email(String input) throws InvalidInputException {
        if (input.isEmpty()) {
            throw new InvalidInputException("Email address is required.");
        }
        if (input.length() > MAX_EMAIL_LENGTH || !input.matches(EMAIL)) {
            throw new InvalidInputException(
                    "That is not a valid email address. Use a format like juan@example.com.");
        }
        return input.toLowerCase();
    }

    public static String contactNumber(String input) throws InvalidInputException {
        if (!input.matches(PH_MOBILE)) {
            throw new InvalidInputException(
                    "Enter 11 digits starting with 09, with no spaces or dashes (e.g. 09171234567).");
        }
        return input;
    }

    // ------------------------------------------------------ employee login

    public static String username(String input) throws InvalidInputException {
        if (input.isEmpty() || input.contains(" ")) {
            throw new InvalidInputException("Enter your username (no spaces).");
        }
        return input;
    }

    public static String password(String input) throws InvalidInputException {
        if (input.isEmpty()) {
            throw new InvalidInputException("Enter your password.");
        }
        return input;
    }

    // --------------------------------------------------------- reservations

    /** Only the 6 digits are typed; "RN-" is added automatically. */
    public static String reservationNumber(String input) throws InvalidInputException {
        if (!input.matches(RESERVATION_DIGITS)) {
            throw new InvalidInputException(
                    "Enter only the 6 digits of the reservation ID (e.g. 123456), without RN-.");
        }
        return "RN-" + input;
    }

    /** A real date from today up to 1 year ahead, in YYYY-MM-DD format. */
    public static LocalDate reservationDate(String input) throws InvalidInputException {
        if (!input.matches(DATE)) {
            throw new InvalidInputException("Use the format YYYY-MM-DD (e.g. 2026-12-24).");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("That date does not exist on the calendar.");
        }
        LocalDate today = LocalDate.now();
        LocalDate latest = BookingRules.latestBookableDate();
        if (date.isBefore(today)) {
            throw new InvalidInputException("That date has already passed. Pick today or a later date.");
        }
        if (date.isAfter(latest)) {
            throw new InvalidInputException("You can only book up to 1 year ahead (latest date: " + latest + ").");
        }
        if (date.equals(today) && !BookingRules.isTodayStillOpen()) {
            throw new InvalidInputException("Bookings for today are closed (last booking is "
                    + BookingRules.LAST_BOOKING_TIME + "). Pick another date.");
        }
        return date;
    }

    /** A time in 24-hour HH:mm within booking hours; if the date is today, it must be later than now. */
    public static InputParser<LocalTime> reservationTime(final LocalDate date) {
        return input -> {
            if (!input.matches(TIME)) {
                throw new InvalidInputException(
                        "Use 24-hour format HH:mm (e.g. 10:00, 13:30, 18:45).");
            }
            LocalTime time = LocalTime.parse(input);
            if (time.isBefore(BookingRules.OPENING_TIME) || time.isAfter(BookingRules.LAST_BOOKING_TIME)) {
                throw new InvalidInputException("We accept bookings from " + BookingRules.OPENING_TIME
                        + " to " + BookingRules.LAST_BOOKING_TIME + " only.");
            }
            if (date.equals(LocalDate.now()) && !time.isAfter(LocalTime.now())) {
                throw new InvalidInputException("That time has already passed today. Pick a later time.");
            }
            return time;
        };
    }

    /** Menu choice of table type that can actually seat this many guests. */
    public static InputParser<TableType> tableTypeFor(final int guests) {
        final TableType[] types = TableType.values();
        final InputParser<Integer> choice = intInRange(1, types.length);
        return input -> {
            TableType type = types[choice.parse(input) - 1];
            int capacity = type.getSeats() * BookingRules.MAX_TABLES;
            if (guests > capacity) {
                throw new InvalidInputException(type.getLabel() + " tables can fit at most " + capacity
                        + " guests (" + BookingRules.MAX_TABLES + " tables). Pick a bigger table type.");
            }
            return type;
        };
    }

    /** Number of tables: enough to seat the guests, and no more than the maximum. */
    public static InputParser<Integer> tableCountFor(final int guests, final TableType type) {
        final int min = BookingRules.minTablesFor(guests, type);
        final InputParser<Integer> range = intInRange(1, BookingRules.MAX_TABLES);
        return input -> {
            int tables = range.parse(input);
            if (tables < min) {
                throw new InvalidInputException(guests + " guests need at least " + min + " "
                        + type.getLabel() + " table(s) (" + type.getSeats() + " seats each).");
            }
            return tables;
        };
    }

    // --------------------------------------------------------------- other

    public static boolean yesNo(String input) throws InvalidInputException {
        if (input.equals("y") || input.equals("Y")) {
            return true;
        }
        if (input.equals("n") || input.equals("N")) {
            return false;
        }
        throw new InvalidInputException("Type y for yes or n for no.");
    }
}
