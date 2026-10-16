package exception;

/** Thrown for a zero/negative payment, an overpayment, or checking out with a balance. */
public class InvalidPaymentException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public InvalidPaymentException(String message) {
        super(message);
    }
}
