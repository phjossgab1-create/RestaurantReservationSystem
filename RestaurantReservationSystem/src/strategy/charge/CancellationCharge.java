package strategy.charge;

import model.Reservation;

/** Percentage of the table cost, charged when a cancellation is approved. */
public class CancellationCharge implements ChargeStrategy {

    public static final double CANCELLATION_RATE = 0.20;

    @Override
    public double computeCharge(Reservation reservation) {
        return reservation.getBaseAmount() * CANCELLATION_RATE;
    }

    @Override
    public String getDescription() {
        return String.format("Cancellation fee (%.0f%% of table cost)", CANCELLATION_RATE * 100);
    }
}
