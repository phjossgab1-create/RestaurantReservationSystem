package strategy.sort;

import model.Reservation;

import java.util.List;

/**
 * STRATEGY PATTERN - sorting.
 * Staff and managers pick how to sort; ReservationService just calls sort()
 * on whichever strategy it was given.
 */
public interface SortStrategy {

    void sort(List<Reservation> reservations);

    String getName();
}
