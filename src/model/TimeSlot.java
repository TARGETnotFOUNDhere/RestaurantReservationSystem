package model;

import java.time.LocalTime;

/** The fixed one-hour time slots in which a table can be reserved. */
public enum TimeSlot {
    SLOT_12_PM("12:00 PM", LocalTime.of(12, 0)),
    SLOT_1_PM("1:00 PM", LocalTime.of(13, 0)),
    SLOT_2_PM("2:00 PM", LocalTime.of(14, 0)),
    SLOT_7_PM("7:00 PM", LocalTime.of(19, 0)),
    SLOT_8_PM("8:00 PM", LocalTime.of(20, 0)),
    SLOT_9_PM("9:00 PM", LocalTime.of(21, 0));

    private static final long SLOT_LENGTH_HOURS = 1;

    private final String label;
    private final LocalTime startTime;

    TimeSlot(String label, LocalTime startTime) {
        this.label = label;
        this.startTime = startTime;
    }

    public String getLabel() {
        return label;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return startTime.plusHours(SLOT_LENGTH_HOURS);
    }

    /** Returns the first slot that has not finished at the given time, or null if all are over. */
    public static TimeSlot firstSlotNotFinishedAt(LocalTime time) {
        for (TimeSlot slot : values()) {
            if (time.isBefore(slot.getEndTime())) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
