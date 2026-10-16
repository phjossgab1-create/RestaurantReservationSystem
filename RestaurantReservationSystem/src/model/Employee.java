package model;

import enums.Role;

/**
 * Abstract parent of Staff and Manager (multilevel inheritance: User -> Employee -> Staff/Manager).
 * Holds the login credentials that both employee roles share. Saved in EMPLOYEE.txt.
 */
public abstract class Employee extends User implements Storable {

    private String password;

    protected Employee(String username, String password, String fullName) {
        super(username, fullName);
        this.password = password;
    }

    public String getUsername() {
        return getUserId();
    }

    /** The password is never returned; it can only be checked (Encapsulation). */
    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /** Format: username|password|fullName|ROLE */
    @Override
    public String toFileString() {
        return String.join(DELIMITER, getUsername(), password, getFullName(), getRole().name());
    }

    /**
     * Rebuilds the right subclass (Staff or Manager) from one line of EMPLOYEE.txt.
     * The caller just gets an Employee back and doesn't need to know which one (Polymorphism).
     */
    public static Employee fromFileString(String line) {
        String[] parts = line.split(SPLIT_REGEX, -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("expected 4 fields but found " + parts.length);
        }
        Role role = Role.valueOf(parts[3]);
        switch (role) {
            case STAFF:
                return new Staff(parts[0], parts[1], parts[2]);
            case MANAGER:
                return new Manager(parts[0], parts[1], parts[2]);
            default:
                throw new IllegalArgumentException("invalid employee role: " + role);
        }
    }
}
