package lk.gov.health.phsp.enums;

/**
 * DriverAllocationType Enum
 *
 * Defines whether a driver is temporarily or permanently allocated.
 *
 * @author Dr M H B Ariyaratne
 */
public enum DriverAllocationType {
    Temporary("Temporary"),
    Permanent("Permanent");

    private final String label;

    private DriverAllocationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
