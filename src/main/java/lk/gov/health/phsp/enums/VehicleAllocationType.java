package lk.gov.health.phsp.enums;

/**
 * VehicleAllocationType Enum
 *
 * Defines whether a vehicle is temporarily or permanently allocated.
 *
 * @author Dr M H B Ariyaratne
 */
public enum VehicleAllocationType {
    Temporary("Temporary"),
    Permanent("Permanent");

    private final String label;

    private VehicleAllocationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
