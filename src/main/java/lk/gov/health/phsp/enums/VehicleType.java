package lk.gov.health.phsp.enums;

/**
 * VehicleType Enum
 * 
 * Enumerates different types of vehicles and assigns a color to each.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum VehicleType {
    // maxRequestQuantity = maximum fuel (liters) allowed per request order for this type.
    // null means no limit (any quantity allowed). Values are policy limits and are
    // very unlikely to change; update them here if the policy changes.
    Ambulance("Ambulance", true, "rgba(255, 99, 132, 0.6)", 95.0),
    Bowser("Bowser", true, "rgba(54, 162, 235, 0.6)", 150.0),
    Bus("Bus", true, "rgba(255, 206, 86, 0.6)", 350.0),
    Cab("Cab", true, "rgba(75, 192, 192, 0.6)", 80.0),
    Car("Car", true, "rgba(153, 102, 255, 0.6)", 65.0),
    FoggingMachine("Fogging Machine", false, "rgba(255, 159, 64, 0.6)", 40.0),
    Generator("Generator", false, "rgba(199, 199, 199, 0.6)", 350.0), // Example of a lighter shade for non-vehicle items
    GullyBowser("Gully Bowser", true, "rgba(255, 129, 102, 0.6)", 100.0),
    Incinerator("Incinerator", false, "rgba(220, 20, 60, 0.6)", 1000.0),
    ServiceStation("Service Station", false, "rgba(200, 60, 60, 0.6)", null), // no limit
    Jeep("Jeep", true, "rgba(0, 255, 255, 0.6)", 110.0),
    Lorry("Lorry", true, "rgba(0, 0, 128, 0.6)", 300.0),
    ThreeWheeler("Three Wheeler", true, "rgba(128, 0, 128, 0.6)", 10.0),
    Tractor("Tractor", true, "rgba(128, 128, 0, 0.6)", 60.0),
    Van("Van", true, "rgba(0, 128, 0, 0.6)", 80.0);

    private final String label;
    private final boolean isVehicle;
    private final String color; // New field to store the color
    private final Double maxRequestQuantity; // max liters allowed per request order; null = no limit

    private VehicleType(String label, boolean isVehicle, String color, Double maxRequestQuantity) {
        this.label = label;
        this.isVehicle = isVehicle;
        this.color = color;
        this.maxRequestQuantity = maxRequestQuantity;
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

    public Double getMaxRequestQuantity() {
        return maxRequestQuantity;
    }
}
