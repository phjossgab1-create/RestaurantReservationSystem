package enums;

/** The three kinds of users in the system. */
public enum Role {
    CUSTOMER("Customer"),
    STAFF("Staff"),
    MANAGER("Manager");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
