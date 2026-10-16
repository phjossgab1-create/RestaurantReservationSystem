package enums;

/**
 * Table types the restaurant offers.
 * Change the seats or price here and the whole system follows.
 */
public enum TableType {
    REGULAR("Regular", 4, 500.00),
    FAMILY("Family", 8, 1200.00),
    VIP("VIP", 6, 2500.00);

    private final String label;
    private final int seats;
    private final double pricePerTable;

    TableType(String label, int seats, double pricePerTable) {
        this.label = label;
        this.seats = seats;
        this.pricePerTable = pricePerTable;
    }

    public String getLabel() {
        return label;
    }

    public int getSeats() {
        return seats;
    }

    public double getPricePerTable() {
        return pricePerTable;
    }
}
