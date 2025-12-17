package lk.gov.health.phsp.enums;

/**
 * Enum representing different types of system messages with associated visual properties
 *
 * @author System
 */
public enum MessageType {
    SYSTEM_ALERT("System Alert", "bi-exclamation-triangle-fill", "danger"),
    ANNOUNCEMENT("Announcement", "bi-megaphone-fill", "info"),
    MAINTENANCE("Maintenance Notice", "bi-tools", "warning"),
    POLICY_UPDATE("Policy Update", "bi-file-text-fill", "primary"),
    TRAINING("Training Notice", "bi-book-fill", "success"),
    GENERAL("General Message", "bi-envelope-fill", "secondary");

    private final String label;
    private final String icon;        // Bootstrap icon class
    private final String colorClass;  // Bootstrap color class

    MessageType(String label, String icon, String colorClass) {
        this.label = label;
        this.icon = icon;
        this.colorClass = colorClass;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorClass() {
        return colorClass;
    }
}
