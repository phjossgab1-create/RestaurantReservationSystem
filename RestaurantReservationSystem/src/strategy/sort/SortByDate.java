package strategy.sort;

import model.Reservation;

import java.util.Comparator;
import java.util.List;

/** Earliest date first; same-day reservations by time. */
public class SortByDate implements SortStrategy {

    @Override
    public void sort(List<Reservation> reservations) {
        reservations.sort(Comparator.comparing(Reservation::getDate)
                .thenComparing(Reservation::getTime));
    }

    @Override
    public String getName() {
        return "Date (earliest first)";
    }
}
