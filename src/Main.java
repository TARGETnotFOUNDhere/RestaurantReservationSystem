import gui.MainFrame;
import gui.Theme;
import service.RestaurantSystem;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Theme.install();
                RestaurantSystem system = new RestaurantSystem();
                system.loadSampleData();
                new MainFrame(system).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "The application could not start: " + ex.getMessage(),
                        "Startup error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
