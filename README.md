# Restaurant Table Reservation System
Case Study No. 127 - Java (Swing) - B.Tech CSE Semester III

Run `src/Main.java` (JDK 17 or newer). No external libraries or database are needed; all data is kept in memory
with Java Collections and sample data is loaded on start-up.

## Packages
- `model`   - Customer, RestaurantTable, Reservation, TimeSlot, ReservationStatus (+ abstract BaseEntity)
- `service` - RestaurantSystem (all business rules and TreeMap storage)
- `gui`     - MainFrame, DashboardPanel, TablePanel, CustomerPanel, ReservationPanel, SchedulePanel (+ Theme, RefreshablePanel)
- `Main`    - starts the Swing application with SwingUtilities.invokeLater
