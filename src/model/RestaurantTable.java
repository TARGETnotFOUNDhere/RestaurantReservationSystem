package model;

public class RestaurantTable extends BaseEntity {
    private int capacity;

    public RestaurantTable(int id, int capacity) {
        super(id);
        validateCapacity(capacity);
        this.capacity = capacity;
    }

    public static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be a positive number.");
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        validateCapacity(capacity);
        this.capacity = capacity;
    }

    @Override
    public String getSummary() {
        return "Table " + getId() + " (seats " + capacity + ")";
    }
}
