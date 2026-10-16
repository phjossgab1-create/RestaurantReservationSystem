package exception;

/**
 * Thrown when the user types \ at any prompt.
 * It is not an error: it travels up until a menu catches it,
 * and that menu is shown again (so the user returns to the prior menu).
 * It is unchecked (extends RuntimeException) so it can pass through every input method.
 */
public class GoBackException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GoBackException() {
        super("User chose to go back.");
    }
}
