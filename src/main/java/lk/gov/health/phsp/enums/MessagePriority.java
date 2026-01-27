package lk.gov.health.phsp.enums;

/**
 * Enum representing priority levels for system messages
 *
 * @author System
 */
public enum MessagePriority {
    HIGH("High", "danger", 3),
    MEDIUM("Medium", "warning", 2),
    LOW("Low", "info", 1);

    private final String label;
    private final String colorClass;  // Bootstrap color class
    private final int sortOrder;      // For sorting (higher number = higher priority)

    MessagePriority(String label, String colorClass, int sortOrder) {
        this.label = label;
        this.colorClass = colorClass;
        this.sortOrder = sortOrder;
    }

    public String getLabel() {
        return label;
    }

    public String getColorClass() {
        return colorClass;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
