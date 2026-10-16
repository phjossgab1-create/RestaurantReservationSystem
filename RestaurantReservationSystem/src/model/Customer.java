package model;

import enums.Role;
import ui.CustomerMenu;

/**
 * A registered restaurant customer. Saved in CUSTOMER.txt.
 * Inherits from User and implements Storable.
 *
 * The name is kept as first name + last name. If another customer already has the
 * same first and last name, duplicateNumber is 1, 2, ... and the full name shows it,
 * e.g. "Juan Dela Cruz(1)".
 */
public class Customer extends User implements Storable {

    private final String firstName;
    private final String lastName;
    private final int duplicateNumber;
    private String contactNumber;
    private String email;

    public Customer(String customerId, String firstName, String lastName, int duplicateNumber,
                    String contactNumber, String email) {
        super(customerId, buildFullName(firstName, lastName, duplicateNumber));
        this.firstName = firstName;
        this.lastName = lastName;
        this.duplicateNumber = duplicateNumber;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    /** "Juan" + "Dela Cruz" + 1  ->  "Juan Dela Cruz(1)" */
    public static String buildFullName(String firstName, String lastName, int duplicateNumber) {
        String name = lastName.isEmpty() ? firstName : firstName + " " + lastName;
        return duplicateNumber > 0 ? name + "(" + duplicateNumber + ")" : name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getDuplicateNumber() {
        return duplicateNumber;
    }

    /** True if this customer has the same first and last name (ignoring upper/lower case). */
    public boolean hasSameName(String otherFirst, String otherLast) {
        return firstName.equalsIgnoreCase(otherFirst) && lastName.equalsIgnoreCase(otherLast);
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    public void showMenu() {
        new CustomerMenu(this).run();
    }

    /** Format: customerId|firstName|lastName|duplicateNumber|contactNumber|email */
    @Override
    public String toFileString() {
        return String.join(DELIMITER, getUserId(), firstName, lastName, String.valueOf(duplicateNumber),
                contactNumber, email);
    }

    /**
     * Rebuilds a Customer from one line of CUSTOMER.txt.
     * Older lines that stored a single full name (4 or 5 fields) are still read:
     * the name is split into first name (first word) and last name (the rest).
     */
    public static Customer fromFileString(String line) {
        String[] p = line.split(SPLIT_REGEX, -1);
        switch (p.length) {
            case 6:
                return new Customer(p[0], p[1], p[2], Integer.parseInt(p[3]), p[4], p[5]);
            case 5: // old: id|fullName|address|contact|email
                return fromOldFullName(p[0], p[1], p[3], p[4]);
            case 4: // old: id|fullName|contact|email
                return fromOldFullName(p[0], p[1], p[2], p[3]);
            default:
                throw new IllegalArgumentException("expected 6 fields but found " + p.length);
        }
    }

    private static Customer fromOldFullName(String id, String fullName, String contact, String email) {
        String name = fullName;
        int duplicate = 0;
        if (name.matches(".*\\(\\d+\\)")) { // e.g. "joss(1)"
            int open = name.lastIndexOf('(');
            duplicate = Integer.parseInt(name.substring(open + 1, name.length() - 1));
            name = name.substring(0, open);
        }
        int space = name.indexOf(' ');
        String first = space < 0 ? name : name.substring(0, space);
        String last = space < 0 ? "" : name.substring(space + 1);
        return new Customer(id, first, last, duplicate, contact, email);
    }
}
