package gui;

import model.Customer;
import model.Reservation;
import model.ReservationStatus;
import model.RestaurantTable;
import model.TimeSlot;
import service.RestaurantSystem;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationPanel extends RefreshablePanel {
    private final JComboBox<Customer> customerCombo = Theme.comboBox();
    private final JComboBox<RestaurantTable> tableCombo = Theme.comboBox();
    private final JTextField dateField = Theme.textField(9);
    private final JComboBox<TimeSlot> slotCombo = createSlotCombo();
    private final JTextField guestsField = Theme.textField(4);
    private final JTextField searchField = Theme.textField(10);
    private final Theme.ReadOnlyTableModel model = new Theme.ReadOnlyTableModel(
            "Res. ID", "Customer", "Table", "Date", "Time Slot", "Guests", "Status");
    private final JTable table = Theme.createTable(model, 6);

    private String currentQuery = "";

    public ReservationPanel(RestaurantSystem system) {
        super(system);
        dateField.setText(todayText());

        add(createHeader("Reservations", "Create reservations, search the history and cancel bookings."),
                BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createControls(), BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createControls() {
        JButton todayButton = Theme.secondaryButton("Today");
        JButton reserveButton = Theme.primaryButton("Reserve Table");
        JButton searchButton = Theme.primaryButton("Search");
        JButton showAllButton = Theme.secondaryButton("Show All");
        JButton cancelButton = Theme.dangerButton("Cancel Reservation");
        JButton completeButton = Theme.successButton("Mark Completed");

        todayButton.addActionListener(e -> dateField.setText(todayText()));
        reserveButton.addActionListener(e -> runAction(this::createReservation));
        searchButton.addActionListener(e -> runAction(this::search));
        showAllButton.addActionListener(e -> {
            currentQuery = "";
            searchField.setText("");
            refresh();
        });
        cancelButton.addActionListener(e -> runAction(this::cancelReservation));
        completeButton.addActionListener(e -> runAction(this::completeReservation));

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.row(
                Theme.labeled("Customer", customerCombo),
                Theme.labeled("Table", tableCombo),
                Theme.labeled("Time slot", slotCombo)));
        card.add(Theme.row(
                Theme.labeled("Date (dd-MM-yyyy)", dateField),
                Theme.labeled(" ", todayButton),
                Theme.labeled("Guests", guestsField),
                Theme.labeled(" ", reserveButton)));
        card.add(Theme.row(
                Theme.labeled("Search (ID or name)", searchField),
                Theme.labeled(" ", searchButton),
                Theme.labeled(" ", showAllButton),
                Theme.labeled(" ", cancelButton),
                Theme.labeled(" ", completeButton)));
        return card;
    }

    private void createReservation() {
        Customer customer = (Customer) customerCombo.getSelectedItem();
        RestaurantTable restaurantTable = (RestaurantTable) tableCombo.getSelectedItem();
        if (customer == null) {
            throw new IllegalArgumentException("Please select a customer. Add a customer first if the list is empty.");
        }
        if (restaurantTable == null) {
            throw new IllegalArgumentException("Please select a table. Add a table first if the list is empty.");
        }
        LocalDate date = readDate(dateField);
        TimeSlot slot = (TimeSlot) slotCombo.getSelectedItem();
        int guests = readInt(guestsField, "Number of guests");

        Reservation reservation = system.createReservation(
                customer.getId(), restaurantTable.getId(), date, slot, guests);
        guestsField.setText("");
        currentQuery = "";
        searchField.setText("");
        refresh();
        Theme.showInfo(this, "Reservation #" + reservation.getId() + " confirmed.\n"
                + "Table " + restaurantTable.getId() + " for " + customer.getName() + " on "
                + date.format(Reservation.DATE_FORMAT) + " at " + slot + " (" + guests + " guests).");
    }

    private void search() {
        currentQuery = searchField.getText().trim();
        refresh();
        if (model.getRowCount() == 0) {
            Theme.showInfo(this, "No reservation matches \"" + currentQuery + "\".");
        }
    }

    private void cancelReservation() {
        Integer id = selectedOrAskedReservationId("cancel");
        if (id == null) {
            return;
        }
        Reservation reservation = system.getReservation(id);
        if (!Theme.confirm(this, "Cancel this reservation?\n\n" + reservation.getSummary())) {
            return;
        }
        system.cancelReservation(id);
        refresh();
        Theme.showInfo(this, "Reservation #" + id + " cancelled. Table " + reservation.getTable().getId()
                + " is free again on " + reservation.getDate().format(Reservation.DATE_FORMAT)
                + " at " + reservation.getTimeSlot() + ".");
    }

    private void completeReservation() {
        Integer id = selectedOrAskedReservationId("mark as completed");
        if (id == null) {
            return;
        }
        system.completeReservation(id);
        refresh();
        Theme.showInfo(this, "Reservation #" + id + " marked as COMPLETED.");
    }

    /** Uses the selected table row; if none is selected, asks for a reservation ID. Null means "user cancelled". */
    private Integer selectedOrAskedReservationId(String action) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            return (Integer) model.getValueAt(row, 0);
        }
        String input = JOptionPane.showInputDialog(this,
                "No row selected. Enter the reservation ID to " + action + ":",
                "Reservation ID", JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return null;
        }
        return parseInt(input, "Reservation ID");
    }

    @Override
    public void refresh() {
        refillCombo(customerCombo, system.getAllCustomers());
        refillCombo(tableCombo, system.getAllTables());

        model.setRowCount(0);
        for (Reservation r : system.searchReservations(currentQuery)) {
            model.addRow(new Object[]{r.getId(), r.getCustomer().getName(), "Table " + r.getTable().getId(),
                    r.getDate().format(Reservation.DATE_FORMAT), r.getTimeSlot().getLabel(),
                    r.getGuests(), r.getStatus().toString()});
        }
    }
}
