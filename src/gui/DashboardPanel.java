package gui;

import model.Reservation;
import model.ReservationStatus;
import model.TimeSlot;
import service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;

public class DashboardPanel extends RefreshablePanel {
    private final StatCard totalTables = new StatCard("Total Tables", Theme.ACCENT);
    private final StatCard availableTables = new StatCard("Available Tables", Theme.SUCCESS);
    private final StatCard totalCustomers = new StatCard("Total Customers", Theme.INFO);
    private final StatCard todaysReservations = new StatCard("Today's Reservations", Theme.WARNING);
    private final StatCard confirmed = new StatCard("Confirmed Reservations", Theme.SUCCESS);
    private final StatCard cancelled = new StatCard("Cancelled Reservations", Theme.DANGER);

    private final Theme.ReadOnlyTableModel todayModel =
            new Theme.ReadOnlyTableModel("Time Slot", "Table", "Customer", "Guests", "Status");
    private final JTable todayTable = Theme.createTable(todayModel, 4);

    public DashboardPanel(RestaurantSystem system) {
        super(system);
        add(createHeader("Dashboard", "Overview of the restaurant today."), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setOpaque(false);
        for (StatCard card : new StatCard[]{totalTables, availableTables, totalCustomers,
                todaysReservations, confirmed, cancelled}) {
            cards.add(card);
        }

        JPanel todayArea = new JPanel(new BorderLayout(0, 8));
        todayArea.setOpaque(false);
        todayArea.add(Theme.heading("Today's Schedule"), BorderLayout.NORTH);
        todayArea.add(Theme.scroll(todayTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(todayArea, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        LocalDate slotDate = system.getNextSlotDate();
        TimeSlot slot = system.getNextSlot();
        String when = slotDate.equals(LocalDate.now()) ? "today" : "tomorrow";

        totalTables.setValue(system.getTotalTables(), "registered in the restaurant");
        availableTables.setValue(system.countAvailableTables(slotDate, slot),
                "free at " + slot + " " + when);
        totalCustomers.setValue(system.getTotalCustomers(), "registered customers");
        todaysReservations.setValue(system.countTodaysReservations(), "not cancelled");
        confirmed.setValue(system.countReservationsByStatus(ReservationStatus.CONFIRMED), "all dates");
        cancelled.setValue(system.countReservationsByStatus(ReservationStatus.CANCELLED), "kept in history");

        todayModel.setRowCount(0);
        for (Reservation r : system.getReservationsForDate(LocalDate.now())) {
            todayModel.addRow(new Object[]{r.getTimeSlot().getLabel(), "Table " + r.getTable().getId(),
                    r.getCustomer().getName(), r.getGuests(), r.getStatus().toString()});
        }
    }

    /** One coloured statistic tile. */
    private static class StatCard extends JPanel {
        private final JLabel valueLabel = new JLabel("0");
        private final JLabel noteLabel = new JLabel(" ");

        StatCard(String title, Color accent) {
            super(new BorderLayout(0, 2));
            setBackground(Theme.CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    BorderFactory.createCompoundBorder(Theme.accentStrip(accent), new EmptyBorder(12, 16, 12, 16))));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(Theme.BODY_BOLD);
            titleLabel.setForeground(Theme.MUTED);
            valueLabel.setFont(Theme.BIG_NUMBER_FONT);
            valueLabel.setForeground(accent);
            noteLabel.setFont(Theme.SMALL_BOLD);
            noteLabel.setForeground(Theme.MUTED);

            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
            add(noteLabel, BorderLayout.SOUTH);
        }

        void setValue(int value, String note) {
            valueLabel.setText(String.valueOf(value));
            noteLabel.setText(note);
        }
    }
}
