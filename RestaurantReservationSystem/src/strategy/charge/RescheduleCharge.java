package strategy.charge;

import model.Reservation;

/** Flat fee when the reservation date or time is changed. */
public class RescheduleCharge implements ChargeStrategy {

    public static final double RESCHEDULE_FEE = 200.00;

    @Override
    public double computeCharge(Reservation reservation) {
        return RESCHEDULE_FEE;
    }

    @Override
    public String getDescription() {
        return String.format("Reschedule fee (flat PHP %,.2f)", RESCHEDULE_FEE);
    }
}
