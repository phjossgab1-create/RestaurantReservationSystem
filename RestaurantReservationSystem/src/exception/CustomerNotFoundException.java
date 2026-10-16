package exception;

/** Thrown when no customer exists with the given Customer ID. */
public class CustomerNotFoundException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(String customerId) {
        super("No customer found with ID: " + customerId);
    }
}
