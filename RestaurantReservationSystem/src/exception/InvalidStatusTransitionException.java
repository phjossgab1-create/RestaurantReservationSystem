package exception;

/**
 * Thrown when a reservation cannot move to the requested status,
 * e.g. checking in a cancelled reservation.
 */
public class InvalidStatusTransitionException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
