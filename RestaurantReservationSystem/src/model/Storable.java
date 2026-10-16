package model;

/**
 * Anything that can be saved as one line in a .txt file.
 * (Abstraction + Polymorphism: Customer, Employee and Reservation
 * each implement toFileString() in their own way.)
 */
public interface Storable {

    /** Separator between fields in a saved line. */
    String DELIMITER = "|";

    /** Regex version of the delimiter, for String.split(). */
    String SPLIT_REGEX = "\\|";

    /** Converts this object into one line of text for saving. */
    String toFileString();
}
