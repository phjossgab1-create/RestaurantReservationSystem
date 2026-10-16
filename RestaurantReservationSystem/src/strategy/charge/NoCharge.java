package strategy.charge;

import model.Reservation;

/** Used when an update doesn't change the date or time (e.g. only the guest count). */
public class NoCharge implements ChargeStrategy {

    @Override
    public double computeCharge(Reservation reservation) {
        return 0.0;
    }

    @Override
    public String getDescription() {
        return "No extra charge";
    }
}
