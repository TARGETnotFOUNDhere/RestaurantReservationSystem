package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Central place for colours, fonts and small component factories so every screen looks the same. */
public final class Theme {
    public static final Color SIDEBAR = new Color(0x1E293B);
    public static final Color SIDEBAR_HOVER = new Color(0x334155);
    public static final Color ACCENT = new Color(0x4F46E5);
    public static final Color ACCENT_HOVER = new Color(0x4338CA);
    public static final Color BACKGROUND = new Color(0xF1F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_HOVER = new Color(0xB91C1C);
    public static final Color WARNING = new Color(0xD97706);
    public static final Color INFO = new Color(0x2563EB);

    private static final String FONT_FAMILY = pickFontFamily();
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font HEADING_FONT = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font BODY_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font BIG_NUMBER_FONT = new Font(FONT_FAMILY, Font.BOLD, 34);

    private Theme() {
    }

    private static String pickFontFamily() {
        String[] preferred = {"Segoe UI", "Helvetica Neue", "SF Pro Text", "Roboto", "Ubuntu"};
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String family : preferred) {
            if (available.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Call once at start-up so dialogs use the same font. */
    public static void install() {
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("CheckBox.font", BODY_FONT);
    }

    // --------------------------------------------------------------- buttons

    public static JButton primaryButton(String text) {
        return new AppButton(text, ACCENT, ACCENT_HOVER, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return new AppButton(text, new Color(0xE2E8F0), new Color(0xCBD5E1), TEXT);
    }

    public static JButton dangerButton(String text) {
        return new AppButton(text, DANGER, DANGER_HOVER, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return new AppButton(text, SUCCESS, new Color(0x15803D), Color.WHITE);
    }

    public static JButton navButton(String text) {
        JButton button = new AppButton(text, SIDEBAR, SIDEBAR_HOVER, Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 22, 12, 22));
        return button;
    }

    public static void setNavSelected(JButton button, boolean selected) {
        button.putClientProperty("selected", selected);
        button.repaint();
    }

    /** Custom-painted rounded button, so colours look identical on every operating system. */
    private static class AppButton extends JButton {
        private final Color base;
        private final Color hover;

        AppButton(String text, Color base, Color hover, Color foreground) {
            super(text);
            this.base = base;
            this.hover = hover;
            setForeground(foreground);
            setFont(BODY_BOLD);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));
            Color fill = selected ? ACCENT : (getModel().isRollover() ? hover : base);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------ fields and cards

    public static JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCBD5E1), 1, true), new EmptyBorder(7, 10, 7, 10)));
        return field;
    }

    public static <T> JComboBox<T> comboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(BODY_FONT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(190, 38));
        return combo;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING_FONT);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));
        return panel;
    }

    /** A small caption above an input component. */
    public static JPanel labeled(String caption, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(caption);
        label.setFont(SMALL_BOLD);
        label.setForeground(MUTED);
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    /** A left-aligned horizontal row of components. */
    public static JPanel row(JComponent... items) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent item : items) {
            panel.add(item);
        }
        return panel;
    }

    public static Color statusColor(String status) {
        if (status.startsWith("CONFIRMED") || status.startsWith("Available")) {
            return SUCCESS;
        } else if (status.startsWith("CANCELLED")) {
            return DANGER;
        } else if (status.startsWith("COMPLETED")) {
            return INFO;
        } else if (status.startsWith("Reserved")) {
            return WARNING;
        }
        return TEXT;
    }

    // ---------------------------------------------------------------- tables

    public static class ReadOnlyTableModel extends DefaultTableModel {
        public ReadOnlyTableModel(String... columns) {
            super(columns, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    /** @param statusColumn index of the column to colour by status, or -1 for none */
    public static JTable createTable(DefaultTableModel model, int statusColumn) {
        JTable table = new JTable(model);
        table.setFont(BODY_FONT);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE0E7FF));
        table.setSelectionForeground(TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new RowRenderer(statusColumn));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 40));
        return table;
    }

    public static JScrollPane scroll(JComponent view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(new LineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    private static class RowRenderer extends DefaultTableCellRenderer {
        private final int statusColumn;

        RowRenderer(int statusColumn) {
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(BODY_FONT);
            setForeground(TEXT);
            if (!selected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
            }
            if (column == statusColumn && value != null) {
                setForeground(statusColor(value.toString()));
                setFont(BODY_BOLD);
            }
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setOpaque(true);
            setBackground(SIDEBAR);
            setForeground(Color.WHITE);
            setFont(BODY_BOLD);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }
    }

    // --------------------------------------------------------------- dialogs

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int choice = JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    /** Left accent strip used on dashboard cards. */
    public static MatteBorder accentStrip(Color color) {
        return new MatteBorder(0, 5, 0, 0, color);
    }
}
