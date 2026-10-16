package service;

import enums.ReservationStatus;
import enums.Role;
import enums.TableType;
import exception.InvalidReservationException;
import exception.InvalidStatusTransitionException;
import exception.ReservationNotFoundException;
import exception.ReservationSystemException;
import model.Customer;
import model.PendingChange;
import model.Reservation;
import repository.CustomerRepository;
import repository.ReservationRepository;
import strategy.charge.CancellationCharge;
import strategy.charge.ChargeStrategy;
import strategy.charge.NoCharge;
import strategy.charge.RescheduleCharge;
import strategy.sort.SortByDate;
import strategy.sort.SortStrategy;
import util.BookingRules;
import util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * All reservation business logic. Every public method first checks the user's role,
 * then validates, then saves through the repository.
 *
 * This class is the "context" in the Strategy pattern: it holds a ChargeStrategy
 * and a SortStrategy and uses whichever one is currently set.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final SessionManager session = SessionManager.getInstance();

    private ChargeStrategy chargeStrategy = new NoCharge();
    private SortStrategy sortStrategy = new SortByDate();

    // ------------------------------------------------------ strategy setters

    public void setChargeStrategy(ChargeStrategy chargeStrategy) {
        this.chargeStrategy = chargeStrategy;
    }

    public void setSortStrategy(SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy;
    }

    public SortStrategy getSortStrategy() {
        return sortStrategy;
    }

    // ============================================================ CUSTOMER

    /** CREATE: a customer makes a new reservation. */
    public Reservation makeReservation(LocalDate date, LocalTime time, int guests,
                                       TableType tableType, int numTables) throws ReservationSystemException {
        Customer customer = session.getCurrentCustomer();
        validateDetails(date, time, guests, tableType, numTables);

        String id = IdGenerator.generateReservationId(reservationRepository.findAllIdsEverUsed());
        Reservation reservation = new Reservation(id, customer.getUserId(), date, time,
                guests, tableType, numTables);
        reservationRepository.create(reservation);
        return reservation;
    }

    /** READ: the logged-in customer's reservations, including cancelled ones. */
    public List<Reservation> getMyReservations() throws ReservationSystemException {
        Customer customer = session.getCurrentCustomer();
        List<Reservation> mine = reservationRepository.findByCustomerId(customer.getUserId());
        for (Reservation r : reservationRepository.findAllCancelled()) {
            if (r.getCustomerId().equals(customer.getUserId())) {
                mine.add(r);
            }
        }
        new SortByDate().sort(mine);
        return mine;
    }

    /** READ: one active reservation that belongs to the logged-in customer. */
    public Reservation getMyReservation(String reservationId) throws ReservationSystemException {
        Customer customer = session.getCurrentCustomer();
        Reservation reservation = reservationRepository.findById(reservationId);
        if (!reservation.getCustomerId().equals(customer.getUserId())) {
            // Don't reveal that someone else's reservation exists.
            throw new ReservationNotFoundException(reservationId);
        }
        return reservation;
    }

    /** UPDATE (step 1): the customer asks for a change; it waits for staff approval. */
    public void requestUpdate(String reservationId, PendingChange change) throws ReservationSystemException {
        Reservation reservation = getMyReservation(reservationId);
        if (change.isSameAs(reservation)) {
            throw new InvalidReservationException("The new details are the same as the current ones.");
        }
        validateDetails(change.getDate(), change.getTime(), change.getGuests(),
                change.getTableType(), change.getNumTables());
        reservation.requestUpdate(change);
        reservationRepository.update(reservation);
    }

    /** Shows the customer what fee an update will cost, before they submit it. */
    public double previewUpdateCharge(Reservation reservation, PendingChange change) {
        setChargeStrategy(chooseUpdateStrategy(reservation, change));
        return chargeStrategy.computeCharge(reservation);
    }

    /** DELETE (step 1): the customer asks to cancel; it waits for manager approval. */
    public void requestCancel(String reservationId) throws ReservationSystemException {
        Reservation reservation = getMyReservation(reservationId);
        reservation.requestCancel();
        reservationRepository.update(reservation);
    }

    public double previewCancellationCharge(Reservation reservation) {
        setChargeStrategy(new CancellationCharge());
        return chargeStrategy.computeCharge(reservation);
    }

    // =============================================================== STAFF

    public List<Reservation> getPendingUpdates() throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        return filterInternal(r -> r.getStatus() == ReservationStatus.PENDING_UPDATE);
    }

    /** UPDATE (step 2): staff approves the change. Returns the fee that was charged. */
    public double approveUpdate(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        Reservation reservation = reservationRepository.findById(reservationId);
        PendingChange change = reservation.getPendingChange();
        if (change == null) {
            throw new InvalidStatusTransitionException(
                    "Reservation " + reservation.getReservationId() + " has no pending update.");
        }
        // Validate again: the requested date might have passed while it was waiting.
        validateDetails(change.getDate(), change.getTime(), change.getGuests(),
                change.getTableType(), change.getNumTables());

        // Strategy chosen at runtime: reschedule fee only if the date/time changed.
        setChargeStrategy(chooseUpdateStrategy(reservation, change));
        double charge = chargeStrategy.computeCharge(reservation);

        reservation.applyPendingChange();
        reservation.addCharge(charge);
        reservationRepository.update(reservation);
        return charge;
    }

    public void rejectUpdate(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.rejectPendingChange();
        reservationRepository.update(reservation);
    }

    public void checkIn(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.checkIn();
        reservationRepository.update(reservation);
        reservationRepository.logStatusChange(reservation, getCustomerName(reservation.getCustomerId()));
    }

    public void checkOut(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.checkOut();
        reservationRepository.update(reservation);
        reservationRepository.logStatusChange(reservation, getCustomerName(reservation.getCustomerId()));
    }

    /** UPDATE: staff records a payment. Returns the updated reservation. */
    public Reservation recordPayment(String reservationId, double amount) throws ReservationSystemException {
        session.requireRole(Role.STAFF);
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.recordPayment(amount);
        reservationRepository.update(reservation);
        return reservation;
    }

    // ============================================================= MANAGER

    public List<Reservation> getCancelRequests() throws ReservationSystemException {
        session.requireRole(Role.MANAGER);
        return filterInternal(r -> r.getStatus() == ReservationStatus.CANCEL_REQUESTED);
    }

    /** DELETE (step 2): manager approves; the record moves to CANCELLED.txt. Returns the fee. */
    public double approveCancel(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.MANAGER);
        Reservation reservation = reservationRepository.findById(reservationId);

        setChargeStrategy(new CancellationCharge());
        double charge = chargeStrategy.computeCharge(reservation);

        reservation.markCancelled();
        reservation.addCharge(charge);
        reservationRepository.archiveCancelled(reservation);
        return charge;
    }

    public void rejectCancel(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.MANAGER);
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.rejectCancel();
        reservationRepository.update(reservation);
    }

    public List<String[]> getStatusLog() throws ReservationSystemException {
        session.requireRole(Role.MANAGER);
        return reservationRepository.readStatusLog();
    }

    public List<Reservation> getCancelledReservations() throws ReservationSystemException {
        session.requireRole(Role.MANAGER);
        return reservationRepository.findAllCancelled();
    }

    // ===================================================== STAFF + MANAGER

    public List<Reservation> getAllReservations() throws ReservationSystemException {
        session.requireRole(Role.STAFF, Role.MANAGER);
        return reservationRepository.findAll();
    }

    public Reservation searchReservation(String reservationId) throws ReservationSystemException {
        session.requireRole(Role.STAFF, Role.MANAGER);
        return reservationRepository.findById(reservationId);
    }

    /** Sorts using whichever SortStrategy is currently set. */
    public List<Reservation> getSortedReservations() throws ReservationSystemException {
        session.requireRole(Role.STAFF, Role.MANAGER);
        List<Reservation> list = reservationRepository.findAll();
        sortStrategy.sort(list);
        return list;
    }

    /** Filters with any condition the caller passes in (a Predicate is a strategy too). */
    public List<Reservation> filterReservations(Predicate<Reservation> condition) throws ReservationSystemException {
        session.requireRole(Role.STAFF, Role.MANAGER);
        return filterInternal(condition);
    }

    // ============================================================= helpers

    /** The customer who owns a reservation, or null if the record is missing. */
    public Customer getCustomer(String customerId) {
        try {
            return customerRepository.findById(customerId);
        } catch (ReservationSystemException e) {
            return null;
        }
    }

    public String getCustomerName(String customerId) {
        try {
            return customerRepository.findById(customerId).getFullName();
        } catch (ReservationSystemException e) {
            return "(unknown)";
        }
    }

    private List<Reservation> filterInternal(Predicate<Reservation> condition) throws ReservationSystemException {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservationRepository.findAll()) {
            if (condition.test(r)) {
                result.add(r);
            }
        }
        new SortByDate().sort(result);
        return result;
    }

    private ChargeStrategy chooseUpdateStrategy(Reservation reservation, PendingChange change) {
        return change.changesSchedule(reservation) ? new RescheduleCharge() : new NoCharge();
    }

    /**
     * Throws InvalidReservationException if any detail breaks a restaurant rule.
     * The menus already check each field as it is typed; this is the final safety check.
     */
    private void validateDetails(LocalDate date, LocalTime time, int guests,
                                 TableType tableType, int numTables) throws InvalidReservationException {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new InvalidReservationException("Reservation date cannot be in the past.");
        }
        if (date.isAfter(BookingRules.latestBookableDate())) {
            throw new InvalidReservationException("Reservations can only be made up to "
                    + BookingRules.MAX_YEARS_AHEAD + " year ahead.");
        }
        if (date.equals(today) && !time.isAfter(LocalTime.now())) {
            throw new InvalidReservationException("That time has already passed today.");
        }
        if (time.isBefore(BookingRules.OPENING_TIME) || time.isAfter(BookingRules.LAST_BOOKING_TIME)) {
            throw new InvalidReservationException("Reservations are only accepted from "
                    + BookingRules.OPENING_TIME + " to " + BookingRules.LAST_BOOKING_TIME + ".");
        }
        if (numTables < 1 || numTables > BookingRules.MAX_TABLES) {
            throw new InvalidReservationException("Number of tables must be between 1 and "
                    + BookingRules.MAX_TABLES + ".");
        }
        if (guests < 1) {
            throw new InvalidReservationException("Number of guests must be at least 1.");
        }
        int capacity = tableType.getSeats() * numTables;
        if (guests > capacity) {
            throw new InvalidReservationException(String.format(
                    "%d %s table(s) seat only %d guests, but there are %d guests.",
                    numTables, tableType.getLabel(), capacity, guests));
        }
    }
}
