package exception;

/** Thrown when a Customer ID, username or password is wrong. */
public class InvalidLoginException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public InvalidLoginException(String message) {
        super(message);
    }
}
