/*
 * The MIT License
 *
 * Copyright 2019 buddhika.ari@gmail.com
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

// <editor-fold defaultstate="collapsed" desc="Imports">
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.FuelTransactionType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.enums.VehiclePurpose;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.pojcs.AreaCount;
import lk.gov.health.phsp.pojcs.FuelIssuedSummary;
import lk.gov.health.phsp.pojcs.FuelTransactionLight;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.primefaces.model.StreamedContent;
// </editor-fold>   

/**
 *
 * @author hiu_pdhs_sp
 */
@Named
@SessionScoped
public class ReportController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private FuelTransactionHistoryFacade encounterFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    FuelTransactionFacade fuelTransactionFacade;
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;

    @Inject
    private ExcelReportController excelReportController;

    @Inject
    private UserTransactionController userTransactionController;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<FuelTransaction> transactions;
    private FuelTransaction fuelTransaction;
    private FuelTransactionLight fuelTransactionLight;
    private List<FuelTransactionLight> transactionLights;
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Institution fromInstitution;
    private Institution toInstitution;
    private Area area;
    private StreamedContent file;
    private String mergingMessage;
    private List<InstitutionCount> institutionCounts;
    private Long reportCount;
    private List<AreaCount> areaCounts;
    private Long areaRepCount;

    private Upload currentUpload;
    private StreamedContent downloadingFile;
    private StreamedContent resultExcelFile;

    private ReportTimePeriod reportTimePeriod;
    private TimePeriodType timePeriodType;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer dateOfMonth;
    private Quarter quarterEnum;
    private boolean recalculate;

    private VehicleType vehicleType;
    private VehiclePurpose vehiclePurpose;
    private Driver driver;
    private InstitutionType institutionType;
    private List<FuelIssuedSummary> issuedSummaries;
    Long fuelStationId;
    Long healthInstitutionId;
    private Date selectedDate; // This represents the date clicked in the comprehensive report

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new instance of ReportController
     */
    public ReportController() {
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Navigational Methods">
    public String navigateToListFuelRequests() {
        fillAllInstitutionFuelTransactions();
        return "/reports/list?faces-redirect=true;";
    }

    public String navigateToDieselDistributionFuelStationSummary() {
        fillDieselDistributionFuelStationSummary();
        return "/reports/diesel_distribution_fuel_station_summary?faces-redirect=true;";
    }

    public String navigateToDieselDistributionHealthInstitutionSummary() {
        fillDieselDistributionHealthInstitutionSummary();
        return "/reports/diesel_distribution_health_institution_summary?faces-redirect=true;";
    }

    public String navigateToComprehensiveDieselIssuanceSummary() {
        fillComprehensiveDieselIssuanceSummary();
        return "/reports/comprehensive_diesel_issuance_summary?faces-redirect=true;";
    }

    public String navigateToReportsIndex() {
        return "/reports/index?faces-redirect=true;";
    }

    public String navigateToViewRequest() {
        if (fuelTransactionLight == null) {
            JsfUtil.addErrorMessage("Error");
            return "";
        }
        if (fuelTransactionLight.getId() == null) {
            JsfUtil.addErrorMessage("Error");
            return "";
        }
        fuelTransaction = fuelTransactionFacade.find(fuelTransactionLight.getId());
        if (fuelTransaction == null) {
            JsfUtil.addErrorMessage("Error");
            return "";
        }
        return "/reports/request?faces-redirect=true;";
    }

    public String navigateToComprehensiveSummaryFromFuelStationSummary() {
        toInstitution = institutionController.getInstitutionById(fuelStationId);
        if (toInstitution == null) {
            JsfUtil.addErrorMessage("Error");
            return null;
        }
        fromInstitution = null;
        return navigateToComprehensiveDieselIssuanceSummary();
    }

    public String navigateToComprehensiveSummaryFromHealthInstitutionSummary() {
        fromInstitution = institutionController.getInstitutionById(healthInstitutionId);
        if (fromInstitution == null) {
            JsfUtil.addErrorMessage("Error");
            return null;
        }
        toInstitution = null;
        return navigateToComprehensiveDieselIssuanceSummary();
    }

    public String navigateToIndividualTransactionsFromHealthInstitution() {
        // Set filter for health institution
        fromInstitution = institutionController.getInstitutionById(healthInstitutionId);
        // Reset other filters if necessary
        toInstitution = null;
        // Call the method to fill the individual transactions based on the set filters
        fillAllInstitutionFuelTransactions();
        // Navigate to the page that lists individual transactions
        return navigateToDieselDistributionFuelStationSummary();
    }

    public String navigateToIndividualTransactionsFromFuelStation() {
        // Set filter for fuel station
        toInstitution = institutionController.getInstitutionById(fuelStationId);
        // Reset other filters if necessary
        fromInstitution = null;
        // Call the method to fill the individual transactions based on the set filters
        fillAllInstitutionFuelTransactions();
        // Navigate to the page that lists individual transactions
        return navigateToDieselDistributionFuelStationSummary();
    }

    public String navigateToIndividualTransactionsFromDate() {
        // Set fromDate and toDate to the selectedDate
        fromDate = selectedDate;
        toDate = selectedDate;
        // Call the method to fill the individual transactions based on the set filters
        fillAllInstitutionFuelTransactions();
        // Navigate to the page that lists individual transactions
        return navigateToDieselDistributionFuelStationSummary();
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Functional Methods">
    public void fillAllInstitutionFuelTransactions() {
        transactionLights = fillFuelTransactions(fromInstitution, toInstitution, fromDate, toDate, vehicleType, vehiclePurpose, driver, institutionType);
    }

    public List<FuelTransactionLight> fillFuelTransactions(
            Institution requestingInstitution, Institution fuelStation, Date fromDate, Date toDate,
            VehicleType vehicleType, VehiclePurpose vehiclePurpose, Driver driver, InstitutionType institutionType) {

        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelTransactionLight(")
                .append("ft.id, ft.requestAt, ft.requestReferenceNumber, ")
                .append("v.vehicleNumber, ft.requestQuantity, ft.issuedQuantity, ")
                .append("ft.issueReferenceNumber, ")
                .append("fi.name, ") // fromInstitution name
                .append("ti.name, ") // toInstitution name
                .append("COALESCE(d.name, 'No Driver')") // driver name or 'No Driver' if null
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.vehicle v ")
                .append("LEFT JOIN ft.driver d ")
                .append("LEFT JOIN ft.fromInstitution fi ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);

        if (requestingInstitution != null) {
            jpqlBuilder.append("AND ft.requestedInstitution = :reqInstitute ");
            parameters.put("reqInstitute", requestingInstitution);
        }
        if (fuelStation != null) {
            jpqlBuilder.append("AND ft.issuedInstitution = :fuelStation ");
            parameters.put("fuelStation", fuelStation);
        }
        if (fromDate != null && toDate != null) {
            jpqlBuilder.append("AND ft.requestAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
        }
        if (vehicleType != null) {
            jpqlBuilder.append("AND v.vehicleType = :vType ");
            parameters.put("vType", vehicleType);
        }
        if (vehiclePurpose != null) {
            jpqlBuilder.append("AND v.vehiclePurpose = :vPurpose ");
            parameters.put("vPurpose", vehiclePurpose);
        }
        if (driver != null) {
            jpqlBuilder.append("AND ft.driver = :drv ");
            parameters.put("drv", driver);
        }
        if (institutionType != null) {
            jpqlBuilder.append("AND ft.requestedInstitution.institutionType = :instType ");
            parameters.put("instType", institutionType);
        }

        jpqlBuilder.append("ORDER BY ft.requestAt");

        List<FuelTransactionLight> resultList = (List<FuelTransactionLight>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
        return resultList;
    }

    public void fillDieselDistributionFuelStationSummary() {
        issuedSummaries = fillFuelIssuedFromFuelStationSummary(fromDate, toDate);
    }

    public void fillDieselDistributionHealthInstitutionSummary() {
        issuedSummaries = fillFuelIssuedToHealthInstitutionSummary(fromDate, toDate);
    }

    public void fillComprehensiveDieselIssuanceSummary() {
        issuedSummaries = fillFuelIssuedSummary(fromInstitution, toInstitution, fromDate, toDate);
    }

    public List<FuelIssuedSummary> fillFuelIssuedToHealthInstitutionSummary(Date fromDate, Date toDate) {
        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelIssuedSummary(")
                .append("FUNCTION('date', ft.issuedAt), ") // Group by issued date
                .append("fi.name, ") // fromInstitution name
                .append("fi.id, ") // fromInstitution ID
                .append("SUM(ft.issuedQuantity)") // sum of issued qty
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.fromInstitution fi ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);

        if (fromDate != null && toDate != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), fi.name, fi.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), fi.name");

        return (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
    }

    public List<FuelIssuedSummary> fillFuelIssuedFromFuelStationSummary(Date fromDate, Date toDate) {
        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelIssuedSummary(")
                .append("FUNCTION('date', ft.issuedAt), ") // Group by issued date
                .append("ti.name, ") // toInstitution name
                .append("ti.id, ") // toInstitution ID
                .append("SUM(ft.issuedQuantity)") // sum of issued qty
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);

        if (fromDate != null && toDate != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), ti.name, ti.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), ti.name");

        return (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
    }

    public List<FuelIssuedSummary> fillFuelIssuedSummary(Institution fromInstitution, Institution toInstitution, Date fromDate, Date toDate) {
        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelIssuedSummary(")
                .append("FUNCTION('date', ft.issuedAt), ") // Group by issued date
                .append("fi.name, ") // fromInstitution name
                .append("fi.id, ") // fromInstitution ID
                .append("ti.name, ") // toInstitution name
                .append("ti.id, ") // toInstitution ID
                .append("SUM(ft.issuedQuantity)") // sum of issued qty
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.fromInstitution fi ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);

        if (fromInstitution != null) {
            jpqlBuilder.append("AND ft.fromInstitution = :fromInstitute ");
            parameters.put("fromInstitute", fromInstitution);
        }
        if (toInstitution != null) {
            jpqlBuilder.append("AND ft.toInstitution = :toInstitute ");
            parameters.put("toInstitute", toInstitution);
        }
        if (fromDate != null && toDate != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), fi.name, fi.id, ti.name, ti.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), fi.name, ti.name");

        List<FuelIssuedSummary> resultList = (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
        return resultList;
    }

    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public List<FuelIssuedSummary> getIssuedSummaries() {
        return issuedSummaries;
    }

    public void setIssuedSummaries(List<FuelIssuedSummary> issuedSummaries) {
        this.issuedSummaries = issuedSummaries;
    }

    public Long getFuelStationId() {
        return fuelStationId;
    }

    public void setFuelStationId(Long fuelStationId) {
        this.fuelStationId = fuelStationId;
    }

    public Long getHealthInstitutionId() {
        return healthInstitutionId;
    }

    public void setHealthInstitutionId(Long healthInstitutionId) {
        this.healthInstitutionId = healthInstitutionId;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public FuelTransaction getFuelTransaction() {
        return fuelTransaction;
    }

    public void setFuelTransaction(FuelTransaction fuelTransaction) {
        this.fuelTransaction = fuelTransaction;
    }

    public FuelTransactionLight getFuelTransactionLight() {
        return fuelTransactionLight;
    }

    public void setFuelTransactionLight(FuelTransactionLight fuelTransactionLight) {
        this.fuelTransactionLight = fuelTransactionLight;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public VehiclePurpose getVehiclePurpose() {
        return vehiclePurpose;
    }

    public void setVehiclePurpose(VehiclePurpose vehiclePurpose) {
        this.vehiclePurpose = vehiclePurpose;
    }

    public List<FuelTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FuelTransaction> transactions) {
        this.transactions = transactions;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheYear();
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public TimePeriodType getTimePeriodType() {
        if (timePeriodType == null) {
            timePeriodType = TimePeriodType.Monthly;
        }
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public Integer getYear() {
        if (year == null || year == 0) {
            year = CommonController.getYear(CommonController.startOfTheLastQuarter());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        if (quarter == null) {
            quarter = CommonController.getQuarter(CommonController.startOfTheLastQuarter());
        }
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        if (month == null) {
            month = CommonController.getMonth(CommonController.startOfTheLastMonth());
        }
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDateOfMonth() {
        return dateOfMonth;
    }

    public void setDateOfMonth(Integer dateOfMonth) {
        this.dateOfMonth = dateOfMonth;
    }

    public Quarter getQuarterEnum() {
        if (quarterEnum == null) {
            switch (getQuarter()) {
                case 1:
                    quarterEnum = Quarter.First;
                    break;
                case 2:
                    quarterEnum = Quarter.Second;
                    break;
                case 3:
                    quarterEnum = Quarter.Third;
                    break;
                case 4:
                    quarterEnum = Quarter.Fourth;
                    break;
                default:
                    quarterEnum = Quarter.First;
            }
        }
        return quarterEnum;
    }

    public void setQuarterEnum(Quarter quarterEnum) {
        switch (quarterEnum) {
            case First:
                quarter = 1;
                break;
            case Second:
                quarter = 2;
                break;
            case Third:
                quarter = 3;
                break;
            case Fourth:
                quarter = 4;
                break;
            default:
                quarter = 1;
        }
        this.quarterEnum = quarterEnum;
    }

    public ReportTimePeriod getReportTimePeriod() {
        return reportTimePeriod;
    }

    public void setReportTimePeriod(ReportTimePeriod reportTimePeriod) {
        this.reportTimePeriod = reportTimePeriod;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public StreamedContent getFile() {
        return file;
    }

    public String getMergingMessage() {
        return mergingMessage;
    }

    public void setMergingMessage(String mergingMessage) {
        this.mergingMessage = mergingMessage;
    }

    public FuelTransactionHistoryFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(FuelTransactionHistoryFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public Upload getCurrentUpload() {
        return currentUpload;
    }

    public void setCurrentUpload(Upload currentUpload) {
        this.currentUpload = currentUpload;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public void setInstitutionCounts(List<InstitutionCount> institutionCounts) {
        this.institutionCounts = institutionCounts;
    }

    public Long getReportCount() {
        return reportCount;
    }

    public void setReportCount(Long reportCount) {
        this.reportCount = reportCount;
    }

    public StreamedContent getResultExcelFile() {
        return resultExcelFile;
    }

    public void setResultExcelFile(StreamedContent resultExcelFile) {
        this.resultExcelFile = resultExcelFile;
    }

    public ExcelReportController getExcelReportController() {
        return excelReportController;
    }

    public void setExcelReportController(ExcelReportController excelReportController) {
        this.excelReportController = excelReportController;
    }

    public List<AreaCount> getAreaCounts() {
        return areaCounts;
    }

    public void setAreaCounts(List<AreaCount> areaCounts) {
        this.areaCounts = areaCounts;
    }

    public Long getAreaRepCount() {
        return areaRepCount;
    }

    public void setAreaRepCount(Long areaRepCount) {
        this.areaRepCount = areaRepCount;
    }

    public boolean isRecalculate() {
        return recalculate;
    }

    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public List<FuelTransactionLight> getTransactionLights() {
        return transactionLights;
    }

    // </editor-fold> 
}
