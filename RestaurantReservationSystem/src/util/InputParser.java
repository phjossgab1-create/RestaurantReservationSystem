package util;

import exception.InvalidInputException;

/**
 * Checks one typed answer and turns it into a value (Abstraction).
 * If the answer is not valid it throws InvalidInputException with a message,
 * and ConsoleInput asks the same question again.
 *
 * @param <T> the type of value produced, e.g. Integer, LocalDate or String
 */
@FunctionalInterface
public interface InputParser<T> {

    T parse(String input) throws InvalidInputException;
}
