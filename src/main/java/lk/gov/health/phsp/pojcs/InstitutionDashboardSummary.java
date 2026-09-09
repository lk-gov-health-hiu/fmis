package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.util.List;

/**
 * Precomputed numbers and chart data for one institution's dashboard.
 * Built by InstitutionDashboardApplicationController and cached per
 * institution for a day.
 *
 * @author Dr M H B Ariyaratne
 */
public class InstitutionDashboardSummary implements Serializable {

    private Double requestedThisMonth;
    private Double requestedLastMonth;
    private Double issuedThisMonth;
    private Double issuedLastMonth;
    private Double pendingIssueQuantity;
    private Long pendingIssueCount;

    private Long notSubmittedForPaymentCount;
    private Double notSubmittedForPaymentQuantity;

    private Long rejectedCpcBillCount;
    private Long acceptedCpcBillCount;

    private List<InstitutionCount> top10VehiclesByUsage;
    private List<InstitutionCount> top10VehiclesByUsageLastMonth;
    private List<VehicleFuelEfficiency> top10VehiclesByLitersPerKm;
    private int vehiclesExcludedFromEfficiencyChart;
    private List<InstitutionCount> top10VehiclesByDistanceLastMonth;

    public Double getRequestedThisMonth() {
        return requestedThisMonth;
    }

    public void setRequestedThisMonth(Double requestedThisMonth) {
        this.requestedThisMonth = requestedThisMonth;
    }

    public Double getRequestedLastMonth() {
        return requestedLastMonth;
    }

    public void setRequestedLastMonth(Double requestedLastMonth) {
        this.requestedLastMonth = requestedLastMonth;
    }

    public Double getIssuedThisMonth() {
        return issuedThisMonth;
    }

    public void setIssuedThisMonth(Double issuedThisMonth) {
        this.issuedThisMonth = issuedThisMonth;
    }

    public Double getIssuedLastMonth() {
        return issuedLastMonth;
    }

    public void setIssuedLastMonth(Double issuedLastMonth) {
        this.issuedLastMonth = issuedLastMonth;
    }

    public Double getPendingIssueQuantity() {
        return pendingIssueQuantity;
    }

    public void setPendingIssueQuantity(Double pendingIssueQuantity) {
        this.pendingIssueQuantity = pendingIssueQuantity;
    }

    public Long getPendingIssueCount() {
        return pendingIssueCount;
    }

    public void setPendingIssueCount(Long pendingIssueCount) {
        this.pendingIssueCount = pendingIssueCount;
    }

    public Long getNotSubmittedForPaymentCount() {
        return notSubmittedForPaymentCount;
    }

    public void setNotSubmittedForPaymentCount(Long notSubmittedForPaymentCount) {
        this.notSubmittedForPaymentCount = notSubmittedForPaymentCount;
    }

    public Double getNotSubmittedForPaymentQuantity() {
        return notSubmittedForPaymentQuantity;
    }

    public void setNotSubmittedForPaymentQuantity(Double notSubmittedForPaymentQuantity) {
        this.notSubmittedForPaymentQuantity = notSubmittedForPaymentQuantity;
    }

    public Long getRejectedCpcBillCount() {
        return rejectedCpcBillCount;
    }

    public void setRejectedCpcBillCount(Long rejectedCpcBillCount) {
        this.rejectedCpcBillCount = rejectedCpcBillCount;
    }

    public Long getAcceptedCpcBillCount() {
        return acceptedCpcBillCount;
    }

    public void setAcceptedCpcBillCount(Long acceptedCpcBillCount) {
        this.acceptedCpcBillCount = acceptedCpcBillCount;
    }

    public List<InstitutionCount> getTop10VehiclesByUsage() {
        return top10VehiclesByUsage;
    }

    public void setTop10VehiclesByUsage(List<InstitutionCount> top10VehiclesByUsage) {
        this.top10VehiclesByUsage = top10VehiclesByUsage;
    }

    public List<InstitutionCount> getTop10VehiclesByUsageLastMonth() {
        return top10VehiclesByUsageLastMonth;
    }

    public void setTop10VehiclesByUsageLastMonth(List<InstitutionCount> top10VehiclesByUsageLastMonth) {
        this.top10VehiclesByUsageLastMonth = top10VehiclesByUsageLastMonth;
    }

    public List<VehicleFuelEfficiency> getTop10VehiclesByLitersPerKm() {
        return top10VehiclesByLitersPerKm;
    }

    public void setTop10VehiclesByLitersPerKm(List<VehicleFuelEfficiency> top10VehiclesByLitersPerKm) {
        this.top10VehiclesByLitersPerKm = top10VehiclesByLitersPerKm;
    }

    public int getVehiclesExcludedFromEfficiencyChart() {
        return vehiclesExcludedFromEfficiencyChart;
    }

    public void setVehiclesExcludedFromEfficiencyChart(int vehiclesExcludedFromEfficiencyChart) {
        this.vehiclesExcludedFromEfficiencyChart = vehiclesExcludedFromEfficiencyChart;
    }

    public List<InstitutionCount> getTop10VehiclesByDistanceLastMonth() {
        return top10VehiclesByDistanceLastMonth;
    }

    public void setTop10VehiclesByDistanceLastMonth(List<InstitutionCount> top10VehiclesByDistanceLastMonth) {
        this.top10VehiclesByDistanceLastMonth = top10VehiclesByDistanceLastMonth;
    }
}
