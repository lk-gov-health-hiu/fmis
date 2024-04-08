package lk.gov.health.phsp.pojcs;

import java.util.Date;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.VehiclePurpose;
import lk.gov.health.phsp.enums.VehicleType;

public class InstitutionCount {

    private String countLabel;
    private VehiclePurpose vehiclePurpose;
    private VehicleType vehicleType;
    private Institution institution;
    private Institution fuelStation;
    private Area area;
    private Long count;
    private Date date;
    private Long id;
    private Double requestedQty; // Calculate from JPQL
    private Double issuedQty; //Calculate from JPQL
    private Double totalGrant; //have to be assigned from a preferance
    private Double toBeIssuedQty; // requested qty - issuedQty
    private Double remainingQty; //Total grant value minus (issued + to be issue)

    public InstitutionCount() {
    }

    public InstitutionCount(VehicleType vehicleType, Double requestedQty) {
        this.vehicleType = vehicleType;
        this.requestedQty = requestedQty;
    }
    
    

    public InstitutionCount(Institution institution, Double requestedQty, Double issuedQty) {
        this.institution = institution;
        this.requestedQty = requestedQty;
        this.issuedQty = issuedQty;
    }

    public InstitutionCount(String countLabel, Double requestedQty) {
        this.countLabel = countLabel;
        this.requestedQty = requestedQty;
    }

    public InstitutionCount(VehiclePurpose vehiclePurpose, Double requestedQty) {
        this.vehiclePurpose = vehiclePurpose;
        this.requestedQty = requestedQty;
    }

    
    
    
    public Double getTotalGrant() {
        return totalGrant;
    }

    public void setTotalGrant(Double totalGrant) {
        this.totalGrant = totalGrant;
    }

    public Double getToBeIssuedQty() {
        return toBeIssuedQty;
    }

    public void setToBeIssuedQty(Double toBeIssuedQty) {
        this.toBeIssuedQty = toBeIssuedQty;
    }

    public Double getRemainingQty() {
        remainingQty = getRequestedQty() - getIssuedQty();
        return remainingQty;
    }

    public void setRemainingQty(Double remainingQty) {
        this.remainingQty = remainingQty;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getFuelStation() {
        return fuelStation;
    }

    public void setFuelStation(Institution fuelStation) {
        this.fuelStation = fuelStation;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getRequestedQty() {
        if (requestedQty == null) {
            requestedQty = 0.0;
        }
        return requestedQty;
    }

    public void setRequestedQty(Double requestedQty) {
        this.requestedQty = requestedQty;
    }

    public Double getIssuedQty() {
        if (issuedQty == null) {
            issuedQty = 0.0;
        }
        return issuedQty;
    }

    public void setIssuedQty(Double issuedQty) {
        this.issuedQty = issuedQty;
    }

    public String getCountLabel() {
        return countLabel;
    }

    public void setCountLabel(String countLabel) {
        this.countLabel = countLabel;
    }

    public VehiclePurpose getVehiclePurpose() {
        return vehiclePurpose;
    }

    public void setVehiclePurpose(VehiclePurpose vehiclePurpose) {
        this.vehiclePurpose = vehiclePurpose;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

}
