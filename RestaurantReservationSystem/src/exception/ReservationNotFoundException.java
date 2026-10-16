package exception;

/** Thrown when no reservation exists with the given Reservation ID. */
public class ReservationNotFoundException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public ReservationNotFoundException(String reservationId) {
        super("No reservation found with ID: " + reservationId);
    }
}
