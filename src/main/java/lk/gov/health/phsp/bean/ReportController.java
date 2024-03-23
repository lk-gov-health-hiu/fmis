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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.FuelTransactionType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.FuelEstimateRowType;
import lk.gov.health.phsp.enums.InstitutionCategory;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.enums.VehiclePurpose;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
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
    private FuelTransactionFacade fuelTransactionFacade;
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private InstitutionApplicationController institutionApplicationController;

    @Inject
    private ExcelReportController excelReportController;

    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    private VehicleController vehicleController;

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
    private List<Institution> hospitals;
    private double totalFuelEstimate;
    private List<FuelEstimateRow> fuelEstimateRows;

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
    private FuelEstimate fuelEstimate;
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

    public String navigateToListDeletedFuelRequests() {
        fillAllInstitutionDeletedFuelTransactions();
        return "/reports/list_deleted?faces-redirect=true;";
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

    public String navigateToListFuelRequestsForCpc() {
        return "/reports/cpc/list?faces-redirect=true;";
    }

    public String navigateToListFuelRequestsForCpcHeadOffice() {
        return "/reports/cpc_head_office/list?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessForCpcToPrint() {
        return "/reports/cpc/national_estimate_print?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessForCpcToDownload() {
        return "/reports/cpc/national_estimate_to_download?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessForCpcHeadOfficeToPrint() {
        return "/reports/cpc_head_office/national_estimate_print?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessForCpcHeadOfficeToDownload() {
        return "/reports/cpc_head_office/national_estimate_to_download?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessToPrint() {
        fillAllInstitutionFuelTransactions();
        return "/reports/national_estimate_print?faces-redirect=true;";
    }

    public String navigateToListHospitalEstimatessToDownload() {
        fillAllInstitutionFuelTransactions();
        return "/reports/national_estimate_to_download?faces-redirect=true;";
    }

    public String navigateToDieselDistributionFuelStationSummaryForFuelDispensor() {
        fillDieselDistributionFuelStationSummary();
        return "/reports/fuel_dispensor/diesel_distribution_fuel_station_summary?faces-redirect=true;";
    }

    public String navigateToDieselDistributionHealthInstitutionSummaryForFuelDispensor() {
        fillDieselDistributionHealthInstitutionSummary();
        return "/reports/fuel_dispensor/diesel_distribution_health_institution_summary?faces-redirect=true;";
    }

    public String navigateToComprehensiveDieselIssuanceSummaryForFuelDispensor() {
        fillComprehensiveDieselIssuanceSummary();
        return "/reports/fuel_dispensor/comprehensive_diesel_issuance_summary?faces-redirect=true;";
    }

    public String navigateToReportsIndex() {
        WebUser loggedUser = webUserController.getLoggedUser();
        if (loggedUser == null) {
            JsfUtil.addErrorMessage("No Logged User");
            return ""; // Redirect to login or a relevant page
        }

        Institution userInstitution = loggedUser.getInstitution();
        if (userInstitution == null) {
            JsfUtil.addErrorMessage("No Institution for Logged User");
            return ""; // Redirect to login or a relevant page
        }

        InstitutionType institutionType = userInstitution.getInstitutionType();
        if (institutionType == null) {
            JsfUtil.addErrorMessage("No Institution type for the Logged Institution");
            return ""; // Redirect to login or a relevant page
        }

        InstitutionCategory category = institutionType.getCategory();
        if (category == null) {
            JsfUtil.addErrorMessage("No Category for Logged Institution");
            return ""; // Redirect to login or a relevant page
        }

        switch (category) {
            case CPC:
                return "/reports/cpc/index";
            case CPC_HEAD_OFFICE:
                return "/reports/cpc_head_office/index";
            case FUEL_RECEIVER:
                return "/reports/index?faces-redirect=true;";
            case MONITORING_AND_EVALUATION:
            case OTHER:
                return "/institution/reports/index";
            default:
                return "/default"; // Redirect to a default page
        }
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
        fromInstitution = institutionController.getInstitutionById(healthInstitutionId);
        toInstitution = null;
        return navigateToListFuelRequests();
    }

    public String navigateToIndividualTransactionsFromFuelStation() {
        toInstitution = institutionController.getInstitutionById(fuelStationId);
        fromInstitution = null;
        return navigateToListFuelRequests();
    }

    public String navigateToIndividualTransactionsFromDate() {
        fromDate = selectedDate;
        toDate = selectedDate;
        fromInstitution = null;
        toInstitution = null;
        return navigateToListFuelRequests();
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Functional Methods">
    public void fillAllInstitutionFuelTransactions() {
        transactionLights = fillFuelTransactions(fromInstitution, toInstitution, getFromDate(), getToDate(), vehicleType, vehiclePurpose, driver, institutionType);
    }

    public void fillAllInstitutionDeletedFuelTransactions() {
        transactionLights = fillDeletedFuelTransactions(fromInstitution, toInstitution, getFromDate(), getToDate(), vehicleType, vehiclePurpose, driver, institutionType);
    }

    public void fillAllInstitutionFuelTransactionsForCpcHeadOffice() {
        transactionLights = fillFuelTransactions(fromInstitution,
                toInstitution,
                getFromDate(),
                getToDate(), null, null, null, null);
    }

    public void fillFuelTransactionsForCpc() {
        if (toInstitution == null) {
            transactionLights = fillFuelTransactions(null,
                    webUserController.findAutherizedInstitutions(),
                    getFromDate(),
                    getToDate(),
                    null,
                    null,
                    null,
                    null);
        } else {
            transactionLights = fillFuelTransactions(null,
                    toInstitution,
                    getFromDate(),
                    getToDate(),
                    null,
                    null,
                    null,
                    null);
        }

    }

    public List<FuelTransactionLight> fillFuelTransactions(
            Institution requestingInstitution, Institution fuelStation, Date fd, Date td,
            VehicleType vehicleType, VehiclePurpose vehiclePurpose, Driver driver, InstitutionType institutionType) {

        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelTransactionLight(")
                .append("ft.id, ft.requestAt, ft.requestReferenceNumber, ")
                .append("v.vehicleNumber, ft.requestQuantity, ft.issuedQuantity, ")
                .append("ft.issueReferenceNumber, ")
                .append("fi.name, ") // fromInstitution name
                .append("ti.name, ") // toInstitution name
                .append("COALESCE(d.name, 'No Driver'), ") // driver name or 'No Driver' if null
                .append("ti.code ") // toInstitution name
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.vehicle v ")
                .append("LEFT JOIN ft.driver d ")
                .append("LEFT JOIN ft.fromInstitution fi ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);

        if (requestingInstitution != null) {
            jpqlBuilder.append("AND ft.fromInstitution = :reqInstitute ");
            parameters.put("reqInstitute", requestingInstitution);
        }
        if (fuelStation != null) {
            jpqlBuilder.append("AND ft.toInstitution = :fuelStation ");
            parameters.put("fuelStation", fuelStation);
        }
        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.requestAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
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
            jpqlBuilder.append("AND ft.fromInstitution.institutionType = :instType ");
            parameters.put("instType", institutionType);
        }

        jpqlBuilder.append("ORDER BY ft.requestAt");

        List<FuelTransactionLight> resultList = (List<FuelTransactionLight>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
        System.out.println("jpqlBuilder.toString() = " + jpqlBuilder.toString());
        System.out.println("parameters = " + parameters);
        
        return resultList;
    }

    public List<FuelTransactionLight> fillDeletedFuelTransactions(
            Institution requestingInstitution, Institution fuelStation, Date fd, Date td,
            VehicleType vehicleType, VehiclePurpose vehiclePurpose, Driver driver, InstitutionType institutionType) {

        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelTransactionLight(")
                .append("ft.id, ft.requestAt, ft.requestReferenceNumber, ")
                .append("v.vehicleNumber, ft.requestQuantity, ft.issuedQuantity, ")
                .append("ft.issueReferenceNumber, ")
                .append("fi.name, ") // fromInstitution name
                .append("ti.name, ") // toInstitution name
                .append("COALESCE(d.name, 'No Driver'), ") // driver name or 'No Driver' if null
                .append("ti.code ") // toInstitution name
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.vehicle v ")
                .append("LEFT JOIN ft.driver d ")
                .append("LEFT JOIN ft.fromInstitution fi ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", true);

        if (requestingInstitution != null) {
            jpqlBuilder.append("AND ft.requestedInstitution = :reqInstitute ");
            parameters.put("reqInstitute", requestingInstitution);
        }
        if (fuelStation != null) {
            jpqlBuilder.append("AND ft.issuedInstitution = :fuelStation ");
            parameters.put("fuelStation", fuelStation);
        }
        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.requestAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
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
            jpqlBuilder.append("AND ft.fromInstitution.institutionType = :instType ");
            parameters.put("instType", institutionType);
        }

        jpqlBuilder.append("ORDER BY ft.requestAt");

        List<FuelTransactionLight> resultList = (List<FuelTransactionLight>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
        return resultList;
    }

    public List<FuelTransactionLight> fillFuelTransactions(
            List<Institution> requestingInstitutions, List<Institution> fuelStations,
            Date fd, Date td, VehicleType vehicleType, VehiclePurpose vehiclePurpose,
            Driver driver, InstitutionType institutionType) {

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

        if (requestingInstitutions != null && !requestingInstitutions.isEmpty()) {
            jpqlBuilder.append("AND ft.fromInstitution IN :reqInstitutes ");
            parameters.put("reqInstitutes", requestingInstitutions);
        }

        if (fuelStations != null && !fuelStations.isEmpty()) {
            jpqlBuilder.append("AND ft.toInstitution IN :fuelStations ");
            parameters.put("fuelStations", fuelStations);
        }

        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.requestAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
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
            jpqlBuilder.append("AND ft.fromInstitution.institutionType = :instType ");
            parameters.put("instType", institutionType);
        }

        jpqlBuilder.append("ORDER BY ft.requestAt");

        List<FuelTransactionLight> resultList = (List<FuelTransactionLight>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
        
        System.out.println("jpqlBuilder.toString() = " + jpqlBuilder.toString());
        System.out.println("parameters = " + parameters);
        return resultList;
    }

    public void fillDieselDistributionFuelStationSummary() {
        issuedSummaries = fillFuelIssuedFromFuelStationSummary(getFromDate(), getToDate());
    }

    public void fillDieselDistributionFuelStationSummaryForInstitution() {
//        issuedSummaries = fillFuelIssuedFromFuelStationSummaryForInstitution(getFromDate(), getToDate());
    }

    public void fillDieselDistributionHealthInstitutionSummary() {
        issuedSummaries = fillFuelIssuedToHealthInstitutionSummary(getFromDate(), getToDate());
    }

    public void fillComprehensiveDieselIssuanceSummary() {
        issuedSummaries = fillFuelIssuedSummary(fromInstitution, toInstitution, getFromDate(), getToDate());
    }

    public void fillAllHospitalsEstimatesToDownload() {
        List<FuelEstimateRow> estimateRows = new ArrayList<>();

        List<InstitutionType> fuelReceivingInstitutionTypes = Arrays.stream(InstitutionType.values())
                .filter(it -> it.getCategory() == InstitutionCategory.CPC)
                .collect(Collectors.toList());
        List<Institution> fuelStations = institutionController.fillInstitutions(fuelReceivingInstitutionTypes);

        for (Institution fuelStation : fuelStations) {
            List<Institution> suppliedInstitutions = institutionController.findInstitutionsByMainFuelStation(fuelStation);

            if (!suppliedInstitutions.isEmpty()) {
                // Add fuel station row only if there are supplied institutions
                FuelEstimateRow fuelStationRow = new FuelEstimateRow();
                fuelStationRow.setRow(FuelEstimateRowType.FUEL_STATION_HEADING_ROW);
                fuelStationRow.setFuelStation(fuelStation);
                estimateRows.add(fuelStationRow);

                double fuelShedTotalEstimate = 0.0;

                for (Institution institution : suppliedInstitutions) {
                    // Add institution row
                    FuelEstimateRow institutionRow = new FuelEstimateRow();
                    institutionRow.setRow(FuelEstimateRowType.INSTITUTION_HEADING_ROW);
                    institutionRow.setInstitution(institution);
                    estimateRows.add(institutionRow);

                    List<Vehicle> vehicles = vehicleController.fillVehicles(institution);
                    double institutionFuelEstimate = 0.0;

                    if (vehicles != null) {
                        for (Vehicle vehicle : vehicles) {
                            // Add vehicle row
                            FuelEstimateRow vehicleRow = new FuelEstimateRow();
                            vehicleRow.setRow(FuelEstimateRowType.VEHICLE_ROW);
                            vehicleRow.setVehicle(vehicle);
                            Double vehicleFuelConsumption = vehicle.getEstiamtedMonthlyFuelConsumption();
                            vehicleFuelConsumption = vehicleFuelConsumption != null ? vehicleFuelConsumption : 0.0;
                            vehicleRow.setTotalEstimate(vehicleFuelConsumption);
                            estimateRows.add(vehicleRow);

                            institutionFuelEstimate += vehicleFuelConsumption;
                        }
                    }

                    fuelShedTotalEstimate += institutionFuelEstimate;
                    institutionRow.setInstitutionEstimate(institutionFuelEstimate);
                }

                fuelStationRow.setFuelStationEstimate(fuelShedTotalEstimate);
            }
        }

        // Add total row
        FuelEstimateRow totalRow = new FuelEstimateRow();
        totalRow.setRow(FuelEstimateRowType.TOTAL_ROW);
        totalRow.setTotalEstimate(estimateRows.stream()
                .map(FuelEstimateRow::getTotalEstimate)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        estimateRows.add(totalRow);

        this.fuelEstimateRows = estimateRows;
    }

    public void fillAllHospitalsEstimatesToPrint() {
        fuelEstimate = new FuelEstimate();
        List<FuelShedEstimate> fuelShedEstimates = new ArrayList<>();

        List<InstitutionType> fuelReceivingInstitutionTypes = Arrays.stream(InstitutionType.values())
                .filter(it -> it.getCategory() == InstitutionCategory.CPC)
                .collect(Collectors.toList());
        List<Institution> fuelStations = institutionController.fillInstitutions(fuelReceivingInstitutionTypes);

        double totalEstimate = 0.0;

        for (Institution fuelStation : fuelStations) {
            List<Institution> suppliedInstitutions = institutionController.findInstitutionsByMainFuelStation(fuelStation);

            if (!suppliedInstitutions.isEmpty()) { // Only proceed if there are supplied institutions
                FuelShedEstimate fuelShedEstimate = new FuelShedEstimate();
                fuelShedEstimate.setFuelStation(fuelStation);
                List<InstitutionEstimate> institutionEstimates = new ArrayList<>();

                double fuelShedTotalEstimate = 0.0;

                for (Institution institution : suppliedInstitutions) {
                    InstitutionEstimate institutionEstimate = new InstitutionEstimate();
                    institutionEstimate.setInstitution(institution);

                    List<Vehicle> vehicles = vehicleController.fillVehicles(institution);
                    double institutionFuelEstimate = vehicles != null ? vehicles.stream()
                            .filter(Objects::nonNull)
                            .mapToDouble(v -> v.getEstiamtedMonthlyFuelConsumption() != null ? v.getEstiamtedMonthlyFuelConsumption() : 0.0)
                            .sum() : 0.0;

                    institutionEstimate.setVehicles(vehicles);
                    institutionEstimate.setInstitutionFuelEstimate(institutionFuelEstimate);

                    fuelShedTotalEstimate += institutionFuelEstimate;
                    institutionEstimates.add(institutionEstimate);
                }

                if (!institutionEstimates.isEmpty()) {
                    fuelShedEstimate.setFuelShedEstimate(fuelShedTotalEstimate);
                    fuelShedEstimate.setInstitutionEstimates(institutionEstimates);
                    fuelShedEstimates.add(fuelShedEstimate);
                    totalEstimate += fuelShedTotalEstimate;
                }
            }
        }

        fuelEstimate.setTotalEstimate(totalEstimate);
        fuelEstimate.setFuelShedEstimates(fuelShedEstimates);
    }

    public List<FuelIssuedSummary> fillFuelIssuedToHealthInstitutionSummary(Date fd, Date td) {
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

        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), fi.name, fi.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), fi.name");

        return (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
    }

    public List<FuelIssuedSummary> fillFuelIssuedFromFuelStationSummary(Date fd, Date td) {
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

        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), ti.name, ti.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), ti.name");

        return (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
    }

    public List<FuelIssuedSummary> fillFuelIssuedFromFuelStationSummaryForInstitution(Date fd, Date td, Institution ins) {
        StringBuilder jpqlBuilder = new StringBuilder();
        jpqlBuilder.append("SELECT new lk.gov.health.phsp.pojcs.FuelIssuedSummary(")
                .append("FUNCTION('date', ft.issuedAt), ") // Group by issued date
                .append("ti.name, ") // toInstitution name
                .append("ti.id, ") // toInstitution ID
                .append("SUM(ft.issuedQuantity)") // sum of issued qty
                .append(") FROM FuelTransaction ft ")
                .append("LEFT JOIN ft.toInstitution ti ")
                .append("WHERE ft.retired = :ret ")
                .append("AND ft.fromInstitution = :fi ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);
        parameters.put("fi", ins);

        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
        }

        // Include all non-aggregated fields in the GROUP BY clause
        jpqlBuilder.append("GROUP BY FUNCTION('date', ft.issuedAt), ti.name, ti.id ");
        jpqlBuilder.append("ORDER BY FUNCTION('date', ft.issuedAt), ti.name");

        return (List<FuelIssuedSummary>) fuelTransactionFacade.findLightsByJpql(
                jpqlBuilder.toString(), parameters, TemporalType.TIMESTAMP);
    }

    public List<FuelIssuedSummary> fillFuelIssuedSummary(Institution fromInstitution, Institution toInstitution, Date fd, Date td) {
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
        if (fd != null && td != null) {
            jpqlBuilder.append("AND ft.issuedAt BETWEEN :fromDate AND :toDate ");
            parameters.put("fromDate", fd);
            parameters.put("toDate", td);
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
            fromDate = CommonController.startOfTheMonth();
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
    public List<Institution> getHospitals() {
        return hospitals;
    }

    public void setHospitals(List<Institution> hospitals) {
        this.hospitals = hospitals;
    }

    public double getTotalFuelEstimate() {
        return totalFuelEstimate;
    }

    public void setTotalFuelEstimate(double totalFuelEstimate) {
        this.totalFuelEstimate = totalFuelEstimate;
    }

    public FuelEstimate getFuelEstimate() {
        return fuelEstimate;
    }

    public void deleteSelected() {
        if (fuelTransaction == null) {
            return;
        }
        if (webUserController.getLoggedUser().getWebUserRole() != WebUserRole.SYSTEM_ADMINISTRATOR) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return;
        }
        fuelTransaction.setRetired(true);
        fuelTransaction.setRetiredAt(new Date());
        fuelTransaction.setRetiredBy(webUserController.getLoggedUser());
        fuelTransactionFacade.edit(fuelTransaction);
        JsfUtil.addSuccessMessage("Deleted");
    }

    public void reverseDeletionSelected() {
        if (fuelTransaction == null) {
            return;
        }
        if (webUserController.getLoggedUser().getWebUserRole() != WebUserRole.SYSTEM_ADMINISTRATOR) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return;
        }
        fuelTransaction.setRetired(false);
        fuelTransaction.setRetireReversedAt(new Date());
        fuelTransaction.setRetiredReversedBy(webUserController.getLoggedUser());
        fuelTransactionFacade.edit(fuelTransaction);
        JsfUtil.addSuccessMessage("Deletion Reversed");
    }

    public void setFuelEstimate(FuelEstimate fuelEstimate) {
        this.fuelEstimate = fuelEstimate;

    }

    public FuelTransactionFacade getFuelTransactionFacade() {
        return fuelTransactionFacade;
    }

    public void setFuelTransactionFacade(FuelTransactionFacade fuelTransactionFacade) {
        this.fuelTransactionFacade = fuelTransactionFacade;
    }

    public InstitutionApplicationController getInstitutionApplicationController() {
        return institutionApplicationController;
    }

    public void setInstitutionApplicationController(InstitutionApplicationController institutionApplicationController) {
        this.institutionApplicationController = institutionApplicationController;
    }

    public VehicleController getVehicleController() {
        return vehicleController;
    }

    public void setVehicleController(VehicleController vehicleController) {
        this.vehicleController = vehicleController;
    }

    public List<FuelEstimateRow> getFuelEstimateRows() {
        return fuelEstimateRows;
    }

    public void setFuelEstimateRows(List<FuelEstimateRow> fuelEstimateRows) {
        this.fuelEstimateRows = fuelEstimateRows;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public StreamedContent getDownloadingFile() {
        return downloadingFile;
    }

    public void setDownloadingFile(StreamedContent downloadingFile) {
        this.downloadingFile = downloadingFile;
    }

    public class FuelEstimate {

        private double totalEstimate;
        private List<FuelShedEstimate> fuelShedEstimates;

        public FuelEstimate() {
        }

        public double getTotalEstimate() {
            return totalEstimate;
        }

        public void setTotalEstimate(double totalEstimate) {
            this.totalEstimate = totalEstimate;
        }

        public List<FuelShedEstimate> getFuelShedEstimates() {
            return fuelShedEstimates;
        }

        public void setFuelShedEstimates(List<FuelShedEstimate> fuelShedEstimates) {
            this.fuelShedEstimates = fuelShedEstimates;
        }
    }

    public class FuelShedEstimate {

        private double fuelShedEstimate;
        private Institution fuelStation;
        private List<InstitutionEstimate> institutionEstimates;

        public FuelShedEstimate() {
        }

        public double getFuelShedEstimate() {
            return fuelShedEstimate;
        }

        public void setFuelShedEstimate(double fuelShedEstimate) {
            this.fuelShedEstimate = fuelShedEstimate;
        }

        public List<InstitutionEstimate> getInstitutionEstimates() {
            return institutionEstimates;
        }

        public void setInstitutionEstimates(List<InstitutionEstimate> institutionEstimates) {
            this.institutionEstimates = institutionEstimates;
        }

        public Institution getFuelStation() {
            return fuelStation;
        }

        public void setFuelStation(Institution fuelStation) {
            this.fuelStation = fuelStation;
        }
    }

    public class InstitutionEstimate {

        private Institution institution;
        private double institutionFuelEstimate;
        private List<Vehicle> vehicles;

        public InstitutionEstimate() {
        }

        public Institution getInstitution() {
            return institution;
        }

        public void setInstitution(Institution institution) {
            this.institution = institution;
        }

        public double getInstitutionFuelEstimate() {
            return institutionFuelEstimate;
        }

        public void setInstitutionFuelEstimate(double institutionFuelEstimate) {
            this.institutionFuelEstimate = institutionFuelEstimate;
        }

        public List<Vehicle> getVehicles() {
            return vehicles;
        }

        public void setVehicles(List<Vehicle> vehicles) {
            this.vehicles = vehicles;
        }
    }

    public class FuelEstimateRow {

        private FuelEstimateRowType row;
        private Institution fuelStation;
        private Double fuelStationEstimate;
        private Institution institution;
        private Double institutionEstimate;
        private Vehicle vehicle;
        private Double totalEstimate;

        public FuelEstimateRowType getRow() {
            return row;
        }

        public void setRow(FuelEstimateRowType row) {
            this.row = row;
        }

        public Institution getFuelStation() {
            return fuelStation;
        }

        public void setFuelStation(Institution fuelStation) {
            this.fuelStation = fuelStation;
        }

        public Double getFuelStationEstimate() {
            return fuelStationEstimate;
        }

        public void setFuelStationEstimate(Double fuelStationEstimate) {
            this.fuelStationEstimate = fuelStationEstimate;
        }

        public Institution getInstitution() {
            return institution;
        }

        public void setInstitution(Institution institution) {
            this.institution = institution;
        }

        public Double getInstitutionEstimate() {
            return institutionEstimate;
        }

        public void setInstitutionEstimate(Double institutionEstimate) {
            this.institutionEstimate = institutionEstimate;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        public Double getTotalEstimate() {
            return totalEstimate;
        }

        public void setTotalEstimate(Double totalEstimate) {
            this.totalEstimate = totalEstimate;
        }

    }

}
