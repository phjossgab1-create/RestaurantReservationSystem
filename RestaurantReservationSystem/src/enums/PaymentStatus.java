package enums;

/**
 * Payment status of a reservation.
 * The declaration order (Paid, Partially Paid, Unpaid) is used when sorting.
 */
public enum PaymentStatus {
    FULLY_PAID("Fully Paid"),
    PARTIALLY_PAID("Partially Paid"),
    UNPAID("Unpaid");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
