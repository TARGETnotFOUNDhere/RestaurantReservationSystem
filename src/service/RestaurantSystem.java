package service;

import model.Customer;
import model.Reservation;
import model.ReservationStatus;
import model.RestaurantTable;
import model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Business-logic layer. All rules (unique IDs, capacity check, conflict prevention,
 * cancellation) are enforced here, so the GUI only has to call these methods.
 * Every rule violation is reported with an IllegalArgumentException / IllegalStateException
 * carrying a user-friendly message.
 */
public class RestaurantSystem {
    private static final int FIRST_RESERVATION_ID = 1001;

    // TreeMap keeps records sorted by ID and gives fast lookup by ID.
    private final Map<Integer, RestaurantTable> tables = new TreeMap<>();
    private final Map<Integer, Customer> customers = new TreeMap<>();
    private final Map<Integer, Reservation> reservations = new TreeMap<>();
    private int nextReservationId = FIRST_RESERVATION_ID;

    // ---------------------------------------------------------------- tables

    public RestaurantTable addTable(int id, int capacity) {
        RestaurantTable table = new RestaurantTable(id, capacity);   // validates id and capacity
        if (tables.containsKey(id)) {
            throw new IllegalArgumentException("Table ID " + id + " already exists. Please use a unique ID.");
        }
        tables.put(id, table);
        return table;
    }

    public RestaurantTable getTable(int id) {
        RestaurantTable table = tables.get(id);
        if (table == null) {
            throw new IllegalArgumentException("Table " + id + " does not exist.");
        }
        return table;
    }

    public List<RestaurantTable> getAllTables() {
        return new ArrayList<>(tables.values());
    }

    public void updateTableCapacity(int id, int newCapacity) {
        RestaurantTable table = getTable(id);
        RestaurantTable.validateCapacity(newCapacity);
        LocalDate today = LocalDate.now();
        for (Reservation r : reservations.values()) {
            boolean upcomingBooking = r.isConfirmed() && !r.getDate().isBefore(today);
            if (upcomingBooking && r.getTable().getId() == id && r.getGuests() > newCapacity) {
                throw new IllegalArgumentException(String.format(
                        "Cannot reduce Table %d to %d seats: Reservation #%d has %d guests.",
                        id, newCapacity, r.getId(), r.getGuests()));
            }
        }
        table.setCapacity(newCapacity);
    }

    /** Returns the reservation that currently occupies the table at that date/slot, or null if free. */
    public Reservation findActiveReservation(int tableId, LocalDate date, TimeSlot slot) {
        for (Reservation r : reservations.values()) {
            if (r.blocks(tableId, date, slot)) {
                return r;
            }
        }
        return null;
    }

    public boolean isTableAvailable(int tableId, LocalDate date, TimeSlot slot) {
        getTable(tableId);   // fails for a non-existing table
        return findActiveReservation(tableId, date, slot) == null;
    }

    public int countAvailableTables(LocalDate date, TimeSlot slot) {
        int count = 0;
        for (RestaurantTable table : tables.values()) {
            if (findActiveReservation(table.getId(), date, slot) == null) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------- customers

    public Customer addCustomer(int id, String name, String phone) {
        Customer customer = new Customer(id, name, phone);           // validates everything
        if (customers.containsKey(id)) {
            throw new IllegalArgumentException("Customer ID " + id + " already exists. Please use a unique ID.");
        }
        customers.put(id, customer);
        return customer;
    }

    public Customer getCustomer(int id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new IllegalArgumentException("Customer ID " + id + " does not exist.");
        }
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    /** Case-insensitive partial match on the customer name. */
    public List<Customer> searchCustomersByName(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a name to search.");
        }
        String query = text.trim().toLowerCase();
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers.values()) {
            if (c.getName().toLowerCase().contains(query)) {
                result.add(c);
            }
        }
        return result;
    }

    public void updateCustomer(int id, String name, String phone) {
        Customer customer = getCustomer(id);
        // Validate both values first so a bad phone number cannot leave a half-updated customer.
        String validName = Customer.validateName(name);
        String validPhone = Customer.validatePhone(phone);
        customer.setName(validName);
        customer.setPhone(validPhone);
    }

    public int countReservationsForCustomer(int customerId) {
        int count = 0;
        for (Reservation r : reservations.values()) {
            if (r.getCustomer().getId() == customerId) {
                count++;
            }
        }
        return count;
    }

    // ---------------------------------------------------------- reservations

