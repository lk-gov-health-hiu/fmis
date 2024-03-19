/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.InstitutionCategory;
import lk.gov.health.phsp.enums.InstitutionType;

import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import org.json.JSONObject;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class DashboardController implements Serializable {

    @EJB
    private FuelTransactionHistoryFacade encounterFacade;

    @Inject
    PreferenceController preferenceController;
    @Inject
    private ItemController itemController;
    @Inject
    private DashboardApplicationController dashboardApplicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemApplicationController itemApplicationController;
    @Inject
    FuelRequestAndIssueController letterController;

    private Date fromDate;
    private Date toDate;
    private List<InstitutionCount> fuelOrdersByInstitution;
    private List<InstitutionCount> fuelOrdersByFuelStation;
    private BarChartModel stackedBarModelForHospitalFuelDetails;
    private BarChartModel stackedBarModelForFuelShedDetails;


    Double totalOrderedButNotIssued;
    Double totalIssued;
    Double totalRemainingToBeIssued;

    DecimalFormat df = new DecimalFormat("0.00");

    private List<InstitutionCount> orderingCategories;

    @PostConstruct
    public void init() {
        createStackedBarModelForHospitalDetails();
        createStackedBarModelForFuelStationDetails();
        createPiChartForRemaining();
    }

    public void createStackedBarModelForHospitalDetails() {
        fuelOrdersByInstitution = dashboardApplicationController.fuelOrdersByInstitution(getFromDate(), getToDate());
        stackedBarModelForHospitalFuelDetails = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet requestedSet = new BarChartDataSet();
        requestedSet.setLabel("Issued Quantity");
        List<Number> requestedValues = new ArrayList<>();
        List<String> bgColorRequested = new ArrayList<>();
        List<String> borderColorRequested = new ArrayList<>();

        BarChartDataSet remainingSet = new BarChartDataSet();
        remainingSet.setLabel("Requested, but not issued Quantity");
        List<Number> remainingValues = new ArrayList<>();
        List<String> bgColorRemaining = new ArrayList<>();
        List<String> borderColorRemaining = new ArrayList<>();

        List<String> labels = new ArrayList<>();

        for (InstitutionCount ic : fuelOrdersByInstitution) {
            labels.add(ic.getInstitution().getName()); // Assumes Institution has a getName() method
            requestedValues.add(ic.getRequestedQty());
            remainingValues.add(ic.getRemainingQty());

            // For "Requested Quantity", use a solid green color
            bgColorRequested.clear(); // Clear existing colors if any
            borderColorRequested.clear(); // Clear existing border colors if any

            bgColorRequested.add("rgba(0, 128, 0, 1.0)"); // Solid green with full opacity
            borderColorRequested.add("rgb(0, 128, 0)");  // Solid green for the border

// For "Remaining Quantity", use a solid red color
            bgColorRemaining.clear(); // Clear existing colors if any
            borderColorRemaining.clear(); // Clear existing border colors if any

            bgColorRemaining.add("rgba(255, 0, 0, 1.0)"); // Solid red with full opacity
            borderColorRemaining.add("rgb(255, 0, 0)");  // Solid red for the border

        }

        requestedSet.setData(requestedValues);
        requestedSet.setBackgroundColor(bgColorRequested);
        requestedSet.setBorderColor(borderColorRequested);

        remainingSet.setData(remainingValues);
        remainingSet.setBackgroundColor(bgColorRemaining);
        remainingSet.setBorderColor(borderColorRemaining);

        data.addChartDataSet(requestedSet);
        data.addChartDataSet(remainingSet);
        data.setLabels(labels);

        stackedBarModelForHospitalFuelDetails.setData(data);

        // Options Configuration
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true); // Important for stacking
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Fuel Orders by Institution - Top 10");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);

        stackedBarModelForHospitalFuelDetails.setOptions(options);
    }

    private PieChartModel pieChartModelForRemaining;

