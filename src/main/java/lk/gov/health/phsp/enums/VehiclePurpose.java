package lk.gov.health.phsp.enums;

public enum VehiclePurpose {
    Ambulance("Patient Transport", "rgba(255, 99, 132, 0.6)"), // Example color in RGBA format
    Generator("Generator", "rgba(54, 162, 235, 0.6)"),
    Suwasariya("Suwaseriya", "rgba(255, 206, 86, 0.6)"),
    PreventiveActivities("Health Services", "rgba(75, 192, 192, 0.6)"),
    BloodTransportation("Blood Transportation", "rgba(153, 102, 255, 0.6)"),
    DrugDistribution("Drug Distribution", "rgba(255, 159, 64, 0.6)");

    private final String label;
    private final String color; // Color attribute

    VehiclePurpose(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