    public Reservation createReservation(int customerId, int tableId, LocalDate date,
                                         TimeSlot slot, int guests) {
        Customer customer = getCustomer(customerId);
        RestaurantTable table = getTable(tableId);

        if (date == null) {
            throw new IllegalArgumentException("Please select a valid date.");
        }
        if (slot == null) {
            throw new IllegalArgumentException("Please select a time slot.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reservations cannot be made for a past date.");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }
        if (guests > table.getCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "Table %d seats only %d guests, but %d guests were requested. Please choose a larger table.",
                    tableId, table.getCapacity(), guests));
        }

        // Conflict prevention: same table + same date + same slot must not already be taken.
        Reservation existing = findActiveReservation(tableId, date, slot);
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                    "Table %d is already reserved on %s at %s (Reservation #%d for %s). "
                            + "Please choose another table, date or time slot.",
                    tableId, date.format(Reservation.DATE_FORMAT), slot,
                    existing.getId(), existing.getCustomer().getName()));
        }

        Reservation reservation = new Reservation(nextReservationId, customer, table, date, slot, guests);
        reservations.put(nextReservationId, reservation);
        nextReservationId++;
        return reservation;
    }

    public Reservation getReservation(int id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation #" + id + " does not exist.");
        }
        return reservation;
    }

    /** Cancelling only changes the status; the record stays in the history. */
    public void cancelReservation(int id) {
        getReservation(id).cancel();
    }

    public void completeReservation(int id) {
        getReservation(id).markCompleted();
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    /** Empty text returns everything; otherwise matches the reservation ID or part of the customer name. */
    public List<Reservation> searchReservations(String text) {
        if (text == null || text.trim().isEmpty()) {
            return getAllReservations();
        }
        String query = text.trim().toLowerCase();
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            boolean idMatches = String.valueOf(r.getId()).equals(query);
            boolean nameMatches = r.getCustomer().getName().toLowerCase().contains(query);
            if (idMatches || nameMatches) {
                result.add(r);
            }
        }
        return result;
    }

    /** All reservations of a date (including cancelled ones), sorted by time slot and then table ID. */
    public List<Reservation> getReservationsForDate(LocalDate date) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getDate().equals(date)) {
                result.add(r);
            }
        }
        result.sort(Comparator.comparing(Reservation::getTimeSlot)
                .thenComparingInt(r -> r.getTable().getId()));
        return result;
    }

    // ------------------------------------------------------------ statistics

    public int getTotalTables() {
        return tables.size();
    }

    public int getTotalCustomers() {
        return customers.size();
    }

    public int countReservationsByStatus(ReservationStatus status) {
        int count = 0;
        for (Reservation r : reservations.values()) {
            if (r.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /** Today's reservations that are not cancelled. */
    public int countTodaysReservations() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Reservation r : reservations.values()) {
            if (r.getDate().equals(today) && r.getStatus() != ReservationStatus.CANCELLED) {
                count++;
            }
        }
        return count;
    }

    /** The slot the dashboard uses for "available tables": the first slot of today not yet finished. */
    public TimeSlot getNextSlot() {
        TimeSlot slot = TimeSlot.firstSlotNotFinishedAt(LocalDateTime.now().toLocalTime());
        return slot != null ? slot : TimeSlot.values()[0];   // after the last slot: tomorrow's first slot
    }

    public LocalDate getNextSlotDate() {
        boolean slotLeftToday = TimeSlot.firstSlotNotFinishedAt(LocalDateTime.now().toLocalTime()) != null;
        return slotLeftToday ? LocalDate.now() : LocalDate.now().plusDays(1);
    }

    // ----------------------------------------------------------- sample data

    public void loadSampleData() {
        addTable(1, 2);
        addTable(2, 4);
        addTable(3, 4);
        addTable(4, 6);
        addTable(5, 6);
        addTable(6, 8);

        addCustomer(101, "Lakshya", "9876543210");
        addCustomer(102, "Anurag", "9876543211");
        addCustomer(103, "Ayush", "9876543212");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        createReservation(103, 2, today, TimeSlot.SLOT_1_PM, 3);
        createReservation(101, 4, today, TimeSlot.SLOT_7_PM, 6);
        createReservation(102, 1, today, TimeSlot.SLOT_8_PM, 2);
        createReservation(101, 5, tomorrow, TimeSlot.SLOT_8_PM, 5);
        createReservation(103, 3, tomorrow, TimeSlot.SLOT_12_PM, 4);

        // One completed and one cancelled reservation so the history and dashboard show every status.
        Reservation done = createReservation(102, 3, today, TimeSlot.SLOT_12_PM, 2);
        done.markCompleted();
        Reservation cancelled = createReservation(102, 6, today, TimeSlot.SLOT_9_PM, 7);
        cancelReservation(cancelled.getId());
    }
}
