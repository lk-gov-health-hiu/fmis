package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.util.Date;

public class FuelIssuedSummary implements Serializable {

    private Date issuedDate;
    private String fromInstitutionName;
    private String toInstitutionName;
    private Double sumOfIssuedQty;
    private Long fromInstitutionId;
    private Long toInstitutionId;

    // Constructor
    public FuelIssuedSummary(Date issuedDate, String fromInstitutionName, String toInstitutionName, Double sumOfIssuedQty) {
        this.issuedDate = issuedDate;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.sumOfIssuedQty = sumOfIssuedQty;
    }

    // Getters and setters
    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getFromInstitutionName() {
        return fromInstitutionName;
    }

    
    
    public void setFromInstitutionName(String fromInstitutionName) {
        this.fromInstitutionName = fromInstitutionName;
    }

    public String getToInstitutionName() {
        return toInstitutionName;
    }

    public void setToInstitutionName(String toInstitutionName) {
        this.toInstitutionName = toInstitutionName;
    }

    public Double getSumOfIssuedQty() {
        return sumOfIssuedQty;
    }

    public void setSumOfIssuedQty(Double sumOfIssuedQty) {
        this.sumOfIssuedQty = sumOfIssuedQty;
    }

    // toString method, if needed for debugging
    @Override
    public String toString() {
        return "FuelIssuedSummary{"
                + "issuedDate=" + issuedDate
                + ", fromInstitutionName='" + fromInstitutionName + '\''
                + ", toInstitutionName='" + toInstitutionName + '\''
                + ", sumOfIssuedQty=" + sumOfIssuedQty
                + '}';
    }

    public Long getFromInstitutionId() {
        return fromInstitutionId;
    }

    public void setFromInstitutionId(Long fromInstitutionId) {
        this.fromInstitutionId = fromInstitutionId;
    }

    public Long getToInstitutionId() {
        return toInstitutionId;
    }

    public void setToInstitutionId(Long toInstitutionId) {
        this.toInstitutionId = toInstitutionId;
    }
}
