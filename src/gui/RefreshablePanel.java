package gui;

import model.BaseEntity;
import model.Reservation;
import model.TimeSlot;
import service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Base class of every screen. MainFrame only knows this type and calls refresh()
 * (polymorphism), each screen decides how to redraw its own data.
 */
public abstract class RefreshablePanel extends JPanel {
    protected final RestaurantSystem system;

    protected RefreshablePanel(RestaurantSystem system) {
        super(new BorderLayout(0, 14));
        this.system = system;
        setBackground(Theme.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));
    }

    /** Reload the data shown on this screen from the RestaurantSystem. */
    public abstract void refresh();

    protected JComponent createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        header.add(Theme.title(title));
        header.add(Theme.mutedLabel(subtitle));
        return header;
    }

    /** Runs a GUI action and turns any business-rule or input error into a friendly dialog. */
    protected void runAction(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Theme.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Theme.showError(this, "Something went wrong: " + ex.getMessage());
        }
    }

    protected static int readInt(JTextField field, String label) {
        return parseInt(field.getText(), label);
    }

    protected static int parseInt(String text, String label) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    protected static LocalDate readDate(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Date cannot be empty. Use the format dd-MM-yyyy.");
        }
        try {
            return LocalDate.parse(text, Reservation.DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date \"" + text
                    + "\". Please enter a real date as dd-MM-yyyy, e.g. 30-09-2026.");
        }
    }

    protected static String todayText() {
        return LocalDate.now().format(Reservation.DATE_FORMAT);
    }

    protected static JComboBox<TimeSlot> createSlotCombo() {
        JComboBox<TimeSlot> combo = Theme.comboBox();
        for (TimeSlot slot : TimeSlot.values()) {
            combo.addItem(slot);
        }
        return combo;
    }

    /** Reloads a combo box while keeping the previously selected record selected. */
    protected static <T extends BaseEntity> void refillCombo(JComboBox<T> combo, List<T> items) {
        int index = combo.getSelectedIndex();
        int selectedId = index >= 0 ? combo.getItemAt(index).getId() : -1;
        combo.removeAllItems();
        for (T item : items) {
            combo.addItem(item);
            if (item.getId() == selectedId) {
                combo.setSelectedItem(item);
            }
        }
    }
}
