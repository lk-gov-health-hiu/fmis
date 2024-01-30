package lk.gov.health.phsp.enums;

/**
 * VehicleType Enum
 * 
 * Enumerates different types of vehicles.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Ambulance("Ambulance", true),
    Bowser("Bowser", true),
    Bus("Bus", true),
    Cab("Cab", true),
    Car("Car", true),
    FoggingMachine("Fogging Machine", false),
    Generator("Generator", false),
    GullyBowser("Gully Bowser", true),
    Incinerator("Incinerator", false),
    Jeep("Jeep", true),
    Lorry("Lorry", true),
    ThreeWheeler("Three Wheeler", true),
    Tractor("Tractor", true),
    Van("Van", true);

    private final String label;
    private final boolean isVehicle;

    private VehicleType(String label, boolean isVehicle) {
        this.label = label;
        this.isVehicle = isVehicle;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVehicle() {
        return isVehicle;
    }
}
