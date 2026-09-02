package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.Bill;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionDashboardSummary;
import lk.gov.health.phsp.pojcs.VehicleFuelEfficiency;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

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

    private InstitutionDashboardSummary summary;
    private BarChartModel vehicleUsageChart;
    private BarChartModel vehicleUsageLastMonthChart;
    private BarChartModel vehicleEfficiencyChart;

    @PostConstruct
    public void init() {
        summary = institutionDashboardApplicationController.getSummary(webUserController.getLoggedInstitution());
        vehicleUsageChart = createVehicleUsageChart(summary.getTop10VehiclesByUsage(), "Top 10 Vehicles by Fuel Usage - This Month");
        vehicleUsageLastMonthChart = createVehicleUsageChart(summary.getTop10VehiclesByUsageLastMonth(), "Top 10 Vehicles by Fuel Usage - Last Month");
        vehicleEfficiencyChart = createVehicleEfficiencyChart();
    }

    private BarChartModel createVehicleUsageChart(List<InstitutionCount> rows, String titleText) {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Issued Quantity (L)");
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        if (rows != null) {
            for (InstitutionCount ic : rows) {
                labels.add(ic.getVehicle().getVehicleNumber() + " (" + ic.getVehicle().getVehicleType().getLabel() + ")");
                values.add(ic.getIssuedQty());
                bgColors.add(ic.getVehicle().getVehicleType().getColor());
            }
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);

        BarChartOptions options = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText(titleText);
        options.setTitle(title);
        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        model.setOptions(options);

        return model;
    }

    private BarChartModel createVehicleEfficiencyChart() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("KM per Liter");
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        List<VehicleFuelEfficiency> rows = summary.getTop10VehiclesByLitersPerKm();
        if (rows != null) {
            for (VehicleFuelEfficiency vfe : rows) {
                labels.add(vfe.getVehicle().getVehicleNumber() + " (" + vfe.getVehicle().getVehicleType().getLabel() + ")");
                values.add(vfe.getKmPerLiter());
                bgColors.add(vfe.getVehicle().getVehicleType().getColor());
            }
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(false);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Top 10 Vehicles by Fuel Efficiency (KM per Liter) - Last Month");
        options.setTitle(title);
        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        model.setOptions(options);

        return model;
    }

    public String getVehicleUsageChartKmLabelsJson() {
        return toKmLabelsJson(summary.getTop10VehiclesByUsage());
    }

    public String getVehicleUsageLastMonthChartKmLabelsJson() {
        return toKmLabelsJson(summary.getTop10VehiclesByUsageLastMonth());
    }

    private String toKmLabelsJson(List<InstitutionCount> rows) {
        if (rows == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Double km = rows.get(i).getKmDriven();
            sb.append(km == null ? "null" : km);
        }
        sb.append("]");
        return sb.toString();
    }

    private Double trendPercent(Double current, Double previous) {
        if (current == null || previous == null || previous == 0) {
            return null;
        }
        return ((current - previous) / previous) * 100;
    }

    public InstitutionDashboardSummary getSummary() {
        return summary;
    }

    public BarChartModel getVehicleUsageChart() {
        return vehicleUsageChart;
    }

    public BarChartModel getVehicleUsageLastMonthChart() {
        return vehicleUsageLastMonthChart;
    }

    public BarChartModel getVehicleEfficiencyChart() {
        return vehicleEfficiencyChart;
    }

    public Double getRequestedTrendPercent() {
        return trendPercent(summary.getRequestedThisMonth(), summary.getRequestedLastMonth());
    }

    public Double getIssuedTrendPercent() {
        return trendPercent(summary.getIssuedThisMonth(), summary.getIssuedLastMonth());
    }

    public List<Bill> getRejectedCpcBills() {
        return summary.getRejectedCpcBills();
    }
}
