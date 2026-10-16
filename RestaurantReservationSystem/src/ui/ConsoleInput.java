package ui;

import exception.GoBackException;
import exception.InvalidInputException;
import util.InputParser;
import util.Validator;

import java.util.Scanner;

/**
 * All keyboard input goes through prompt().
 *
 *  - Typing \ throws GoBackException, which returns the user to the prior menu.
 *  - Any invalid answer prints the reason and asks the SAME question again,
 *    so a mistake never throws the user out of what they were doing.
 */
public final class ConsoleInput {

    public static final String BACK = "\\";

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleInput() {
    }

    /** Asks until the parser accepts the answer, then returns the parsed value. */
    public static <T> T prompt(String label, InputParser<T> parser) {
        while (true) {
            String input = readRaw(label);
            if (BACK.equals(input)) {
                throw new GoBackException();
            }
            try {
                return parser.parse(input);
            } catch (InvalidInputException e) {
                System.out.println("  [Invalid] " + e.getMessage());
            }
        }
    }

    /**
     * Like prompt(), but pressing Enter keeps the current value.
     * The current value is still checked by the same rules.
     *
     * @param shown        what to display in brackets, e.g. "Regular"
     * @param currentInput what pressing Enter counts as, e.g. "1"
     */
    public static <T> T promptOrKeep(String label, String shown, final String currentInput,
                                     final InputParser<T> parser) {
        return prompt(label + " [" + shown + "]: ",
                input -> parser.parse(input.isEmpty() ? currentInput : input));
    }

    public static boolean confirm(String question) {
        return prompt(question + " (y/n): ", Validator::yesNo);
    }

    public static void pause() {
        readRaw("\nPress Enter to continue...");
    }

    /** Reads one line exactly as typed (nothing is trimmed, so extra spaces count as extra characters). */
    private static String readRaw(String label) {
        System.out.print(label);
        if (!SCANNER.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye!");
            System.exit(0);
        }
        return SCANNER.nextLine();
    }
}
