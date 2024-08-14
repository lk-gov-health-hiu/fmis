package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.util.Date;

public class FuelIssuedSummary implements Serializable {

    private Date issuedDate;
    private String institutionName;
    private String fromInstitutionName;
    private String toInstitutionName;
    private Double sumOfIssuedQty;
    private Long institutionId;
    private Long fromInstitutionId;
    private Long toInstitutionId;

    // Constructor
    public FuelIssuedSummary(Date issuedDate, String fromInstitutionName, Long fromInstitutionId,
            String toInstitutionName, Long toInstitutionId, Double sumOfIssuedQty) {
        this.issuedDate = issuedDate;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.sumOfIssuedQty = sumOfIssuedQty;
        this.fromInstitutionId = fromInstitutionId;
        this.toInstitutionId = toInstitutionId;
    }

    public FuelIssuedSummary(Date issuedDate, String institutionName, Long institutionId, Double sumOfIssuedQty) {
        this.issuedDate = issuedDate;
        this.institutionName = institutionName;
        this.institutionId = institutionId;
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

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public FuelIssuedSummary(String toInstitutionName, Long toInstitutionId, Double sumOfIssuedQty) {
        this.toInstitutionName = toInstitutionName;
        this.toInstitutionId = toInstitutionId;
        this.sumOfIssuedQty = sumOfIssuedQty;
    }

}
