package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lk.gov.health.phsp.entity.Bill;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionDashboardSummary;
import lk.gov.health.phsp.pojcs.VehicleFuelEfficiency;

/**
 * Backs dashboardInstitution.xhtml - the fuel-usage/payment-status
 * dashboard for a single (non-national) institution.
 *
 * @author Dr M H B Ariyaratne
 */
@Named
@SessionScoped
public class InstitutionDashboardController implements Serializable {

    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionDashboardApplicationController institutionDashboardApplicationController;
    @Inject
    private FuelRequestAndIssueController fuelRequestAndIssueController;
    @Inject
    private ReportController reportController;

    private static final String KM_LABEL_PLACEHOLDER = "@@KM_LABEL_FORMATTER@@";
    // Renders as: '' when km is not computable for a bar, otherwise the rounded km value.
    private static final String KM_LABEL_FORMATTER_JS
            = "function(p){var k=p&&p.data?p.data.km:null;"
            + "return (k===null||k===undefined)?'':(Math.round(k)+' km');}";

    private InstitutionDashboardSummary summary;
    private String vehicleUsageChartOption;
    private String vehicleUsageLastMonthChartOption;
    private String vehicleEfficiencyChartOption;
    private String vehicleDistanceChartOption;

    @PostConstruct
    public void init() {
        summary = institutionDashboardApplicationController.getSummary(webUserController.getLoggedInstitution());
        vehicleUsageChartOption = buildUsageChartOption(summary.getTop10VehiclesByUsage(), "Top 10 Vehicles by Fuel Usage - This Month");
        vehicleUsageLastMonthChartOption = buildUsageChartOption(summary.getTop10VehiclesByUsageLastMonth(), "Top 10 Vehicles by Fuel Usage - Last Month");
        vehicleEfficiencyChartOption = buildEfficiencyChartOption();
        vehicleDistanceChartOption = buildDistanceChartOption();
    }

    private String buildUsageChartOption(List<InstitutionCount> rows, String titleText) {
        JsonArrayBuilder categories = Json.createArrayBuilder();
        JsonArrayBuilder data = Json.createArrayBuilder();

        if (rows != null) {
            for (InstitutionCount ic : rows) {
                categories.add(ic.getVehicle().getVehicleNumber() + " (" + ic.getVehicle().getVehicleType().getLabel() + ")");
                JsonObjectBuilder point = Json.createObjectBuilder()
                        .add("value", ic.getIssuedQty())
                        .add("itemStyle", Json.createObjectBuilder().add("color", ic.getVehicle().getVehicleType().getColor()));
                if (ic.getKmDriven() == null) {
                    point.addNull("km");
                } else {
                    point.add("km", ic.getKmDriven());
                }
                data.add(point);
            }
        }

        JsonObjectBuilder series = Json.createObjectBuilder()
                .add("name", "Issued Quantity (L)")
                .add("type", "bar")
                .add("data", data)
                .add("label", Json.createObjectBuilder()
                        .add("show", true)
                        .add("position", "top")
                        .add("fontSize", 10)
                        .add("formatter", KM_LABEL_PLACEHOLDER));

        JsonObject option = baseBarOptionBuilder(titleText, categories, series).build();

        return option.toString().replace("\"" + KM_LABEL_PLACEHOLDER + "\"", KM_LABEL_FORMATTER_JS);
    }

    private String buildEfficiencyChartOption() {
        JsonArrayBuilder categories = Json.createArrayBuilder();
        JsonArrayBuilder data = Json.createArrayBuilder();

        List<VehicleFuelEfficiency> rows = summary.getTop10VehiclesByLitersPerKm();
        if (rows != null) {
            for (VehicleFuelEfficiency vfe : rows) {
                categories.add(vfe.getVehicle().getVehicleNumber() + " (" + vfe.getVehicle().getVehicleType().getLabel() + ")");
                data.add(Json.createObjectBuilder()
                        .add("value", vfe.getKmPerLiter())
                        .add("itemStyle", Json.createObjectBuilder().add("color", vfe.getVehicle().getVehicleType().getColor())));
            }
        }

        JsonObjectBuilder series = Json.createObjectBuilder()
                .add("name", "KM per Liter")
                .add("type", "bar")
                .add("data", data);

        JsonObject option = baseBarOptionBuilder("Top 10 Vehicles by Fuel Efficiency (KM per Liter) - Last Month", categories, series).build();
        return option.toString();
    }

