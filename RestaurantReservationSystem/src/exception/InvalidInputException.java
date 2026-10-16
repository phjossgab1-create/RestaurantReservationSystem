package exception;

/** Thrown when registration or other user input is invalid (blank name, bad email, etc.). */
public class InvalidInputException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
