package repository;

import exception.FileOperationException;
import exception.ReservationNotFoundException;
import exception.ReservationSystemException;
import model.Reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD for reservations.
 *   RESERVE.txt   - active reservations
 *   CANCELLED.txt - cancelled reservations (moved here instead of being erased)
 *   STATUS.txt    - log of every check-in and check-out
 */
public class ReservationRepository extends FileRepository<Reservation> {

    private static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationRepository() {
        super(FileManager.RESERVE_FILE);
    }

    @Override
    protected Reservation parse(String line) {
        return Reservation.fromFileString(line);
    }

    @Override
    protected String getId(Reservation reservation) {
        return reservation.getReservationId();
    }

    @Override
    protected ReservationSystemException notFound(String id) {
        return new ReservationNotFoundException(id);
    }

    /**
     * Overrides the normal delete (Polymorphism): instead of erasing the record,
     * it is moved to CANCELLED.txt so there is always a history.
     */
    @Override
    public void delete(String id) throws ReservationSystemException {
        archiveCancelled(findById(id));
    }

    /** Saves the cancelled reservation in CANCELLED.txt, then removes it from RESERVE.txt. */
    public void archiveCancelled(Reservation reservation) throws ReservationSystemException {
        fileManager.appendLine(FileManager.CANCELLED_FILE, reservation.toFileString());
        super.delete(reservation.getReservationId());
    }

    public List<Reservation> findByCustomerId(String customerId) throws FileOperationException {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : findAll()) {
            if (r.getCustomerId().equals(customerId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> findAllCancelled() throws FileOperationException {
        return parseLines(FileManager.CANCELLED_FILE);
    }

    /** All reservation IDs ever used (active and cancelled), so new IDs never repeat. */
    public List<String> findAllIdsEverUsed() throws FileOperationException {
        List<String> ids = new ArrayList<>();
        for (Reservation r : findAll()) {
            ids.add(r.getReservationId());
        }
        for (Reservation r : findAllCancelled()) {
            ids.add(r.getReservationId());
        }
        return ids;
    }

    /** Writes one line to STATUS.txt: timestamp|reservationId|customerId|customerName|status */
    public void logStatusChange(Reservation reservation, String customerName) throws FileOperationException {
        String line = String.join("|",
                LocalDateTime.now().format(LOG_FORMAT),
                reservation.getReservationId(),
                reservation.getCustomerId(),
                customerName,
                reservation.getStatus().getLabel());
        fileManager.appendLine(FileManager.STATUS_FILE, line);
    }

    /** Reads STATUS.txt. Each entry is {timestamp, reservationId, customerId, customerName, status}. */
    public List<String[]> readStatusLog() throws FileOperationException {
        List<String[]> entries = new ArrayList<>();
        for (String line : fileManager.readLines(FileManager.STATUS_FILE)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length == 5) {
                entries.add(parts);
            }
        }
        return entries;
    }
}
