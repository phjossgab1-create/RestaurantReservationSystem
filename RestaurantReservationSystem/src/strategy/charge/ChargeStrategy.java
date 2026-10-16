package strategy.charge;

import model.Reservation;

/**
 * STRATEGY PATTERN - extra charges.
 * Each implementation is one way of calculating a fee. ReservationService holds a
 * ChargeStrategy and swaps it at runtime depending on what is happening
 * (reschedule, cancellation, or no fee) without any if/else inside the calculation.
 */
public interface ChargeStrategy {

    double computeCharge(Reservation reservation);

    String getDescription();
}
