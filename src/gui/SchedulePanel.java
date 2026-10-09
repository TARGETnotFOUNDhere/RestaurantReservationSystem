package gui;

import model.Reservation;
import model.ReservationStatus;
import model.TimeSlot;
import service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SchedulePanel extends RefreshablePanel {
    private final JTextField dateField = Theme.textField(9);
    private final JCheckBox showCancelled = new JCheckBox("Show cancelled", true);
    private final JLabel scheduleTitle = Theme.heading("");
    private final JLabel summaryLabel = Theme.mutedLabel("");
    private final JPanel slotList = new JPanel();

    private LocalDate selectedDate = LocalDate.now();

    public SchedulePanel(RestaurantSystem system) {
        super(system);
        add(createHeader("Daily Schedule", "See every reservation of a day, grouped by time slot."),
                BorderLayout.NORTH);

        slotList.setLayout(new BoxLayout(slotList, BoxLayout.Y_AXIS));
        slotList.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(slotList, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createControls(), BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createControls() {
        JButton previousButton = Theme.secondaryButton("< Prev");
        JButton nextButton = Theme.secondaryButton("Next >");
        JButton todayButton = Theme.secondaryButton("Today");
        JButton showButton = Theme.primaryButton("Show Schedule");

        previousButton.addActionListener(e -> showDate(selectedDate.minusDays(1)));
        nextButton.addActionListener(e -> showDate(selectedDate.plusDays(1)));
        todayButton.addActionListener(e -> showDate(LocalDate.now()));
        showButton.addActionListener(e -> runAction(() -> showDate(readDate(dateField))));
        showCancelled.setOpaque(false);
        showCancelled.addActionListener(e -> refresh());

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Theme.row(
                Theme.labeled(" ", previousButton),
                Theme.labeled("Date (dd-MM-yyyy)", dateField),
                Theme.labeled(" ", nextButton),
                Theme.labeled(" ", todayButton),
                Theme.labeled(" ", showButton),
                Theme.labeled(" ", showCancelled)));
        return card;
    }

    private void showDate(LocalDate date) {
        selectedDate = date;
        refresh();
    }

    @Override
    public void refresh() {
        dateField.setText(selectedDate.format(Reservation.DATE_FORMAT));
        scheduleTitle.setText("DAILY RESERVATION SCHEDULE  -  Date: " + selectedDate.format(Reservation.DATE_FORMAT));

        // Group the (already sorted) reservations by slot.
        Map<TimeSlot, List<Reservation>> bySlot = new EnumMap<>(TimeSlot.class);
        int confirmed = 0;
        int cancelled = 0;
        for (Reservation r : system.getReservationsForDate(selectedDate)) {
            if (r.getStatus() == ReservationStatus.CANCELLED) {
                cancelled++;
                if (!showCancelled.isSelected()) {
                    continue;
                }
            } else if (r.isConfirmed()) {
                confirmed++;
            }
            bySlot.computeIfAbsent(r.getTimeSlot(), slot -> new ArrayList<>()).add(r);
        }

        slotList.removeAll();
        JPanel titleCard = Theme.card();
        titleCard.setLayout(new GridLayout(2, 1, 0, 2));
        titleCard.add(scheduleTitle);
        summaryLabel.setText(confirmed + " confirmed, " + cancelled + " cancelled");
        titleCard.add(summaryLabel);
        addSection(titleCard);

        for (TimeSlot slot : TimeSlot.values()) {
            addSection(createSlotCard(slot, bySlot.getOrDefault(slot, new ArrayList<>())));
        }
        slotList.revalidate();
        slotList.repaint();
    }

    private void addSection(JPanel section) {
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));
        slotList.add(section);
        slotList.add(Box.createVerticalStrut(10));
    }

    private JPanel createSlotCard(TimeSlot slot, List<Reservation> reservations) {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel slotLabel = new JLabel(slot.getLabel());
        slotLabel.setFont(Theme.HEADING_FONT);
        slotLabel.setForeground(Theme.ACCENT);
        slotLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        slotLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(slotLabel);

        if (reservations.isEmpty()) {
            JLabel empty = Theme.mutedLabel("No reservations");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(empty);
        }
        for (Reservation r : reservations) {
            JPanel line = new JPanel(new BorderLayout());
            line.setOpaque(false);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            line.setBorder(new EmptyBorder(3, 0, 3, 0));

            JLabel details = new JLabel("Table " + r.getTable().getId() + "  -  " + r.getCustomer().getName()
                    + "  -  " + r.getGuests() + " guests");
            details.setFont(Theme.BODY_FONT);
            details.setForeground(Theme.TEXT);
            JLabel status = new JLabel(r.getStatus().toString());
            status.setFont(Theme.BODY_BOLD);
            status.setForeground(Theme.statusColor(r.getStatus().toString()));

            line.add(details, BorderLayout.WEST);
            line.add(status, BorderLayout.EAST);
            card.add(line);
        }
        return card;
    }
}
