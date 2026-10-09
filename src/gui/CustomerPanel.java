package gui;

import model.Customer;
import service.RestaurantSystem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends RefreshablePanel {
    private static final String SEARCH_BY_ID = "ID";
    private static final String SEARCH_BY_NAME = "Name";

    private final JTextField idField = Theme.textField(6);
    private final JTextField nameField = Theme.textField(14);
    private final JTextField phoneField = Theme.textField(11);
    private final JComboBox<String> searchMode = Theme.comboBox();
    private final JTextField searchField = Theme.textField(12);
    private final Theme.ReadOnlyTableModel model =
            new Theme.ReadOnlyTableModel("Customer ID", "Name", "Phone", "Reservations");
    private final JTable table = Theme.createTable(model, -1);

    /** Result of the last search, or null when all customers are shown. */
    private List<Customer> searchResult = null;

    public CustomerPanel(RestaurantSystem system) {
        super(system);
        searchMode.addItem(SEARCH_BY_ID);
        searchMode.addItem(SEARCH_BY_NAME);
        searchMode.setPreferredSize(new Dimension(100, 38));

        add(createHeader("Customers", "Add, search and update customer records."), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createControls(), BorderLayout.NORTH);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                idField.setText(String.valueOf(model.getValueAt(row, 0)));
                nameField.setText(String.valueOf(model.getValueAt(row, 1)));
                phoneField.setText(String.valueOf(model.getValueAt(row, 2)));
            }
        });
    }

    private JPanel createControls() {
        JButton addButton = Theme.primaryButton("Add Customer");
        JButton updateButton = Theme.secondaryButton("Update Details");
        JButton clearButton = Theme.secondaryButton("Clear");
        JButton searchButton = Theme.primaryButton("Search");
        JButton showAllButton = Theme.secondaryButton("Show All");

        addButton.addActionListener(e -> runAction(this::addCustomer));
        updateButton.addActionListener(e -> runAction(this::updateCustomer));
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> runAction(this::searchCustomer));
        showAllButton.addActionListener(e -> showAll());

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.row(
                Theme.labeled("Customer ID", idField),
                Theme.labeled("Name", nameField),
                Theme.labeled("Phone (10 digits)", phoneField)));
        card.add(Theme.row(addButton, updateButton, clearButton));
        card.add(Theme.row(
                Theme.labeled("Search by", searchMode),
                Theme.labeled("Search text", searchField),
                Theme.labeled(" ", searchButton),
                Theme.labeled(" ", showAllButton)));
        return card;
    }

    private void addCustomer() {
        int id = readInt(idField, "Customer ID");
        system.addCustomer(id, nameField.getText(), phoneField.getText());
        clearForm();
        showAll();
        Theme.showInfo(this, "Customer " + id + " added successfully.");
    }

    private void updateCustomer() {
        int id = readInt(idField, "Customer ID");
        system.updateCustomer(id, nameField.getText(), phoneField.getText());
        refresh();
        Theme.showInfo(this, "Customer " + id + " updated successfully.");
    }

    private void searchCustomer() {
        List<Customer> found = new ArrayList<>();
        if (SEARCH_BY_ID.equals(searchMode.getSelectedItem())) {
            found.add(system.getCustomer(readInt(searchField, "Customer ID to search")));
        } else {
            found = system.searchCustomersByName(searchField.getText());
        }
        searchResult = found;
        refresh();
        if (found.isEmpty()) {
            Theme.showInfo(this, "No customer matches \"" + searchField.getText().trim() + "\".");
        }
    }

    private void showAll() {
        searchResult = null;
        searchField.setText("");
        refresh();
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        phoneField.setText("");
        table.clearSelection();
    }

    @Override
    public void refresh() {
        List<Customer> shown = searchResult != null ? searchResult : system.getAllCustomers();
        model.setRowCount(0);
        for (Customer c : shown) {
            model.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(),
                    system.countReservationsForCustomer(c.getId())});
        }
    }
}
