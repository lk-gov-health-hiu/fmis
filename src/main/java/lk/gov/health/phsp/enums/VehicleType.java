package lk.gov.health.phsp.enums;

/**
 * VehicleType Enum
 * 
 * Enumerates different types of vehicles.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Ambulance("Ambulance"),
    Bowser("Bowser"),
    Bus("Bus"),
    Cab("Cab"),
    Car("Car"),
    FoggingMachine("Fogging Machine"),
    Generator("Generator"),
    GullyBowser("Gully Bowser"),
    Incinerator("Incinerator"),
    Jeep("Jeep"),
    Lorry("Lorry"),
    ThreeWheeler("Three Wheeler"),
    Tractor("Tractor"),
    Van("Van");

    private final String label;

    private VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
