package gui;

import model.Reservation;
import model.RestaurantTable;
import model.TimeSlot;
import service.RestaurantSystem;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TablePanel extends RefreshablePanel {
    private final JTextField idField = Theme.textField(6);
    private final JTextField capacityField = Theme.textField(6);
    private final JTextField searchField = Theme.textField(6);
    private final JTextField dateField = Theme.textField(9);
    private final JComboBox<TimeSlot> slotCombo = createSlotCombo();
    private final Theme.ReadOnlyTableModel model =
            new Theme.ReadOnlyTableModel("Table ID", "Capacity", "Status", "Reserved By");
    private final JTable table = Theme.createTable(model, 2);

    /** Table ID currently searched for, or null when all tables are shown. */
    private Integer searchedId = null;

    public TablePanel(RestaurantSystem system) {
        super(system);
        slotCombo.setSelectedItem(system.getNextSlot());
        dateField.setText(system.getNextSlotDate().format(Reservation.DATE_FORMAT));

        add(createHeader("Tables", "Manage tables and check their availability for a date and time slot."),
                BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createControls(), BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                idField.setText(String.valueOf(model.getValueAt(table.getSelectedRow(), 0)));
                capacityField.setText(String.valueOf(model.getValueAt(table.getSelectedRow(), 1)));
            }
        });
    }

    private JPanel createControls() {
        JButton addButton = Theme.primaryButton("Add Table");
        JButton updateButton = Theme.secondaryButton("Update Capacity");
        JButton clearButton = Theme.secondaryButton("Clear");
        JButton searchButton = Theme.primaryButton("Search");
        JButton showAllButton = Theme.secondaryButton("Show All");
        JButton checkButton = Theme.primaryButton("Check Availability");

        addButton.addActionListener(e -> runAction(this::addTable));
        updateButton.addActionListener(e -> runAction(this::updateCapacity));
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> runAction(this::searchTable));
        showAllButton.addActionListener(e -> showAll());
        checkButton.addActionListener(e -> runAction(() -> {
            readDate(dateField);   // reports an invalid date before refreshing
            refresh();
        }));

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.row(
                Theme.labeled("Table ID", idField),
                Theme.labeled("Capacity", capacityField),
                Theme.labeled(" ", addButton),
                Theme.labeled(" ", updateButton),
                Theme.labeled(" ", clearButton)));
        card.add(Theme.row(
                Theme.labeled("Search by Table ID", searchField),
                Theme.labeled(" ", searchButton),
                Theme.labeled(" ", showAllButton)));
        card.add(Theme.row(
                Theme.labeled("Availability date (dd-MM-yyyy)", dateField),
                Theme.labeled("Time slot", slotCombo),
                Theme.labeled(" ", checkButton)));
        return card;
    }

    private void addTable() {
        int id = readInt(idField, "Table ID");
        int capacity = readInt(capacityField, "Capacity");
        system.addTable(id, capacity);
        clearForm();
        showAll();
        Theme.showInfo(this, "Table " + id + " added successfully.");
    }

    private void updateCapacity() {
        int id = readInt(idField, "Table ID");
        int capacity = readInt(capacityField, "Capacity");
        system.updateTableCapacity(id, capacity);
        refresh();
        Theme.showInfo(this, "Table " + id + " now seats " + capacity + " guests.");
    }

    private void searchTable() {
        int id = readInt(searchField, "Table ID to search");
        system.getTable(id);   // throws if the table does not exist
        searchedId = id;
        refresh();
    }

    private void showAll() {
        searchedId = null;
        searchField.setText("");
        refresh();
    }

    private void clearForm() {
        idField.setText("");
        capacityField.setText("");
        table.clearSelection();
    }

    @Override
    public void refresh() {
        LocalDate date = null;
        try {
            date = readDate(dateField);
        } catch (IllegalArgumentException ex) {
            // An invalid date is reported when the user presses "Check Availability".
        }
        TimeSlot slot = (TimeSlot) slotCombo.getSelectedItem();

        List<RestaurantTable> shown = new ArrayList<>();
        for (RestaurantTable t : system.getAllTables()) {
            if (searchedId == null || t.getId() == searchedId) {
                shown.add(t);
            }
        }

        model.setRowCount(0);
        for (RestaurantTable t : shown) {
            String status = "-";
            String reservedBy = "-";
            if (date != null && slot != null) {
                Reservation booking = system.findActiveReservation(t.getId(), date, slot);
                status = booking == null ? "Available" : "Reserved";
                if (booking != null) {
                    reservedBy = booking.getCustomer().getName() + " (" + booking.getGuests() + " guests)";
                }
            }
            model.addRow(new Object[]{t.getId(), t.getCapacity(), status, reservedBy});
        }
    }
}
