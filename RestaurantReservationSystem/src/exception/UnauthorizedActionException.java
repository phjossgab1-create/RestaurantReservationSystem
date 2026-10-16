package exception;

/** Thrown when the logged-in user's role is not allowed to perform an action. */
public class UnauthorizedActionException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
