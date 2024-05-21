package lk.gov.health.phsp.enums;

/**
 * VehicleType Enum
 * 
 * Enumerates different types of vehicles and assigns a color to each.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    Ambulance("Ambulance", true, "rgba(255, 99, 132, 0.6)"),
    Bowser("Bowser", true, "rgba(54, 162, 235, 0.6)"),
    Bus("Bus", true, "rgba(255, 206, 86, 0.6)"),
    Cab("Cab", true, "rgba(75, 192, 192, 0.6)"),
    Car("Car", true, "rgba(153, 102, 255, 0.6)"),
    FoggingMachine("Fogging Machine", false, "rgba(255, 159, 64, 0.6)"),
    Generator("Generator", false, "rgba(199, 199, 199, 0.6)"), // Example of a lighter shade for non-vehicle items
    GullyBowser("Gully Bowser", true, "rgba(255, 129, 102, 0.6)"),
    Incinerator("Incinerator", false, "rgba(220, 20, 60, 0.6)"),
    ServiceStation("Service Station", false, "rgba(200, 60, 60, 0.6)"),
    Jeep("Jeep", true, "rgba(0, 255, 255, 0.6)"),
    Lorry("Lorry", true, "rgba(0, 0, 128, 0.6)"),
    ThreeWheeler("Three Wheeler", true, "rgba(128, 0, 128, 0.6)"),
    Tractor("Tractor", true, "rgba(128, 128, 0, 0.6)"),
    Van("Van", true, "rgba(0, 128, 0, 0.6)");

    private final String label;
    private final boolean isVehicle;
    private final String color; // New field to store the color

    private VehicleType(String label, boolean isVehicle, String color) {
        this.label = label;
        this.isVehicle = isVehicle;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVehicle() {
        return isVehicle;
    }

    public String getColor() {
        return color;
    }
}
