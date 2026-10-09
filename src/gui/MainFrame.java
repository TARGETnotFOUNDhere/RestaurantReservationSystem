package gui;

import service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main window: a dark sidebar for navigation and a CardLayout that switches between the screens. */
public class MainFrame extends JFrame {
    public static final String DASHBOARD = "Dashboard";
    public static final String TABLES = "Tables";
    public static final String CUSTOMERS = "Customers";
    public static final String RESERVATIONS = "Reservations";
    public static final String SCHEDULE = "Daily Schedule";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, RefreshablePanel> screens = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame(RestaurantSystem system) {
        super("Restaurant Table Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        screens.put(DASHBOARD, new DashboardPanel(system));
        screens.put(TABLES, new TablePanel(system));
        screens.put(CUSTOMERS, new CustomerPanel(system));
        screens.put(RESERVATIONS, new ReservationPanel(system));
        screens.put(SCHEDULE, new SchedulePanel(system));

        for (Map.Entry<String, RefreshablePanel> entry : screens.entrySet()) {
            cards.add(entry.getValue(), entry.getKey());
        }

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        showScreen(DASHBOARD);   // the application opens on the dashboard
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(24, 12, 24, 12));

        JLabel name = new JLabel("Restaurant");
        name.setFont(Theme.TITLE_FONT);
        name.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Table Reservation System");
        subtitle.setFont(Theme.BODY_FONT);
        subtitle.setForeground(new Color(0x94A3B8));
        name.setBorder(new EmptyBorder(0, 10, 0, 0));
        subtitle.setBorder(new EmptyBorder(0, 10, 28, 0));
        sidebar.add(name);
        sidebar.add(subtitle);

        for (String screenName : screens.keySet()) {
            JButton button = Theme.navButton(screenName);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            button.addActionListener(e -> showScreen(screenName));
            navButtons.put(screenName, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("Case Study No. 127");
        footer.setFont(Theme.SMALL_BOLD);
        footer.setForeground(new Color(0x94A3B8));
        footer.setBorder(new EmptyBorder(0, 10, 0, 0));
        sidebar.add(footer);
        return sidebar;
    }

    /** Reloads the chosen screen (so it shows the latest data) and brings it to the front. */
    public void showScreen(String name) {
        screens.get(name).refresh();
        cardLayout.show(cards, name);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            Theme.setNavSelected(entry.getValue(), entry.getKey().equals(name));
        }
    }
}
