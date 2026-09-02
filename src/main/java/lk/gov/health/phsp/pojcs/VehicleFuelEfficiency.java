package lk.gov.health.phsp.pojcs;

import lk.gov.health.phsp.entity.Vehicle;

/**
 * Average fuel consumption of a vehicle over a period, derived from
 * consecutive odometer readings recorded on its FuelTransactions.
 *
 * @author Dr M H B Ariyaratne
 */
public class VehicleFuelEfficiency {

    private final Vehicle vehicle;
    private final Double totalLiters;
    private final Double totalKm;

    public VehicleFuelEfficiency(Vehicle vehicle, Double totalLiters, Double totalKm) {
        this.vehicle = vehicle;
        this.totalLiters = totalLiters;
        this.totalKm = totalKm;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Double getTotalLiters() {
        return totalLiters;
    }

    public Double getTotalKm() {
        return totalKm;
    }

    public Double getLitersPerKm() {
        if (totalKm == null || totalKm <= 0) {
            return 0.0;
        }
        return totalLiters / totalKm;
    }
}
