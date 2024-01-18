package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Ambulance("Ambulance"),
    Generator("Generator"),
    Suwasariya("Suwasariya"),
    PreventiveActivities("Preventive Activities"),
    BloodTransportation("Blood Transportation"),
    DrugDistribution("Drug Distribution")
    ;

    private final String label;

    private VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
