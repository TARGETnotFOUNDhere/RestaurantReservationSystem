package model;

/** Life-cycle status of a reservation. */
public enum ReservationStatus {
    CONFIRMED,
    CANCELLED,
    COMPLETED;

    /** A cancelled reservation frees its table; confirmed and completed ones keep it occupied. */
    public boolean occupiesTable() {
        return this != CANCELLED;
    }
}
