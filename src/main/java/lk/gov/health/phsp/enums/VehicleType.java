package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Ambulance("Ambulance"),
    Suwasariya("Suwasariya"),
    HealthService("Health Service"),
    Other("Other");

    private final String label;

    private VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