// Getter
    public PieChartModel getPieChartModelForRemaining() {
        return pieChartModelForRemaining;
    }
    Number totalGrantQuantity;
    Number totalIssues;

    public void createPiChartForRemaining() {
        totalGrantQuantity = preferenceController.getTotalFuelInLitersLong();
        totalIssues = dashboardApplicationController.totalIssuedQuantity();

        Number remainingQuantity = totalGrantQuantity.doubleValue() - totalIssues.doubleValue();

        System.out.println("totalGrantQuantity = " + totalGrantQuantity);
        System.out.println("totalIssues = " + totalIssues);
        System.out.println("remainingQuantity = " + remainingQuantity);
        
        PieChartModel pieModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> values = new ArrayList<>();
        values.add(totalIssues); // Total issued quantity
        values.add(remainingQuantity); // Remaining quantity
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("rgba(255, 99, 132, 0.6)"); // Color for total issues, e.g., light red
        bgColors.add("rgba(54, 162, 235, 0.6)"); // Color for remaining quantity, e.g., light blue
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);

        List<String> labels = new ArrayList<>();
        labels.add("Total Issues");
        labels.add("Remaining");
        data.setLabels(labels);

        pieModel.setData(data);

        this.pieChartModelForRemaining = pieModel; // Assuming this.pieChartModelForRemaining is a class member
    }

    public void createStackedBarModelForFuelStationDetails() {
        fuelOrdersByFuelStation = dashboardApplicationController.fuelSupplyByFuelStations(getFromDate(), getToDate());
        stackedBarModelForFuelShedDetails = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet requestedSet = new BarChartDataSet();
        requestedSet.setLabel("Issued Quantity");
        List<Number> requestedValues = new ArrayList<>();
        List<String> bgColorRequested = new ArrayList<>();
        List<String> borderColorRequested = new ArrayList<>();

        BarChartDataSet remainingSet = new BarChartDataSet();
        remainingSet.setLabel("Requested, but not issued Quantity");
        List<Number> remainingValues = new ArrayList<>();
        List<String> bgColorRemaining = new ArrayList<>();
        List<String> borderColorRemaining = new ArrayList<>();

        List<String> labels = new ArrayList<>();

        for (InstitutionCount ic : fuelOrdersByFuelStation) {
            labels.add(ic.getInstitution().getName()); // Assumes Institution has a getName() method
            requestedValues.add(ic.getRequestedQty());
            remainingValues.add(ic.getRemainingQty());

            // For "Requested Quantity", use a solid green color
            bgColorRequested.clear(); // Clear existing colors if any
            borderColorRequested.clear(); // Clear existing border colors if any

            bgColorRequested.add("rgba(0, 128, 0, 1.0)"); // Solid green with full opacity
            borderColorRequested.add("rgb(0, 128, 0)");  // Solid green for the border

// For "Remaining Quantity", use a solid red color
            bgColorRemaining.clear(); // Clear existing colors if any
            borderColorRemaining.clear(); // Clear existing border colors if any

            bgColorRemaining.add("rgba(255, 0, 0, 1.0)"); // Solid red with full opacity
            borderColorRemaining.add("rgb(255, 0, 0)");  // Solid red for the border

        }

        requestedSet.setData(requestedValues);
        requestedSet.setBackgroundColor(bgColorRequested);
        requestedSet.setBorderColor(borderColorRequested);

        remainingSet.setData(remainingValues);
        remainingSet.setBackgroundColor(bgColorRemaining);
        remainingSet.setBorderColor(borderColorRemaining);

        data.addChartDataSet(requestedSet);
        data.addChartDataSet(remainingSet);
        data.setLabels(labels);

        stackedBarModelForFuelShedDetails.setData(data);

        // Options Configuration
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true); // Important for stacking
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Fuel Issues by Fuel Stations - Top 10");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);

        stackedBarModelForFuelShedDetails.setOptions(options);
    }

    public void prepareHospitalDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

    }

    public void prepareNationalDashboard() {
        System.out.println("prepareNationalDashboard = ");
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        createStackedBarModelForHospitalDetails();
        createStackedBarModelForFuelStationDetails();
        createPiChartForRemaining();

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

    }

    public void prepareRegionalDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        if (webUserController.getLoggedInstitution().getRdhsArea() == null) {
            JsfUtil.addErrorMessage("RDHS is not properly set. Please inform the support team. Dashboard will not be prepared.");
            return;
        }

    }

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardController() {
    }

    public BarChartModel getStackedBarModelForHospitalFuelDetails() {
        return stackedBarModelForHospitalFuelDetails;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, 2024);
            c.set(Calendar.MONTH, 1);
            c.set(Calendar.DATE, 1);
            fromDate = c.getTime();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
    
    

    void prepareDashboard() {
        WebUser loggedUser = webUserController.getLoggedUser();
        if (loggedUser == null) {
            return;
        }
        Institution userInstitution = loggedUser.getInstitution();
        if (userInstitution == null) {
            JsfUtil.addErrorMessage("No Institution for Logged User");
            return;
        }
        InstitutionType institutionType = userInstitution.getInstitutionType();
        if (institutionType == null) {
            JsfUtil.addErrorMessage("No Institution type for the Logged Institution");
            return;
        }
        InstitutionCategory category = institutionType.getCategory();
        if (category == null) {
            JsfUtil.addErrorMessage("No Category for Logged Institution");
            return;
        }

        switch (category) {
            case FUEL_RECEIVER:
                prepareHospitalDashboard();
                break;
            default:
                prepareNationalDashboard();
        }
    }

    public List<InstitutionCount> getFuelOrdersByInstitution() {
        return fuelOrdersByInstitution;
    }

    public List<InstitutionCount> getFuelOrdersByFuelStation() {
        return fuelOrdersByFuelStation;
    }

    public BarChartModel getStackedBarModelForFuelShedDetails() {
        return stackedBarModelForFuelShedDetails;
    }

   

}