    private String buildDistanceChartOption() {
        JsonArrayBuilder categories = Json.createArrayBuilder();
        JsonArrayBuilder data = Json.createArrayBuilder();

        List<InstitutionCount> rows = summary.getTop10VehiclesByDistanceLastMonth();
        if (rows != null) {
            for (InstitutionCount ic : rows) {
                categories.add(ic.getVehicle().getVehicleNumber() + " (" + ic.getVehicle().getVehicleType().getLabel() + ")");
                data.add(Json.createObjectBuilder()
                        .add("value", ic.getKmDriven())
                        .add("itemStyle", Json.createObjectBuilder().add("color", ic.getVehicle().getVehicleType().getColor())));
            }
        }

        JsonObjectBuilder series = Json.createObjectBuilder()
                .add("name", "Distance Traveled (KM)")
                .add("type", "bar")
                .add("data", data);

        JsonObject option = baseBarOptionBuilder("Top 10 Vehicles by Distance Traveled (KM) - Last Month", categories, series).build();
        return option.toString();
    }

    private JsonObjectBuilder baseBarOptionBuilder(String titleText, JsonArrayBuilder categories, JsonObjectBuilder series) {
        return Json.createObjectBuilder()
                .add("title", Json.createObjectBuilder().add("text", titleText).add("left", "center"))
                .add("tooltip", Json.createObjectBuilder().add("trigger", "axis"))
                .add("grid", Json.createObjectBuilder().add("containLabel", true))
                .add("xAxis", Json.createObjectBuilder()
                        .add("type", "category")
                        .add("data", categories)
                        .add("axisLabel", Json.createObjectBuilder().add("interval", 0).add("rotate", 30)))
                .add("yAxis", Json.createObjectBuilder().add("type", "value"))
                .add("series", Json.createArrayBuilder().add(series));
    }

    private Double trendPercent(Double current, Double previous) {
        if (current == null || previous == null || previous == 0) {
            return null;
        }
        return ((current - previous) / previous) * 100;
    }

    private String trendLabel(Double percent) {
        if (percent == null) {
            return null;
        }
        return String.format(" (%s%.1f%%)", percent > 0 ? "+" : "", percent);
    }

    public InstitutionDashboardSummary getSummary() {
        return summary;
    }

    public String getVehicleUsageChartOption() {
        return vehicleUsageChartOption;
    }

    public String getVehicleUsageLastMonthChartOption() {
        return vehicleUsageLastMonthChartOption;
    }

    public String getVehicleEfficiencyChartOption() {
        return vehicleEfficiencyChartOption;
    }

    public String getVehicleDistanceChartOption() {
        return vehicleDistanceChartOption;
    }

    public Double getRequestedTrendPercent() {
        return trendPercent(summary.getRequestedThisMonth(), summary.getRequestedLastMonth());
    }

    public Double getIssuedTrendPercent() {
        return trendPercent(summary.getIssuedThisMonth(), summary.getIssuedLastMonth());
    }

    public String getRequestedTrendLabel() {
        return trendLabel(getRequestedTrendPercent());
    }

    public String getIssuedTrendLabel() {
        return trendLabel(getIssuedTrendPercent());
    }

    public List<Bill> getRejectedCpcBills() {
        return summary.getRejectedCpcBills();
    }

    /**
     * "Requested, Not Yet Issued" card - the pending-issue count/quantity
     * on the dashboard has no date bound, so widen the search page's date
     * range to catch everything outstanding rather than just this month.
     */
    public String navigateToPendingIssueRequests() {
        fuelRequestAndIssueController.setFromDate(new Date(0));
        fuelRequestAndIssueController.setToDate(new Date());
        return fuelRequestAndIssueController.navigateToListInstitutionRequestsToMark();
    }

    /**
     * "Not Submitted for Payment" card is based on last month's issued
     * transactions, so scope the payment-request page to the same month.
     */
    public String navigateToNotSubmittedForPayment() {
        fuelRequestAndIssueController.setFromDate(CommonController.startOfTheLastMonth());
        fuelRequestAndIssueController.setToDate(CommonController.endOfTheLastMonth());
        return fuelRequestAndIssueController.navigateToMakePayment();
    }

    /**
     * "Resubmit Requested by CPC" card is based on last month's bills,
     * so pre-populate the payments list with the same month's bills.
     */
    public String navigateToResubmitRequestedBills() {
        fuelRequestAndIssueController.setFromDate(CommonController.startOfTheLastMonth());
        fuelRequestAndIssueController.setToDate(CommonController.endOfTheLastMonth());
        fuelRequestAndIssueController.listPaymentBillsForInstitutionLevel();
        return reportController.navigateToListPayments();
    }
}
