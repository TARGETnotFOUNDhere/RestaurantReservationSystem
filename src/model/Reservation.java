package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class Reservation extends BaseEntity {
    /** dd-MM-yyyy; STRICT so that dates such as 31-02-2026 are rejected instead of adjusted. */
    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Customer customer;
    private final RestaurantTable table;
    private final LocalDate date;
    private final TimeSlot timeSlot;
    private final int guests;
    private ReservationStatus status;

    public Reservation(int id, Customer customer, RestaurantTable table,
                       LocalDate date, TimeSlot timeSlot, int guests) {
        super(id);
        if (customer == null || table == null || date == null || timeSlot == null) {
            throw new IllegalArgumentException("Reservation needs a customer, table, date and time slot.");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }
        this.customer = customer;
        this.table = table;
        this.date = date;
        this.timeSlot = timeSlot;
        this.guests = guests;
        this.status = ReservationStatus.CONFIRMED;   // every new reservation starts as CONFIRMED
    }

    public Customer getCustomer() { return customer; }
    public RestaurantTable getTable() { return table; }
    public LocalDate getDate() { return date; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getGuests() { return guests; }
    public ReservationStatus getStatus() { return status; }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    /** True if this reservation currently blocks the given table/date/slot. */
    public boolean blocks(int tableId, LocalDate otherDate, TimeSlot otherSlot) {
        return status.occupiesTable()
                && table.getId() == tableId
                && date.equals(otherDate)
                && timeSlot == otherSlot;
    }

    /** The record is kept (history); only its status changes. */
    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation #" + getId() + " is already cancelled.");
        }
        if (status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Reservation #" + getId() + " is already completed and cannot be cancelled.");
        }
        status = ReservationStatus.CANCELLED;
    }

    public void markCompleted() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only a CONFIRMED reservation can be completed. Reservation #"
                    + getId() + " is " + status + ".");
        }
        status = ReservationStatus.COMPLETED;
    }

    @Override
    public String getSummary() {
        return String.format("Reservation #%d | %s | Table %d | %s %s | %d guests | %s",
                getId(), customer.getName(), table.getId(),
                date.format(DATE_FORMAT), timeSlot, guests, status);
    }
}
