package exception;

/** Wraps an IOException that happened while reading or writing a .txt data file. */
public class FileOperationException extends ReservationSystemException {

    private static final long serialVersionUID = 1L;

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
