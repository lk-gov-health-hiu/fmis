package lk.gov.health.phsp.enums;

/**
 * VehicleType Enum
 * 
 * Enumerates different types of vehicles.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Bus("Bus"),
    Car("Car"),
    Van("Van"),
    Jeep("Jeep"),
    Lorry("Lorry"),
    Ambulance("Ambulance"),
    Cab("Cab"),
    ThreeWheeler("Three Wheeler"),
    Generator("Generator");

    private final String label;

    private VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
