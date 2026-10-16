package strategy.sort;

import model.Reservation;

import java.util.Comparator;
import java.util.List;

/** Fully Paid, then Partially Paid, then Unpaid; ties are broken by date. */
public class SortByPaymentStatus implements SortStrategy {

    @Override
    public void sort(List<Reservation> reservations) {
        reservations.sort(Comparator.comparing(Reservation::getPaymentStatus)
                .thenComparing(Reservation::getDate)
                .thenComparing(Reservation::getTime));
    }

    @Override
    public String getName() {
        return "Payment status (Paid, Partially Paid, Unpaid)";
    }
}
