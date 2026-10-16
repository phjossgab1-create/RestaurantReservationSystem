package exception;

/**
 * Base class for every checked exception in the system.
 * All custom exceptions extend this (Inheritance), so the menus can
 * catch them in a single catch block.
 */
public class ReservationSystemException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReservationSystemException(String message) {
        super(message);
    }

    public ReservationSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
