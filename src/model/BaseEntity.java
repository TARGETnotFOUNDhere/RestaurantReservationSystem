package model;

/**
 * Common parent of every record kept by the system (table, customer, reservation).
 * It holds the shared ID and forces each subclass to describe itself in getSummary().
 */
public abstract class BaseEntity {
    private final int id;

    protected BaseEntity(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive whole number.");
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /** One-line, human-readable description of the record. */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getSummary();
    }
}
