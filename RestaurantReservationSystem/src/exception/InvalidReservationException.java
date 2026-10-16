package exception;

/**
 * Thrown when reservation details fail validation
 * (past date, closed hours, too many guests for the tables, etc.).
 */
public class InvalidReservationException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public InvalidReservationException(String message) {
        super(message);
    }
}
