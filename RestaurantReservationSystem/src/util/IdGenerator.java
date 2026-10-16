package util;

import java.time.Year;
import java.util.Collection;
import java.util.Random;

/**
 * Generates the IDs required by the spec:
 *   Customer ID:    YEAR-6 random digits  (e.g. 2026-123456)
 *   Reservation ID: RN-6 random digits    (e.g. RN-123456)
 * It keeps trying until it finds one that isn't already used.
 */
public final class IdGenerator {

    private static final Random RANDOM = new Random();

    private IdGenerator() {
        // utility class, no objects
    }

    public static String generateCustomerId(Collection<String> existingIds) {
        String id;
        do {
            id = Year.now().getValue() + "-" + sixRandomDigits();
        } while (existingIds.contains(id));
        return id;
    }

    public static String generateReservationId(Collection<String> existingIds) {
        String id;
        do {
            id = "RN-" + sixRandomDigits();
        } while (existingIds.contains(id));
        return id;
    }

    private static String sixRandomDigits() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
