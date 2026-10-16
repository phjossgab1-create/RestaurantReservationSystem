package enums;

/** Every status a reservation can be in. See Reservation for the allowed transitions. */
public enum ReservationStatus {
    CONFIRMED("Confirmed"),
    PENDING_UPDATE("Pending Update"),
    CANCEL_REQUESTED("Cancel Requested"),
    CHECKED_IN("Checked-In"),
    CHECKED_OUT("Checked-Out"),
    CANCELLED("Cancelled");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
