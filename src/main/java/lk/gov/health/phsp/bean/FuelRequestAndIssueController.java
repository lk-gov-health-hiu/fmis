package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Bill;
import lk.gov.health.phsp.entity.BillItem;
import lk.gov.health.phsp.entity.DataAlterationRequest;
import lk.gov.health.phsp.entity.FuelPrice;
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DataAlterationRequestType;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.facade.BillFacade;
import lk.gov.health.phsp.facade.BillItemFacade;
import lk.gov.health.phsp.facade.DataAlterationRequestFacade;
import lk.gov.health.phsp.facade.FuelPriceFacade;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.VehicleFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.pojcs.BillItemMigrationResults;
import org.primefaces.event.CaptureEvent;

@Named
@SessionScoped
public class FuelRequestAndIssueController implements Serializable {

    @EJB
    private FuelTransactionHistoryFacade fuelTransactionHistoryFacade;
    @EJB
    private FuelTransactionFacade fuelTransactionFacade;
    @EJB
    private FuelPriceFacade fuelPriceFacade;
    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    VehicleFacade vehicleFacade;
    @EJB
    WebUserFacade webUserFacade;
    @EJB
    DataAlterationRequestFacade dataAlterationRequestFacade;
    @EJB
    BillFacade billFacade;
    @EJB
    BillItemFacade billItemFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    VehicleController vehicleController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    MenuController menuController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    VehicleApplicationController vehicleApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    @Inject
    QRCodeController qrCodeController;
    @Inject
    InstitutionController institutionController;

    private DataAlterationRequest dataAlterationRequest;
    private List<DataAlterationRequest> dataAlterationRequests;

    private List<FuelTransaction> transactions = null;
    private List<Bill> bills;
    private List<FuelTransaction> selectedTransactions = null;
    private FuelTransaction selected;
    private List<Institution> availableFuelStations;

    private FuelTransactionHistory selectedTransactionHistory;
    private List<FuelTransactionHistory> selectedTransactionHistories;
    private List<FuelTransactionHistory> transactionHistories;

    private Institution institution;
    private Institution fuelStation;
    private Vehicle vehicle;
    private WebUser webUser;
    private Date fromDate;
    private Date toDate;
    private FuelTransactionType fuelTransactionType;
    private boolean filterByIssuedDate = true; // true = filter by issued date, false = filter by requested date; default is issued date

    private Bill fuelPaymentRequestBill;
    private Bill selectedBill;
    private List<FuelTransaction> selectedBillTransactions;
    private BillItemMigrationResults migrationResults;
    private String billRejectionComment;
    private Map<Long, String> billItemComments; // Map of transaction ID to comment

    private String searchingFuelRequestVehicleNumber;

    // Filter properties for list_bills_to_accept
    private String billNumberFilter;
    private Institution filterFromInstitution;
    private Institution filterFuelStation;

    public FuelRequestAndIssueController() {
    }

    public void fixDates() {
        updateRequestedAtIfNull();
        updateRequestedDateIfNull();
        updateIssuedDateIfNull();
    }

    public void updateRequestedAtIfNull() {
        System.out.println("updateRequestedAtIfNull");
        String jpql = "select ft "
                + " from FuelTransaction ft "
                + " where ft.requestAt is null";
        List<FuelTransaction> fts = getFacade().findByJpql(jpql);
        if (fts == null || fts.isEmpty()) {
            JsfUtil.addErrorMessage("All requestAt are not null");
            return;
        }
        for (FuelTransaction ft : fts) {
            if (ft.getRequestAt() == null) {
                if (ft.getCreatedAt() != null) {
                    ft.setRequestAt(ft.getCreatedAt());
                    getFacade().edit(ft);
                    System.out.println("ft updated to created time = " + ft);
                } else {
                    System.err.println("ft without a created time = " + ft);
                }
            }
        }
    }

    public void updateRequestedDateIfNull() {
        System.out.println("updateRequestedDateIfNull");
        String jpql = "select ft "
                + " from FuelTransaction ft "
                + " where ft.requestedDate is null";
        List<FuelTransaction> fts = getFacade().findByJpql(jpql);
        if (fts == null || fts.isEmpty()) {
            JsfUtil.addErrorMessage("All requestedDate are not null");
            return;
        }
        System.out.println("fts = " + fts.size());
        for (FuelTransaction ft : fts) {
            System.out.println("ft.getRequestedDate() = " + ft.getRequestedDate());

            if (ft.getRequestAt() != null) {
                ft.setRequestedDate(ft.getRequestAt());
                getFacade().edit(ft);
                System.out.println("ft getRequestedDate updated to cgetRequestAt = " + ft);
            } else if (ft.getCreatedAt() != null) {
                ft.setRequestedDate(ft.getCreatedAt());
                getFacade().edit(ft);
                System.out.println("ft getRequestedDate updated to getCreatedAt = " + ft);
            } else {
                System.err.println("ft without a requested date = " + ft);
            }

        }
    }

    public void updateIssuedDateIfNull() {
        System.out.println("updateIssuedDateIfNull");
        String jpql = "select ft "
                + " from FuelTransaction ft "
                + " where ft.issuedDate is null";
        List<FuelTransaction> fts = getFacade().findByJpql(jpql);
        if (fts == null || fts.isEmpty()) {
            JsfUtil.addErrorMessage("All issuedDate are not null");
            return;
        }
        System.out.println("fts = " + fts.size());
        for (FuelTransaction ft : fts) {

            if (ft.getIssuedAt() != null) {
                ft.setIssuedDate(ft.getIssuedAt());
                getFacade().edit(ft);
                System.out.println("ft getRequestedDate updated to cgetRequestAt = " + ft);
            } else if (ft.getCreatedAt() != null) {
                ft.setIssuedDate(ft.getCreatedAt());
                getFacade().edit(ft);
                System.out.println("ft getRequestedDate updated to getCreatedAt = " + ft);
            } else {
                System.err.println("ft without a issued date = " + ft);
            }

        }
    }

    public String searchFuelRequestByVehicleNumber() {
        if (searchingFuelRequestVehicleNumber == null || searchingFuelRequestVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please provide a vehicle number");
            return "";
        }

        Institution toInstitution = webUserController.getLoggedInstitution();
        List<Vehicle> vs = vehicleController.searchVehicles(searchingFuelRequestVehicleNumber);
        if (vs == null || vs.isEmpty()) {
            JsfUtil.addErrorMessage("No Matching Vehicle");
            return "";
        }

        List<FuelTransaction> searchResults = findFuelTransactions(null,
                null,
                toInstitution,
                vs,
                null,
                null,
                null, null, null);

        if (searchResults == null || searchResults.isEmpty()) {
            JsfUtil.addErrorMessage("No search results. Please check and retry.");
            return "";
        }
        if (searchResults.size() == 1) {
            selected = searchResults.get(0);
            return navigateToIssueVehicleFuelRequest();
        } else {
            selected = null;
            transactions = searchResults;
            return navigateToSelectToIssueVehicleFuelRequest();
        }
    }

    public String searchFuelRequestToIssueByVehicleNumber() {
        if (searchingFuelRequestVehicleNumber == null || searchingFuelRequestVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please provide a vehicle number");
            return "";
        }

        Institution toInstitution = webUserController.getLoggedInstitution();
        List<Vehicle> vs = vehicleController.searchVehicles(searchingFuelRequestVehicleNumber);
        if (vs == null || vs.isEmpty()) {
            JsfUtil.addErrorMessage("No Matching Vehicle");
            return "";
        }

        List<FuelTransaction> searchResults = findFuelTransactions(null,
                null,
                toInstitution,
                vs,
                null,
                null,
                false, false, false);

        if (searchResults == null || searchResults.isEmpty()) {
            JsfUtil.addErrorMessage("No search results. Please check and retry.");
            return "";
        }
        if (searchResults.size() == 1) {
            selected = searchResults.get(0);
            return navigateToIssueVehicleFuelRequest();
        } else {
            selected = null;
            transactions = searchResults;
            return navigateToSelectToIssueVehicleFuelRequest();
        }
    }

    public String navigateToIssueVehicleFuelRequest() {
        return "/issues/issue?faces-redirect=true";
    }

    public String navigateToMarkVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getFromInstitution() == null) {
            JsfUtil.addErrorMessage("No from Institution");
            return "";
        }
        if (selected.getFromInstitution().getSupplyInstitution() == null) {
            JsfUtil.addErrorMessage("No Fuel Station selected for your Institution");
            return "";
        }
        if (selected.getToInstitution() == null) {
            selected.setToInstitution(selected.getFromInstitution().getSupplyInstitution());
        }
        selected.setIssuedQuantity(selected.getRequestQuantity());
        selected.setIssuedInstitution(selected.getToInstitution());
        return "/requests/mark?faces-redirect=true";
    }

    public String navigateToViewIssuedVehicleFuelRequest() {
        return "/issues/issued?faces-redirect=true";
    }

    public void rejectFuelIssueAtDepot() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRejected(true);
        selected.setRejectedAt(new Date());
        selected.setRejectedBy(webUserController.getLoggedUser());
        selected.setRejectedInstitution(webUserController.getLoggedInstitution());
        getFacade().edit(selected);
        transactions.remove(selected);
        JsfUtil.addSuccessMessage("Rejected");
    }

    public void issueFuelIssueAtDepotDirectly() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }

        if (selected.getTransactionType() == null) {
            selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        }

        selected.setIssuedQuantity(selected.getRequestQuantity());

        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedInstitution(webUserController.getLoggedInstitution());
        selected.setIssuedUser(webUserController.getLoggedUser());
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        transactions.remove(selected);
        JsfUtil.addSuccessMessage("Rejected");
    }

    public String navigateToReceiveFuelAtDepot() {
        selected = new FuelTransaction();
        selected.setTransactionType(FuelTransactionType.CtbFuelReceive);
        selected.setToInstitution(webUserController.getLoggedInstitution());
        selected.setTxDate(new Date());
        selected.setTxTime(new Date());
        selected.setInstitution(webUserController.getLoggedInstitution());
        return "/depot/receive?faces-redirect=true";
    }

    public String navigateToSelectToIssueVehicleFuelRequest() {
        return "/issues/select_issue?faces-redirect=true";
    }

    public void saveSelected() {
        if (selected == null) {
            return;
        }
        save(selected);
    }

    public String submitVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getRequestedDate() == null) {
            JsfUtil.addErrorMessage("Select Requested Date");
            return "";
        }
        if (selected.getToInstitution() == null) {
            JsfUtil.addErrorMessage("Select Fuel Station");
            return "";
        }
        if (selected.getRequestReferenceNumber() == null) {
            JsfUtil.addErrorMessage("Enter a referance number");
            return "";
        }
        if (selected.getRequestReferenceNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("Enter a referance number");
            return "";
        }

        // Validation 1: Check if reference number is unique within 30 days for this institution
        if (!isReferenceNumberUnique(selected.getRequestReferenceNumber(), webUserController.getLoggedInstitution())) {
            JsfUtil.addErrorMessage("Reference number '" + selected.getRequestReferenceNumber() + "' has already been used within the last 30 days for this institution");
            return "";
        }

        // Validation 2: Check if reference number and ODO meter differ from request quantity
        if (!areFieldValuesDistinct(selected.getRequestReferenceNumber(), selected.getOdoMeterReading(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Request Reference Number and ODO Meter Reading must be different from Request Quantity");
            return "";
        }

        // Validation 3: Check if ODO reading is greater than previous reading
        Double previousOdoReading = getPreviousOdoReading(selected.getVehicle());
        if (previousOdoReading != null && selected.getOdoMeterReading() != null) {
            if (selected.getOdoMeterReading() <= previousOdoReading) {
                JsfUtil.addErrorMessage("ODO Meter Reading (" + selected.getOdoMeterReading() + ") must be greater than the previous reading (" + previousOdoReading + ")");
                return "";
            }
        }

        // Validation 4: Check if there's already a pending request for this vehicle
        if (hasPendingRequest(selected.getVehicle())) {
            JsfUtil.addErrorMessage("This vehicle already has a pending fuel request that has not been issued yet. Please wait for that request to be processed first.");
            return "";
        }

        // Validation 5: Check if request quantity exceeds vehicle fuel capacity
        if (!isRequestQuantityWithinCapacity(selected.getVehicle(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Requested quantity (" + selected.getRequestQuantity() + " liters) exceeds the vehicle's fuel tank capacity (" + selected.getVehicle().getFuelCapacity() + " liters)");
            return "";
        }

        selected.setRequestAt(new Date());
        save(selected);
        JsfUtil.addSuccessMessage("Request Submitted");
        return navigateToViewInstitutionFuelRequestToSltbDepot();
    }

    public String submitSpecialVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getVehicle() == null) {
            JsfUtil.addErrorMessage("No Vehicle Selected");
            return "";
        }
        if (selected.getVehicle().getInstitution() == null) {
            JsfUtil.addErrorMessage("No Institution foind for the selected Vehicle");
            return "";
        }
        if (selected.getRequestedDate() == null) {
            JsfUtil.addErrorMessage("Enter Requested Date");
            return "";
        }

        // Additional validations for required fields
        if (selected.getRequestReferenceNumber() == null || selected.getRequestReferenceNumber().trim().isEmpty()) {
            JsfUtil.addErrorMessage("Request Reference Number is required");
            return "";
        }
        if (selected.getOdoMeterReading() == null) {
            JsfUtil.addErrorMessage("ODO Meter Reading is required");
            return "";
        }
        if (selected.getRequestQuantity() == null) {
            JsfUtil.addErrorMessage("Request Quantity is required");
            return "";
        }

        // Validation 1: Check if reference number is unique within 30 days for this institution
        if (!isReferenceNumberUnique(selected.getRequestReferenceNumber(), selected.getVehicle().getInstitution())) {
            JsfUtil.addErrorMessage("Reference number '" + selected.getRequestReferenceNumber() + "' has already been used within the last 30 days for this institution");
            return "";
        }

        // Validation 2: Check if reference number and ODO meter differ from request quantity
        if (!areFieldValuesDistinct(selected.getRequestReferenceNumber(), selected.getOdoMeterReading(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Request Reference Number and ODO Meter Reading must be different from Request Quantity");
            return "";
        }

        // Validation 3: Check if ODO reading is greater than previous reading
        Double previousOdoReading = getPreviousOdoReading(selected.getVehicle());
        if (previousOdoReading != null && selected.getOdoMeterReading() != null) {
            if (selected.getOdoMeterReading() <= previousOdoReading) {
                JsfUtil.addErrorMessage("ODO Meter Reading (" + selected.getOdoMeterReading() + ") must be greater than the previous reading (" + previousOdoReading + ")");
                return "";
            }
        }

        // Validation 4: Check if there's already a pending request for this vehicle
        if (hasPendingRequest(selected.getVehicle())) {
            JsfUtil.addErrorMessage("This vehicle already has a pending fuel request that has not been issued yet. Please wait for that request to be processed first.");
            return "";
        }

        // Validation 5: Check if request quantity exceeds vehicle fuel capacity
        if (!isRequestQuantityWithinCapacity(selected.getVehicle(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Requested quantity (" + selected.getRequestQuantity() + " liters) exceeds the vehicle's fuel tank capacity (" + selected.getVehicle().getFuelCapacity() + " liters)");
            return "";
        }

        selected.setInstitution(selected.getVehicle().getInstitution());
        if (selected.getTxDate() == null) {
            selected.setTxDate(new Date());
        }
        if (selected.getTxTime() == null) {
            selected.setTxTime(new Date());
        }
        selected.setRequestAt(new Date());
        save(selected);
        JsfUtil.addSuccessMessage("Special Fuel Request Submitted");
        return navigateToViewInstitutionFuelRequestToSltbDepot();
    }

    public String submitSltbFuelRequestFromCpc() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.DepotFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        save(selected);
        JsfUtil.addSuccessMessage("Request Submitted");
        return navigateToViewDepotFuelRequestToCpc();
    }

    public String submitVehicleFuelRequestIssue() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getIssuedQuantity() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() < 1.0) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() > selected.getRequestQuantity()) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedInstitution(webUserController.getLoggedInstitution());
        selected.setIssuedUser(webUserController.getLoggedUser());
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Issued");
        return navigateToSearchRequestsForVehicleFuelIssue();
    }

    public String submitMarkVehicleFuelRequestIssue() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getIssuedQuantity() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() < 1.0) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() > selected.getRequestQuantity()) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedDate() == null) {
            JsfUtil.addErrorMessage("Need Issued Date");
            return "";
        }

        // Validation: Check if issue reference number equals request reference number
        if (selected.getIssueReferenceNumber() != null && selected.getRequestReferenceNumber() != null) {
            if (selected.getIssueReferenceNumber().trim().equalsIgnoreCase(selected.getRequestReferenceNumber().trim())) {
                JsfUtil.addErrorMessage("Issue Reference Number cannot be the same as Request Reference Number");
                return "";
            }
        }

        // Validation: Check if issue date is not before request date
        if (selected.getIssuedDate() != null && selected.getRequestedDate() != null) {
            // Reset time to 00:00:00 for date-only comparison
            java.util.Calendar issuedCal = java.util.Calendar.getInstance();
            issuedCal.setTime(selected.getIssuedDate());
            issuedCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            issuedCal.set(java.util.Calendar.MINUTE, 0);
            issuedCal.set(java.util.Calendar.SECOND, 0);
            issuedCal.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar requestedCal = java.util.Calendar.getInstance();
            requestedCal.setTime(selected.getRequestedDate());
            requestedCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            requestedCal.set(java.util.Calendar.MINUTE, 0);
            requestedCal.set(java.util.Calendar.SECOND, 0);
            requestedCal.set(java.util.Calendar.MILLISECOND, 0);

            if (issuedCal.before(requestedCal)) {
                JsfUtil.addErrorMessage("Issue Date cannot be before Request Date");
                return "";
            }
        }

        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedUser(webUserController.getLoggedUser());
        if (selected.getIssuedInstitution() == null) {
            selected.setIssuedInstitution(selected.getToInstitution());
        }
//        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
//        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Issued");
        listInstitutionRequestsToMark();
        return navigateToListInstitutionRequestsToMark();
    }

    public String submitVehicleFuelReceive() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.CtbFuelReceive) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getReceivedQty() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.addToStock(webUserController.getLoggedInstitution(), selected.getReceivedQty()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Received");
        return navigateToListDepotReceiveList();
    }

    public String navigateToListDepotReceiveList() {
        institution = webUserController.getLoggedInstitution();
        fillDepotReceiveList();
        return "/depot/depot_receive_list?faces-redirect=true";
    }

    public String navigateToSltbReportsFuelRequests() {
        return "/sltb/reports/requests?faces-redirect=true";
    }

    public String navigateToSltbReportsFuelIssues() {
        return "/sltb/reports/issues?faces-redirect=true";
    }

    public String navigateToSltbReportsFuelRejections() {
        return "/sltb/reports/rejections?faces-redirect=true";
    }

    public void fillDepotReceiveList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, null, null, null, null);
    }

    public void fillDepotToIssueList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, Boolean.FALSE,
                Boolean.FALSE, Boolean.FALSE, null);
    }

    public void fillIssuedRequestsFromDepotList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, Boolean.TRUE,
                null, null, null);
    }

    public void fillRejectedIssueRequestsFromDepotList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, null,
                null, Boolean.TRUE, null);
    }

    public String navigateToListFuelTransactions() {
        return "/issues/list?faces-redirect=true";
    }

    public String navigateToViewVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        return "/issues/requested?faces-redirect=true";
    }

    public String navigateToAddVehicleFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setToInstitution(webUserController.getLoggedInstitution().getSupplyInstitution());
        if (webUserController.getManagableVehicles().size() == 1) {
            selected.setVehicle(webUserController.getManagableVehicles().get(0));
        }
        if (webUserController.getManagableDrivers().size() == 1) {
            selected.setDriver(webUserController.getManagableDrivers().get(0));
        }
        return "/requests/request?faces-redirect=true";
    }

    public String navigateToAddCpcFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.DepotFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(institutionApplicationController.findCpc());
        return "/moh/request?faces-redirect=true";
    }

    public String navigateToViewDeleteRequestsToAttend() {
        dataAlterationRequests = findDataAlterationRequests(null, null, null, false, false, DataAlterationRequestType.DELETE_REQUEST);
        return "/national/admin/delete_requests_to_attend?faces-redirect=true";
    }

    public void fillDeleteRequests() {
        dataAlterationRequests = findDataAlterationRequests(fromDate, toDate, institution, null, null, DataAlterationRequestType.DELETE_REQUEST);
    }

    public List<DataAlterationRequest> findDataAlterationRequests(Date fromDate, Date toDate, Institution ins, Boolean completed, Boolean rejected, DataAlterationRequestType type) {
        List<DataAlterationRequest> lst;
        StringBuilder jpql = new StringBuilder("select t from DataAlterationRequest t where 1=1");
        Map<String, Object> parameters = new HashMap<>();

        if (fromDate != null) {
            jpql.append(" and t.requestedDate >= :fromDate");
            parameters.put("fromDate", fromDate);
        }

        if (toDate != null) {
            jpql.append(" and t.requestedDate <= :toDate");
            parameters.put("toDate", toDate);
        }

        if (ins != null) {
            jpql.append(" and t.requestedInstitution = :ins");
            parameters.put("ins", ins);
        }

        if (completed != null) {
            jpql.append(" and t.completed = :completed");
            parameters.put("completed", completed);
        }

        if (rejected != null) {
            jpql.append(" and t.rejected = :rejected");
            parameters.put("rejected", rejected);
        }

        if (type != null) {
            jpql.append(" and t.dataAlterationRequestType = :type");
            parameters.put("type", type);
        }

        lst = dataAlterationRequestFacade.findByJpql(jpql.toString(), parameters);
        return lst;
    }

    public String navigateToDeleteRequest(DataAlterationRequest daq) {
        if (daq == null) {
            JsfUtil.addErrorMessage("Error");
            return "";
        }
        if (daq.getDataAlterationRequestType() != DataAlterationRequestType.DELETE_REQUEST) {
            JsfUtil.addErrorMessage("Error. Not a Delete Request");
            return "";
        }
        if (daq.getFuelTransaction() == null) {
            JsfUtil.addErrorMessage("Error. No Trnasaction to delete");
            return "";
        }
        dataAlterationRequest = daq;
        selected = daq.getFuelTransaction();
        return "/national/admin/request_delete?faces-redirect=true";
    }

    public String deleteRequest() {
        if (dataAlterationRequest == null) {
            JsfUtil.addErrorMessage("Error");
            return "";
        }
        if (dataAlterationRequest.getDataAlterationRequestType() != DataAlterationRequestType.DELETE_REQUEST) {
            JsfUtil.addErrorMessage("Error. Not a Delete Request");
            return "";
        }
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Error. No Trnasaction to delete");
            return "";
        }
        FuelTransaction ft = getSelected();
        ft.setRetired(true);
        ft.setRetiredAt(new Date());
        ft.setRetiredBy(webUserController.getLoggedUser());
        if (ft.getId() == null) {
            fuelTransactionFacade.create(ft);
        } else {
            fuelTransactionFacade.edit(ft);
        }
        dataAlterationRequest.setCompleted(true);
        dataAlterationRequest.setCompletedAt(new Date());
        dataAlterationRequest.setCompletedBy(webUserController.getLoggedUser());
        dataAlterationRequest.setCompletedDate(new Date());
        if (dataAlterationRequest.getId() == null) {
            dataAlterationRequestFacade.create(dataAlterationRequest);
        } else {
            dataAlterationRequestFacade.edit(dataAlterationRequest);
        }
        setSelected(null);
        setDataAlterationRequest(null);
        JsfUtil.addErrorMessage("Deleted");
        return navigateToViewDeleteRequestsToAttend();
    }

    public String navigateToViewChangeRequestsToAttend() {
        return "/national/admin/delete_requests_to_attend?faces-redirect=true";
    }

    public String navigateToAddSpecialVehicleFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.SpecialVehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setToInstitution(webUserController.getLoggedInstitution().getSupplyInstitution());
        return "/requests/special_request?faces-redirect=true";
    }

    public String navigateToSearchRequestsForVehicleFuelIssue() {
        return "/issues/search?faces-redirect=true";
    }

    public String navigateToSearchRequestsForVehicleFuelIssueQr() {
        return "/issues/search_qr?faces-redirect=true";
    }

    public String generateRequest() {
        return "/requests/requested?faces-redirect=true";
    }

    public String completeIssue() {
        return "/issues/issued?faces-redirect=true";
    }

    public String navigateToListInstitutionRequests() {
        listInstitutionRequests();
        return "/requests/list?faces-redirect=true";
    }

    public String navigateToMakePayment() {
        listInstitutionRequestsToPay();
        paymentRequestStarted = false;
        return "/requests/list_to_pay?faces-redirect=true";
    }

    public String navigateToPaymentsMade() {
        listInstitutionRequestsPaid();
        return "/requests/list_to_paid?faces-redirect=true";
    }

    public String navigateToListPaymentBills() {
        listPaymentBills();
        return "/requests/list_payment_bills?faces-redirect=true";
    }

    public String navigateToViewPaymentBill() {
        if (selectedBill == null) {
            JsfUtil.addErrorMessage("No bill selected");
            return null;
        }
        loadBillTransactions();
        return "/requests/view_payment_bill?faces-redirect=true";
    }

    public String navigateToListBillsToAcceptAtCpc() {
        listBillsToAcceptAtCpc();
        return "/requests/list_bills_to_accept_at_cpc?faces-redirect=true";
    }

    public String navigateToListBillsAcceptedByCpc() {
        listBillsAcceptedByCpc();
        return "/requests/list_bills_accepted_by_cpc?faces-redirect=true";
    }

    public String navigateToListBillsRejectedByCpc() {
        listBillsRejectedByCpc();
        return "/requests/list_bills_rejected_by_cpc?faces-redirect=true";
    }

    // CPC-specific navigation methods
    public String navigateToCpcListPaymentBills() {
        listPaymentBills();
        return "/cpc/list_payment_bills?faces-redirect=true";
    }

    public String navigateToCpcViewPaymentBill() {
        if (selectedBill == null) {
            JsfUtil.addErrorMessage("No bill selected");
            return null;
        }
        loadBillTransactions();
        return "/cpc/view_payment_bill?faces-redirect=true";
    }

    public String navigateToCpcListBillsToAccept() {
        listBillsToAcceptAtCpc();
        return "/cpc/list_bills_to_accept?faces-redirect=true";
    }

    public String navigateToCpcListBillsAccepted() {
        listBillsAcceptedByCpc();
        return "/cpc/list_bills_accepted?faces-redirect=true";
    }

    public String navigateToCpcListBillsRejected() {
        listBillsRejectedByCpc();
        return "/cpc/list_bills_rejected?faces-redirect=true";
    }

    public String navigateToCpcBillAction() {
        if (selectedBill == null) {
            JsfUtil.addErrorMessage("No bill selected");
            return null;
        }
        loadBillTransactions();
        // Clear rejection comment fields
        billRejectionComment = null;
        billItemComments = new HashMap<>();
        return "/requests/cpc_bill_action?faces-redirect=true";
    }

    public String navigateToMigrateBillItems() {
        migrationResults = null; // Clear previous results
        return "/admin/migrate_bill_items?faces-redirect=true";
    }

    public String navigateToCreateNewDeleteRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a transaction");
            return null;
        }
        dataAlterationRequest = new DataAlterationRequest();
        dataAlterationRequest.setFuelTransaction(selected);
        dataAlterationRequest.setDataAlterationRequestType(DataAlterationRequestType.DELETE_REQUEST);
        return "/requests/requested_to_delete?faces-redirect=true";
    }

    public String submitNewDeleteRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a transaction");
            return null;
        }
        if (dataAlterationRequest == null) {
            JsfUtil.addErrorMessage("Select a transaction");
            return null;
        }
        dataAlterationRequest.setFuelTransaction(selected);
        dataAlterationRequest.setDataAlterationRequestType(DataAlterationRequestType.DELETE_REQUEST);
        dataAlterationRequest.setRequestAt(new Date());
        dataAlterationRequest.setRequestedBy(webUserController.getLoggedUser());
        dataAlterationRequest.setRequestedDate(new Date());
        dataAlterationRequest.setRequestedInstitution(webUserController.getLoggedInstitution());
        dataAlterationRequest.setCompleted(Boolean.FALSE);
        dataAlterationRequest.setRejected(Boolean.FALSE);
        dataAlterationRequest.setCreatedAt(new Date());
        dataAlterationRequest.setCreatedBy(webUserController.getLoggedUser());
        if (dataAlterationRequest.getId() == null) {
            dataAlterationRequestFacade.create(dataAlterationRequest);
        } else {
            dataAlterationRequestFacade.edit(dataAlterationRequest);
        }
        JsfUtil.addSuccessMessage("Successfully Submitted. Referenace Number is " + dataAlterationRequest.getId());
        dataAlterationRequest = null;
        selected = null;
        return navigateToListInstitutionRequests();
    }

    public String navigateToCreateNewDataChangeRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a transaction");
            return null;
        }
        return "/requests/requested_to_delete?faces-redirect=true";
    }

    public String navigateToListInstitutionRequestsToMark() {
        listInstitutionRequestsToMark();
        return "/requests/list_to_mark?faces-redirect=true";
    }

    public String navigateToListSltbRequestsFromCpc() {
        return "/moh/list?faces-redirect=true";
    }

    public String onCaptureOfVehicleQr(CaptureEvent captureEvent) {
        byte[] imageData = captureEvent.getData();
        searchingFuelRequestVehicleNumber = qrCodeController.scanQRCode(imageData);
        return searchFuelRequestToIssueByVehicleNumber();
    }

    public void listPaymentBills() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.toInstitution IN :fuelStations "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("fuelStations", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listPaymentBillsForInstitutions() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.fromInstitution IN :institutions "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("institutions", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listPaymentBillsForNationalLevel() {
        StringBuilder j = new StringBuilder();
        j.append("SELECT DISTINCT b ")
                .append(" FROM Bill b ")
                .append(" LEFT JOIN FuelTransaction ft ON ft.paymentBill = b ")
                .append(" WHERE b.retired = false ");

        Map<String, Object> params = new HashMap<>();

        if (fromDate != null && toDate != null) {
            if (filterByIssuedDate) {
                j.append(" AND ft.issuedDate BETWEEN :fromDate AND :toDate");
            } else {
                j.append(" AND ft.requestedDate BETWEEN :fromDate AND :toDate");
            }
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        }

        if (institution != null) {
            j.append(" AND b.fromInstitution=:institution ");
            params.put("institution", institution);
        } else {
            j.append(" AND b.fromInstitution IN :institutions ");
            params.put("institutions", webUserController.findAutherizedInstitutions());
        }
        if (fuelStation != null) {
            j.append(" AND b.toInstitution=:fs ");
            params.put("fs", fuelStation);
        }

        System.out.println("params = " + params);
        System.out.println("j = " + j.toString());

        List<Bill> tmpBills = billFacade.findByJpql(j.toString(), params);
        bills = tmpBills;
    }

    public void listPaymentBillsForCpcHeadOffice() {
        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        if (institution != null) {
            j += " AND b.fromInstitution=:institution ";
            params.put("institution", institution);
        }
        if (fuelStation != null) {
            j += " AND b.toInstitution=:fs ";
            params.put("fs", fuelStation);
        }
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listPaymentBillsForCpcRegionalOffice() {
        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        if (institution != null) {
            j += " AND b.fromInstitution=:institution ";
            params.put("institution", institution);
        }
        if (fuelStation != null) {
            j += " AND b.toInstitution=:fs ";
            params.put("fs", fuelStation);
        } else {
            j += " AND b.toInstitution IN :institutions ";
            params.put("institutions", webUserController.findAutherizedInstitutions());
        }
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsToAcceptAtCpc() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.acceptedByCpc = false "
                + " AND b.rejectedByCpc = false "
                + " AND b.toInstitution IN :fuelStations "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("fuelStations", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        // Add bill number filter
        if (billNumberFilter != null && !billNumberFilter.trim().isEmpty()) {
            j += " AND b.billNo LIKE :billNo";
            params.put("billNo", "%" + billNumberFilter.trim() + "%");
        }

        // Add from institution filter
        if (filterFromInstitution != null) {
            j += " AND b.fromInstitution = :fromInstitution";
            params.put("fromInstitution", filterFromInstitution);
        }

        // Add fuel station filter
        if (filterFuelStation != null) {
            j += " AND b.toInstitution = :toInstitution";
            params.put("toInstitution", filterFuelStation);
        }

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsToAcceptAtCpcForInstitutions() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.acceptedByCpc = false "
                + " AND b.rejectedByCpc = false "
                + " AND b.fromInstitution IN :ins "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("ins", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsAcceptedByCpc() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.acceptedByCpc = true "
                + " AND b.toInstitution IN :fuelStations "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("fuelStations", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        // Add bill number filter
        if (billNumberFilter != null && !billNumberFilter.trim().isEmpty()) {
            j += " AND b.billNo LIKE :billNo";
            params.put("billNo", "%" + billNumberFilter.trim() + "%");
        }

        // Add from institution filter
        if (filterFromInstitution != null) {
            j += " AND b.fromInstitution = :fromInstitution";
            params.put("fromInstitution", filterFromInstitution);
        }

        // Add fuel station filter
        if (filterFuelStation != null) {
            j += " AND b.toInstitution = :toInstitution";
            params.put("toInstitution", filterFuelStation);
        }

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsAcceptedByCpcForInstitutions() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.acceptedByCpc = true "
                + " AND b.fromInstitution IN :ins "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("ins", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsRejectedByCpc() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.rejectedByCpc = true "
                + " AND b.toInstitution IN :fuelStations "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("fuelStations", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listBillsRejectedByCpcForInstituion() {
        // Validate that fromDate and toDate are in the same month
        if (!areDatesInSameMonth(fromDate, toDate)) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            bills = new ArrayList<>();
            return;
        }

        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.rejectedByCpc = true "
                + " AND b.fromInstitution IN :ins "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("ins", webUserController.getLoggableInstitutions());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public String acceptBillAtCpc() {
        if (selectedBill == null) {
            JsfUtil.addErrorMessage("No bill selected");
            return null;
        }

        Date acceptanceDate = new Date();
        WebUser acceptanceUser = webUserController.getLoggedUser();

        // Mark bill as accepted (and clear all rejection-related fields)
        selectedBill.setAcceptedByCpc(true);
        selectedBill.setRejectedByCpc(false);
        selectedBill.setRejectedByCpcUser(null);
        selectedBill.setRejectedByCpcAt(null);
        selectedBill.setRejectionComment(null);
        selectedBill.setAcceptedByCpcUser(acceptanceUser);
        selectedBill.setAcceptedByCpcAt(acceptanceDate);

        // Get all BillItems for this bill
        String billItemsJpql = "SELECT bi FROM BillItem bi "
                + "WHERE bi.bill = :bill "
                + "AND bi.retired = false";

        Map<String, Object> params = new HashMap<>();
        params.put("bill", selectedBill);

        List<BillItem> billItems = billItemFacade.findByJpql(billItemsJpql, params);

        // Update each FuelTransaction with acceptance information
        for (BillItem billItem : billItems) {
            FuelTransaction ft = billItem.getFuelTransaction();
            if (ft != null && !ft.isRetired()) {
                // Mark as accepted and clear any rejection data
                ft.setAcceptedByCpcAt(acceptanceDate);
                ft.setAcceptedByCpcBy(acceptanceUser);
                ft.setRejectedByCpcAt(null);
                ft.setRejectedByCpcBy(null);
                fuelTransactionFacade.edit(ft);
            }
        }

        // Save the bill
        billFacade.edit(selectedBill);

        JsfUtil.addSuccessMessage("Payment bill accepted successfully. " + billItems.size() + " transaction(s) updated.");

        // Navigate back to the pending bills list
        return navigateToListBillsToAcceptAtCpc();
    }

    public String rejectBillAtCpc() {
        if (selectedBill == null) {
            JsfUtil.addErrorMessage("No bill selected");
            return null;
        }

        // Validate: Rejection comment is mandatory
        if (billRejectionComment == null || billRejectionComment.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Rejection comment is mandatory. Please provide a reason for rejecting this bill.");
            return null;
        }

        Date rejectionDate = new Date();
        WebUser rejectionUser = webUserController.getLoggedUser();

        // Mark bill as rejected (and clear all acceptance-related fields)
        selectedBill.setRejectedByCpc(true);
        selectedBill.setAcceptedByCpc(false);
        selectedBill.setAcceptedByCpcUser(null);
        selectedBill.setAcceptedByCpcAt(null);
        selectedBill.setRejectedByCpcUser(rejectionUser);
        selectedBill.setRejectedByCpcAt(rejectionDate);
        selectedBill.setRejectionComment(billRejectionComment.trim());

        // Get all BillItems for this bill
        String billItemsJpql = "SELECT bi FROM BillItem bi "
                + "WHERE bi.bill = :bill "
                + "AND bi.retired = false";

        Map<String, Object> params = new HashMap<>();
        params.put("bill", selectedBill);

        List<BillItem> billItems = billItemFacade.findByJpql(billItemsJpql, params);

        // Save bill item comments and reverse transactions
        for (BillItem billItem : billItems) {
            FuelTransaction ft = billItem.getFuelTransaction();
            if (ft != null && !ft.isRetired()) {
                // Save bill item comment if provided
                if (billItemComments != null && billItemComments.containsKey(ft.getId())) {
                    String comment = billItemComments.get(ft.getId());
                    if (comment != null && !comment.trim().isEmpty()) {
                        billItem.setComment(comment.trim());
                        billItemFacade.edit(billItem);
                    }
                }

                // Mark transaction as rejected by CPC and clear any acceptance data
                ft.setRejectedByCpcAt(rejectionDate);
                ft.setRejectedByCpcBy(rejectionUser);
                ft.setAcceptedByCpcAt(null);
                ft.setAcceptedByCpcBy(null);

                // Reverse the transaction - clear paymentBill so it can be resubmitted
                ft.setSubmittedToPayment(false);
                ft.setSubmittedToPaymentAt(null);
                ft.setSubmittedToPaymentBy(null);
                ft.setPaymentBill(null);  // Clear so transaction can be resubmitted to a new bill
                fuelTransactionFacade.edit(ft);
            }
        }

        // Save the bill
        billFacade.edit(selectedBill);

        JsfUtil.addSuccessMessage("Payment bill rejected and all transactions reversed successfully. " + billItems.size() + " transaction(s) marked as rejected by CPC.");

        // Navigate back to the pending bills list
        return navigateToListBillsToAcceptAtCpc();
    }

    public String migrateBillItems() {
        migrationResults = new BillItemMigrationResults();
        migrationResults.setStartTime(new Date());

        try {
            // Get all bills
            String jpql = "SELECT b FROM Bill b WHERE b.retired = false";
            List<Bill> allBills = billFacade.findByJpql(jpql);

            for (Bill bill : allBills) {
                migrationResults.setBillsProcessed(migrationResults.getBillsProcessed() + 1);

                try {
                    // Check if this bill already has BillItems
                    String checkJpql = "SELECT bi FROM BillItem bi WHERE bi.bill = :bill AND bi.retired = false";
                    Map<String, Object> checkParams = new HashMap<>();
                    checkParams.put("bill", bill);
                    List<BillItem> existingItems = billItemFacade.findByJpql(checkJpql, checkParams);

                    if (existingItems != null && !existingItems.isEmpty()) {
                        // Bill already has items, skip
                        migrationResults.setBillsSkipped(migrationResults.getBillsSkipped() + 1);
                        continue;
                    }

                    // Find all transactions that reference this bill
                    String transJpql = "SELECT ft FROM FuelTransaction ft "
                            + "WHERE ft.paymentBill = :bill "
                            + "AND ft.retired = false";
                    Map<String, Object> transParams = new HashMap<>();
                    transParams.put("bill", bill);
                    List<FuelTransaction> transactions = fuelTransactionFacade.findByJpql(transJpql, transParams);

                    if (transactions == null || transactions.isEmpty()) {
                        // No transactions found, skip
                        migrationResults.setBillsSkipped(migrationResults.getBillsSkipped() + 1);
                        continue;
                    }

                    // Create BillItems for each transaction
                    for (FuelTransaction ft : transactions) {
                        BillItem billItem = new BillItem();
                        billItem.setBill(bill);
                        billItem.setFuelTransaction(ft);
                        billItem.setCreatedAt(new Date());
                        billItem.setCreatedBy(webUserController.getLoggedUser());
                        billItem.setRetired(false);
                        billItemFacade.create(billItem);

                        migrationResults.setBillItemsCreated(migrationResults.getBillItemsCreated() + 1);
                    }

                    migrationResults.setBillsMigrated(migrationResults.getBillsMigrated() + 1);

                } catch (Exception e) {
                    String error = "Error migrating bill " + bill.getBillNo() + ": " + e.getMessage();
                    migrationResults.addError(error);
                    Logger.getLogger(FuelRequestAndIssueController.class.getName()).log(Level.SEVERE, error, e);
                }
            }

            migrationResults.setEndTime(new Date());
            JsfUtil.addSuccessMessage("Migration completed. Bills migrated: " + migrationResults.getBillsMigrated()
                    + ", BillItems created: " + migrationResults.getBillItemsCreated());

        } catch (Exception e) {
            migrationResults.setEndTime(new Date());
            String error = "Fatal error during migration: " + e.getMessage();
            migrationResults.addError(error);
            JsfUtil.addErrorMessage(error);
            Logger.getLogger(FuelRequestAndIssueController.class.getName()).log(Level.SEVERE, error, e);
        }

        return null; // Stay on the same page to show results
    }

    public void listInstitutionRequests() {
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), null, null, null, null, null, fuelTransactionType);
    }

    boolean paymentRequestStarted = false;

    public String navigateToReviewPaymentRequest() {
        // Validate that from date and to date are in the same month
        if (fromDate != null && toDate != null) {
            java.util.Calendar fromCal = java.util.Calendar.getInstance();
            fromCal.setTime(fromDate);

            java.util.Calendar toCal = java.util.Calendar.getInstance();
            toCal.setTime(toDate);

            // Check if month and year are the same
            if (fromCal.get(java.util.Calendar.MONTH) != toCal.get(java.util.Calendar.MONTH)
                    || fromCal.get(java.util.Calendar.YEAR) != toCal.get(java.util.Calendar.YEAR)) {
                JsfUtil.addErrorMessage("From Date and To Date must be in the same month");
                return null;
            }
        }

        // Automatically select all transactions from the list
        if (transactions == null || transactions.isEmpty()) {
            JsfUtil.addErrorMessage("No transactions available to review");
            return null;
        }

        // Validate that all transactions have a fuel station
        for (FuelTransaction ft : transactions) {
            if (ft.getToInstitution() == null) {
                JsfUtil.addErrorMessage("All transactions must have a fuel station assigned. Please check transaction for vehicle: "
                        + (ft.getVehicle() != null ? ft.getVehicle().getVehicleNumber() : "Unknown"));
                return null;
            }
        }

        // Auto-select all listed transactions
        selectedTransactions = new ArrayList<>(transactions);
        Collections.sort(selectedTransactions, Comparator.comparing(FuelTransaction::getRequestedDate));
        return "/requests/review_payment?faces-redirect=true";
    }

    public void removeTransactionFromSelection(FuelTransaction transaction) {
        if (selectedTransactions != null && transaction != null) {
            selectedTransactions.remove(transaction);
            JsfUtil.addSuccessMessage("Transaction removed from selection");
        }
    }

    /**
     * Generates a unique monthly number for bills based on the month,
     * fromInstitution, and toInstitution. Format: YYYYMM-NNN where NNN is a
     * 3-digit sequential number Example: 202511-001, 202511-002, etc.
     *
     * @param fromInstitution The institution making the payment request
     * @param toInstitution The institution receiving the payment request
     * @param billDate The date of the bill (used to extract month and year)
     * @return A unique monthly number
     */
    private String generateMonthlyNumber(Institution fromInstitution, Institution toInstitution, Date billDate) {
        if (fromInstitution == null || toInstitution == null || billDate == null) {
            throw new IllegalArgumentException("fromInstitution, toInstitution, and billDate cannot be null");
        }

        // Extract year and month from bill date
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(billDate);
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        // Create month prefix (YYYYMM format)
        String monthPrefix = String.format("%04d%02d", year, month);

        // Calculate start and end of the month for the query
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        Date endOfMonth = cal.getTime();

        // Query to find the last bill for this month with this institution combination
        String jpql = "SELECT b FROM Bill b "
                + "WHERE b.fromInstitution = :fromInstitution "
                + "AND b.toInstitution = :toInstitution "
                + "AND b.billDate BETWEEN :startOfMonth AND :endOfMonth "
                + "AND b.retired = false "
                + "AND b.monthlyNumber LIKE :prefix "
                + "ORDER BY b.id DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("fromInstitution", fromInstitution);
        params.put("toInstitution", toInstitution);
        params.put("startOfMonth", startOfMonth);
        params.put("endOfMonth", endOfMonth);
        params.put("prefix", monthPrefix + "%");

        List<Bill> existingBills = billFacade.findByJpql(jpql, params, 1);

        int nextSequentialNumber = 1;

        if (existingBills != null && !existingBills.isEmpty()) {
            String lastMonthlyNumber = existingBills.get(0).getMonthlyNumber();

            if (lastMonthlyNumber != null) {
                // Extract the sequential number from the last monthly number
                // Format: YYYYMM-NNN (where NNN is the sequential number)
                try {
                    String[] parts = lastMonthlyNumber.split("-");
                    if (parts.length == 2) {
                        int lastNumber = Integer.parseInt(parts[1]);
                        nextSequentialNumber = lastNumber + 1;
                    }
                } catch (NumberFormatException e) {
                    // If parsing fails, start from 1
                    Logger.getLogger(FuelRequestAndIssueController.class.getName())
                            .log(Level.WARNING, "Could not parse sequential number from monthly number: " + lastMonthlyNumber, e);
                    nextSequentialNumber = 1;
                }
            }
        }

        // Format the sequential number with leading zeros (3 digits)
        String sequentialPart = String.format("%03d", nextSequentialNumber);

        // Generate the final monthly number
        String monthlyNumber = monthPrefix + "-" + sequentialPart;

        return monthlyNumber;
    }

    /**
     * Generates a unique bill number based on billType, fromInstitution, and
     * toInstitution. Format: [First 2 letters of billType][fromInstitution
     * code][toInstitution code][sequential number] Example: PAHSP001DEL001-0001
     *
     * @param billType The type of the bill
     * @param fromInstitution The institution making the payment request
     * @param toInstitution The institution receiving the payment request
     * @return A unique bill number
     */
    private String generateUniqueBillNumber(String billType, Institution fromInstitution, Institution toInstitution) {
        if (billType == null || fromInstitution == null || toInstitution == null) {
            throw new IllegalArgumentException("billType, fromInstitution, and toInstitution cannot be null");
        }

        if (fromInstitution.getCode() == null || fromInstitution.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("fromInstitution must have a valid code");
        }

        if (toInstitution.getCode() == null || toInstitution.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("toInstitution must have a valid code");
        }

        // Extract first two letters of bill type (convert to uppercase)
        String billTypePrefix = billType.length() >= 2
                ? billType.substring(0, 2).toUpperCase()
                : billType.toUpperCase();

        // Get institution codes (ensure they're uppercase for consistency)
        String fromCode = fromInstitution.getCode().toUpperCase().trim();
        String toCode = toInstitution.getCode().toUpperCase().trim();

        // Create the prefix for the bill number
        String billNumberPrefix = billTypePrefix + fromCode + toCode;

        // Query to find the last bill with this combination
        String jpql = "SELECT b FROM Bill b "
                + "WHERE b.billType = :billType "
                + "AND b.fromInstitution = :fromInstitution "
                + "AND b.toInstitution = :toInstitution "
                + "AND b.retired = false "
                + "AND b.billNo LIKE :prefix "
                + "ORDER BY b.id DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("billType", billType);
        params.put("fromInstitution", fromInstitution);
        params.put("toInstitution", toInstitution);
        params.put("prefix", billNumberPrefix + "%");

        List<Bill> existingBills = billFacade.findByJpql(jpql, params, 1);

        int nextSequentialNumber = 1;

        if (existingBills != null && !existingBills.isEmpty()) {
            String lastBillNo = existingBills.get(0).getBillNo();

            // Extract the sequential number from the last bill number
            // Format: PREFIX-NNNN (where NNNN is the sequential number)
            try {
                String[] parts = lastBillNo.split("-");
                if (parts.length == 2) {
                    int lastNumber = Integer.parseInt(parts[1]);
                    nextSequentialNumber = lastNumber + 1;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, start from 1
                Logger.getLogger(FuelRequestAndIssueController.class.getName())
                        .log(Level.WARNING, "Could not parse sequential number from bill: " + lastBillNo, e);
                nextSequentialNumber = 1;
            }
        }

        // Format the sequential number with leading zeros (4 digits)
        String sequentialPart = String.format("%04d", nextSequentialNumber);

        // Generate the final bill number
        String billNumber = billNumberPrefix + "-" + sequentialPart;

        return billNumber;
    }

    /**
     * Get the last fuel price entry for the given month and year
     *
     * @param billDate The date for which to find the fuel price
     * @return The last FuelPrice entry for that month, or null if not found
     */
    private FuelPrice getLastFuelPriceForMonth(Date billDate) {
        if (billDate == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(billDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        String jpql = "SELECT fp FROM FuelPrice fp "
                + "WHERE fp.year = :year AND fp.month = :month "
                + "AND fp.retired = false "
                + "ORDER BY fp.createdAt DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("month", month);

        List<FuelPrice> fuelPrices = fuelPriceFacade.findByJpql(jpql, params, 1);

        if (fuelPrices != null && !fuelPrices.isEmpty()) {
            return fuelPrices.get(0);
        }

        return null;
    }

    public String makePaymentRequest() {
        if (paymentRequestStarted) {
            JsfUtil.addErrorMessage("Already started");
            return null;
        }
        paymentRequestStarted = true;
        if (selectedTransactions == null || selectedTransactions.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            paymentRequestStarted = false;
            return null;
        }

        Institution hospital = null;
        Institution fuelStation = null;
        boolean firstTransaction = true;
        boolean moreThanOneCombinationOfHospitalAndFuelStation = false;
        boolean hasTrasnsactionNotYetMarkedAsIssued = false;

        for (FuelTransaction sft : selectedTransactions) {
            if (firstTransaction) {
                hospital = sft.getFromInstitution();
                fuelStation = sft.getToInstitution();
                firstTransaction = false;
            } else {
                if (!sft.getFromInstitution().equals(hospital) || !sft.getToInstitution().equals(fuelStation)) {
                    moreThanOneCombinationOfHospitalAndFuelStation = true;
                    break;
                }
            }
            if (!sft.isIssued()) {
                hasTrasnsactionNotYetMarkedAsIssued = true;
            }
        }

        if (moreThanOneCombinationOfHospitalAndFuelStation) {
            JsfUtil.addErrorMessage("You cannot add more than one fuel station and one hospital at a time for a bill");
            paymentRequestStarted = false;
            return null;
        }

        if (hasTrasnsactionNotYetMarkedAsIssued) {
            JsfUtil.addErrorMessage("You have transactions which are not yet marked as issued, Please remove them and retry");
            paymentRequestStarted = false;
            return null;
        }

        // Proceed to create a bill with the selected transactions if only one combination is found
        fuelPaymentRequestBill = new Bill();
        fuelPaymentRequestBill.setBillDate(new Date());
        fuelPaymentRequestBill.setBillTime(new Date());
        fuelPaymentRequestBill.setBillUser(webUserController.getLoggedUser());
        fuelPaymentRequestBill.setBillType("Payment Request From Hospital");
        fuelPaymentRequestBill.setFromInstitution(hospital);
        fuelPaymentRequestBill.setToInstitution(fuelStation);

        // Generate and set unique bill number
        try {
            String billNumber = generateUniqueBillNumber(
                    fuelPaymentRequestBill.getBillType(),
                    hospital,
                    fuelStation
            );
            fuelPaymentRequestBill.setBillNo(billNumber);
        } catch (Exception e) {
            Logger.getLogger(FuelRequestAndIssueController.class.getName())
                    .log(Level.SEVERE, "Error generating bill number", e);
            JsfUtil.addErrorMessage("Error generating bill number: " + e.getMessage());
            paymentRequestStarted = false;
            return null;
        }

        // Generate and set monthly number
        try {
            String monthlyNumber = generateMonthlyNumber(
                    hospital,
                    fuelStation,
                    fuelPaymentRequestBill.getBillDate()
            );
            fuelPaymentRequestBill.setMonthlyNumber(monthlyNumber);
        } catch (Exception e) {
            Logger.getLogger(FuelRequestAndIssueController.class.getName())
                    .log(Level.SEVERE, "Error generating monthly number", e);
            JsfUtil.addErrorMessage("Error generating monthly number: " + e.getMessage());
            paymentRequestStarted = false;
            return null;
        }

        // Fetch and set the last fuel price for the month
        try {
            FuelPrice lastFuelPrice = getLastFuelPriceForMonth(fuelPaymentRequestBill.getBillDate());
            if (lastFuelPrice != null) {
                fuelPaymentRequestBill.setFuelPrice(lastFuelPrice);
                Logger.getLogger(FuelRequestAndIssueController.class.getName())
                        .log(Level.INFO, "Set fuel price for bill: " + lastFuelPrice.getPrice());
            } else {
                Logger.getLogger(FuelRequestAndIssueController.class.getName())
                        .log(Level.WARNING, "No fuel price found for the bill month");
            }
        } catch (Exception e) {
            Logger.getLogger(FuelRequestAndIssueController.class.getName())
                    .log(Level.WARNING, "Error fetching fuel price for bill", e);
            // Continue without fuel price - this is not a critical error
        }

        billFacade.create(fuelPaymentRequestBill);

        double qtyRequested = 0.0;
        double qtyIssued = 0.0;

        for (FuelTransaction sft : selectedTransactions) {
            sft.setSubmittedToPayment(true);
            sft.setSubmittedToPaymentAt(new Date());
            sft.setSubmittedToPaymentBy(webUserController.getLoggedUser());
            sft.setPaymentBill(fuelPaymentRequestBill);
            if (sft.getRequestQuantity() != null) {
                qtyRequested += sft.getRequestQuantity();
            }
            if (sft.getIssuedQuantity() != null) {
                qtyIssued += sft.getIssuedQuantity();
            }
            fuelTransactionFacade.edit(sft);

            // Create BillItem to maintain bill-transaction relationship
            BillItem billItem = new BillItem();
            billItem.setBill(fuelPaymentRequestBill);
            billItem.setFuelTransaction(sft);
            billItem.setCreatedAt(new Date());
            billItem.setCreatedBy(webUserController.getLoggedUser());
            billItem.setRetired(false);
            billItemFacade.create(billItem);
        }

        fuelPaymentRequestBill.setTotalQty(qtyRequested);
        fuelPaymentRequestBill.setTotalIssuedQty(qtyIssued);
        billFacade.edit(fuelPaymentRequestBill);
        paymentRequestStarted = false;

        Collections.sort(selectedTransactions, Comparator.comparing(FuelTransaction::getRequestedDate));

        return "/requests/list_payment?faces-redirect=true";

    }

    public String viewPaymentRequest() {
        if (fuelPaymentRequestBill == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return null;
        }

        String jpql = "select ft "
                + " from FuelTransaction ft"
                + " where ft.paymentBill=:pb";

        Map m = new HashMap();
        m.put("pb", fuelPaymentRequestBill);

        selectedTransactions = getFacade().findByJpql(jpql, m);

        Collections.sort(selectedTransactions, Comparator.comparing(FuelTransaction::getRequestedDate));

        return "/requests/list_payment?faces-redirect=true";

    }

    public void listInstitutionRequestsToPay() {
        // Always filter by issued date for payment requests
        filterByIssuedDate = true;
        transactions
                = findFuelTransactions(
                        null, // institution
                        webUserController.getLoggedInstitution(), // fromInstitution
                        fuelStation, // toInstitution - use the selected fuel station filter
                        null, // vehicles
                        getFromDate(), // fromDateTime
                        getToDate(), // toDateTime
                        null, // issued
                        null, // cancelled
                        null, // rejected
                        false, // submittedToPayment, specifically asking for those not submitted
                        null, // txTypes
                        null // type
                );

        // Add any additional fuel stations from the search results to the dropdown
        if (transactions != null && !transactions.isEmpty()) {
            for (FuelTransaction ft : transactions) {
                if (ft.getToInstitution() != null) {
                    if (availableFuelStations == null) {
                        availableFuelStations = new ArrayList<>();
                    }
                    if (!availableFuelStations.contains(ft.getToInstitution())) {
                        availableFuelStations.add(ft.getToInstitution());
                    }
                }
            }
        }
    }

    public void listInstitutionRequestsPaid() {
        transactions
                = findFuelTransactions(
                        null, // institution
                        webUserController.getLoggedInstitution(), // fromInstitution
                        null, // toInstitution
                        null, // vehicles
                        getFromDate(), // fromDateTime
                        getToDate(), // toDateTime
                        null, // issued
                        null, // cancelled
                        null, // rejected
                        true, // submittedToPayment, specifically asking for those not submitted
                        null, // txTypes
                        null // type
                );
    }

    public void loadBillTransactions() {
        if (selectedBill == null) {
            selectedBillTransactions = new ArrayList<>();
            return;
        }

        // Query via BillItems to preserve history even if bill is rejected and paymentBill is cleared
        String jpql = "SELECT bi.fuelTransaction FROM BillItem bi "
                + "WHERE bi.bill = :bill "
                + "AND bi.retired = false "
                + "AND bi.fuelTransaction.retired = false "
                + "ORDER BY bi.fuelTransaction.requestedDate ASC";

        Map<String, Object> params = new HashMap<>();
        params.put("bill", selectedBill);

        selectedBillTransactions = fuelTransactionFacade.findByJpql(jpql, params);
    }

    public String getBillItemComment(Long transactionId) {
        if (selectedBill == null || transactionId == null) {
            return "";
        }

        String jpql = "SELECT bi FROM BillItem bi "
                + "WHERE bi.bill = :bill "
                + "AND bi.fuelTransaction.id = :transactionId "
                + "AND bi.retired = false";

        Map<String, Object> params = new HashMap<>();
        params.put("bill", selectedBill);
        params.put("transactionId", transactionId);

        try {
            List<BillItem> results = billItemFacade.findByJpql(jpql, params);
            if (results != null && !results.isEmpty()) {
                BillItem billItem = results.get(0);
                if (billItem != null && billItem.getComment() != null) {
                    return billItem.getComment();
                }
            }
        } catch (Exception e) {
            // Ignore errors, just return empty
        }
        return "";
    }

    public void listPendingPaymentRequests() {
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), null, null, null);
    }

    public void listInstitutionRequestsToMark() {
        filterByIssuedDate = false;
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), false, false, false);
    }

    public void listCtbFuelRequestsFromInstitution() {
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), null, null, null, null, FuelTransactionType.CtbFuelRequest);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected) {
        return findFuelTransactions(institution, fromInstitution, toInstitution, vehicles, fromDateTime, toDateTime, issued, cancelled, rejected, null);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            List<FuelTransactionType> txTypes) {
        // Call the new method with null for submittedToPayment and type
        return findFuelTransactions(institution, fromInstitution, toInstitution, vehicles, fromDateTime, toDateTime,
                issued, cancelled, rejected, null, txTypes, null);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            List<FuelTransactionType> txTypes,
            FuelTransactionType type) {
        // Call the new method with an additional 'null' parameter for submittedToPayment
        return findFuelTransactions(institution, fromInstitution, toInstitution, vehicles, fromDateTime, toDateTime,
                issued, cancelled, rejected, null, txTypes, type);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            Boolean submittedToPayment, // New boolean parameter
            List<FuelTransactionType> txTypes,
            FuelTransactionType type) {
        String j = "SELECT ft "
                + " FROM FuelTransaction ft "
                + " WHERE ft.retired = false";
        Map<String, Object> params = new HashMap<>();

        if (institution != null) {
            j += " AND ft.institution = :institution";
            params.put("institution", institution);
        }
        if (fromInstitution != null) {
            j += " AND ft.fromInstitution = :fromInstitution";
            params.put("fromInstitution", fromInstitution);
        }
        if (toInstitution != null) {
            j += " AND ft.toInstitution = :toInstitution";
            params.put("toInstitution", toInstitution);
        }
        if (vehicles != null && !vehicles.isEmpty()) {
            j += " AND ft.vehicle IN :vehicles";
            params.put("vehicles", vehicles);
        }
        if (fromDateTime != null) {
            if (filterByIssuedDate) {
                j += " AND ft.issuedDate >= :fromDateTime";
            } else {
                j += " AND ft.requestedDate >= :fromDateTime";
            }
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            if (filterByIssuedDate) {
                j += " AND ft.issuedDate <= :toDateTime";
            } else {
                j += " AND ft.requestedDate <= :toDateTime";
            }
            params.put("toDateTime", toDateTime);
        }
        if (issued != null) {
            j += " AND ft.issued = :issued";
            params.put("issued", issued);
        }
        if (cancelled != null) {
            j += " AND ft.cancelled = :cancelled";
            params.put("cancelled", cancelled);
        }
        if (rejected != null) {
            j += " AND ft.rejected = :rejected";
            params.put("rejected", rejected);
        }
        if (submittedToPayment != null) {  // New condition for submittedToPayment
            j += " AND ft.submittedToPayment = :submittedToPayment";
            params.put("submittedToPayment", submittedToPayment);
        }
        if (txTypes != null && !txTypes.isEmpty()) {
            j += " AND ft.transactionType IN :txTypes";
            params.put("txTypes", txTypes);
        }
        if (type != null) {
            j += " AND ft.transactionType = :ftxs";
            params.put("ftxs", type);
        }
        j += " order by ft.requestReferenceNumber ";
        List<FuelTransaction> fuelTransactions = getFacade().findByJpql(j, params);
        return fuelTransactions;
    }

    public String navigateToListInstitutionIssues() {
        return "/issues/list?faces-redirect=true";
    }

    public String navigateToIssueMultipleRequests() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/issues/issue_multiple?faces-redirect=true";
    }

    public String navigateToListToIssueRequestsForDepot() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/sltb/reports/list_to_issue_depot?faces-redirect=true";
    }

    public String navigateToListIssuedRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillIssuedRequestsFromDepotList();
        return "/sltb/reports/list_issued_from_depot?faces-redirect=true";
    }

    public String navigateToListRejectedIssueRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillRejectedIssueRequestsFromDepotList();
        return "/sltb/reports/list_rejected_issue_requests_from_depot?faces-redirect=true";
    }

    public FuelTransaction getSelected() {
        return selected;
    }

    public void setSelected(FuelTransaction selected) {
        this.selected = selected;
    }

    public FuelTransaction find(Object id) {
        return fuelTransactionFacade.find(id);
    }

    private FuelTransactionHistoryFacade getFacade() {
        return fuelTransactionHistoryFacade;
    }

    // Validation Methods
    private boolean isReferenceNumberUnique(String referenceNumber, Institution institution) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty() || institution == null) {
            return true;
        }

        // Calculate date 30 days ago
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();

        String jpql = "SELECT COUNT(ft) FROM FuelTransaction ft "
                + "WHERE ft.requestReferenceNumber = :refNum "
                + "AND ft.fromInstitution = :institution "
                + "AND ft.requestedDate >= :thirtyDaysAgo "
                + "AND ft.retired = false";

        Map<String, Object> params = new HashMap<>();
        params.put("refNum", referenceNumber.trim());
        params.put("institution", institution);
        params.put("thirtyDaysAgo", thirtyDaysAgo);

        Long count = fuelTransactionFacade.countByJpql(jpql, params);
        return count == 0;
    }

    private boolean areFieldValuesDistinct(String referenceNumber, Double odoReading, Double requestQuantity) {
        if (referenceNumber == null || odoReading == null || requestQuantity == null) {
            return true; // Skip validation if any value is null
        }

        try {
            Double refNumAsDouble = Double.parseDouble(referenceNumber.trim());
            // Check if reference number equals request quantity or ODO reading
            if (refNumAsDouble.equals(requestQuantity) || refNumAsDouble.equals(odoReading)) {
                return false;
            }
        } catch (NumberFormatException e) {
            // Reference number is not numeric, which is fine
        }

        // Check if ODO reading equals request quantity
        if (odoReading.equals(requestQuantity)) {
            return false;
        }

        return true;
    }

    private Double getPreviousOdoReading(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        if (vehicle.getId() == null) {
            return null;
        }

        try {
            String jpql = "SELECT ft FROM FuelTransaction ft "
                    + "WHERE ft.vehicle.id = :vehicleId "
                    + "AND ft.retired = false "
                    + "AND ft.odoMeterReading IS NOT NULL "
                    + "ORDER BY ft.requestedDate DESC";

            Map<String, Object> params = new HashMap<>();
            params.put("vehicleId", vehicle.getId());

            List<FuelTransaction> results = fuelTransactionFacade.findByJpql(jpql, params, 1);
            if (results != null && !results.isEmpty()) {
                return results.get(0).getOdoMeterReading();
            }
        } catch (Exception e) {
            Logger.getLogger(FuelRequestAndIssueController.class.getName()).log(Level.SEVERE, "Error getting previous ODO reading for vehicle: " + vehicle.getId(), e);
        }
        return null;
    }

    private boolean hasPendingRequest(Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }

        if (vehicle.getId() == null) {
            return false;
        }

        try {
            String jpql = "SELECT COUNT(ft) FROM FuelTransaction ft "
                    + "WHERE ft.vehicle.id = :vehicleId "
                    + "AND ft.issued = false "
                    + "AND ft.rejected = false "
                    + "AND ft.cancelled = false "
                    + "AND ft.retired = false";

            Map<String, Object> params = new HashMap<>();
            params.put("vehicleId", vehicle.getId());

            Long count = fuelTransactionFacade.countByJpql(jpql, params);
            return count != null && count > 0;
        } catch (Exception e) {
            Logger.getLogger(FuelRequestAndIssueController.class.getName()).log(Level.SEVERE, "Error checking pending request for vehicle: " + vehicle.getId(), e);
            return false;
        }
    }

    private boolean isRequestQuantityWithinCapacity(Vehicle vehicle, Double requestQuantity) {
        // Skip validation if vehicle or request quantity is null
        if (vehicle == null || requestQuantity == null) {
            return true;
        }

        // Skip validation if fuel capacity is not set for the vehicle
        if (vehicle.getFuelCapacity() == null) {
            return true;
        }

        // Check if request quantity exceeds fuel capacity
        return requestQuantity <= vehicle.getFuelCapacity();
    }

    private boolean areDatesInSameMonth(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            return false;
        }

        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(fromDate);

        Calendar calTo = Calendar.getInstance();
        calTo.setTime(toDate);

        return calFrom.get(Calendar.YEAR) == calTo.get(Calendar.YEAR)
                && calFrom.get(Calendar.MONTH) == calTo.get(Calendar.MONTH);
    }

    public void save(FuelTransaction saving) {
        if (saving == null) {
            return;
        }
        if (saving.getId() == null) {
            if (saving.getCreatedBy() == null) {
                saving.setCreatedAt(new Date());
            }
            if (saving.getCreatedBy() == null) {
                saving.setCreatedBy(webUserController.getLoggedUser());
            }
            fuelTransactionFacade.create(saving);
        } else {
            fuelTransactionFacade.edit(saving);
        }
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<FuelTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FuelTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<FuelTransaction> getSelectedTransactions() {
        return selectedTransactions;
    }

    public void setSelectedTransactions(List<FuelTransaction> selectedTransactions) {
        this.selectedTransactions = selectedTransactions;
    }

    public FuelTransactionHistory getSelectedTransactionHistory() {
        return selectedTransactionHistory;
    }

    public void setSelectedTransactionHistory(FuelTransactionHistory selectedTransactionHistory) {
        this.selectedTransactionHistory = selectedTransactionHistory;
    }

    public List<FuelTransactionHistory> getSelectedTransactionHistories() {
        return selectedTransactionHistories;
    }

    public void setSelectedTransactionHistories(List<FuelTransactionHistory> selectedTransactionHistories) {
        this.selectedTransactionHistories = selectedTransactionHistories;
    }

    public List<FuelTransactionHistory> getTransactionHistories() {
        return transactionHistories;
    }

    public void setTransactionHistories(List<FuelTransactionHistory> transactionHistories) {
        this.transactionHistories = transactionHistories;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
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
            toDate = CommonController.endOfTheMonth();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public FuelTransactionType getFuelTransactionType() {
        return fuelTransactionType;
    }

    public void setFuelTransactionType(FuelTransactionType fuelTransactionType) {
        this.fuelTransactionType = fuelTransactionType;
    }

    public boolean isFilterByIssuedDate() {
        return filterByIssuedDate;
    }

    public void setFilterByIssuedDate(boolean filterByIssuedDate) {
        this.filterByIssuedDate = filterByIssuedDate;
    }

    public String navigateToViewInstitutionFuelRequestToSltbDepot() {
        return "/requests/requested?faces-redirect=true";
    }

    public String navigateToViewDepotFuelRequestToCpc() {
        return "/moh/requested?faces-redirect=true";
    }

    public String getSearchingFuelRequestVehicleNumber() {
        return searchingFuelRequestVehicleNumber;
    }

    public void setSearchingFuelRequestVehicleNumber(String searchingFuelRequestVehicleNumber) {
        this.searchingFuelRequestVehicleNumber = searchingFuelRequestVehicleNumber;
    }

    public DataAlterationRequest getDataAlterationRequest() {
        return dataAlterationRequest;
    }

    public void setDataAlterationRequest(DataAlterationRequest dataAlterationRequest) {
        this.dataAlterationRequest = dataAlterationRequest;
    }

    public List<DataAlterationRequest> getDataAlterationRequests() {
        return dataAlterationRequests;
    }

    public void setDataAlterationRequests(List<DataAlterationRequest> dataAlterationRequests) {
        this.dataAlterationRequests = dataAlterationRequests;
    }

    public Bill getFuelPaymentRequestBill() {
        return fuelPaymentRequestBill;
    }

    public void setFuelPaymentRequestBill(Bill fuelPaymentRequestBill) {
        this.fuelPaymentRequestBill = fuelPaymentRequestBill;
    }

    public Bill getSelectedBill() {
        return selectedBill;
    }

    public void setSelectedBill(Bill selectedBill) {
        this.selectedBill = selectedBill;
    }

    public List<FuelTransaction> getSelectedBillTransactions() {
        return selectedBillTransactions;
    }

    public void setSelectedBillTransactions(List<FuelTransaction> selectedBillTransactions) {
        this.selectedBillTransactions = selectedBillTransactions;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public BillItemMigrationResults getMigrationResults() {
        return migrationResults;
    }

    public void setMigrationResults(BillItemMigrationResults migrationResults) {
        this.migrationResults = migrationResults;
    }

    public String getBillRejectionComment() {
        return billRejectionComment;
    }

    public void setBillRejectionComment(String billRejectionComment) {
        this.billRejectionComment = billRejectionComment;
    }

    public Map<Long, String> getBillItemComments() {
        if (billItemComments == null) {
            billItemComments = new HashMap<>();
        }
        return billItemComments;
    }

    public void setBillItemComments(Map<Long, String> billItemComments) {
        this.billItemComments = billItemComments;
    }

    public Institution getFuelStation() {
        return fuelStation;
    }

    public void setFuelStation(Institution fuelStation) {
        this.fuelStation = fuelStation;
    }

    public List<Institution> completeFromInstitutions(String query) {
        if (institutionController != null) {
            return institutionController.completeFuelRequestingFromInstitutions(query);
        }
        return new ArrayList<>();
    }

    public List<Institution> completeFuelStations(String query) {
        if (institutionController != null) {
            return institutionController.completeInstitutions(query);
        }
        return new ArrayList<>();
    }

    public List<Institution> getAvailableFuelStations() {
        if (availableFuelStations == null) {
            availableFuelStations = new ArrayList<>();
            Institution loggedInstitution = webUserController.getLoggedInstitution();
            if (loggedInstitution != null) {
                if (loggedInstitution.getSupplyInstitution() != null) {
                    availableFuelStations.add(loggedInstitution.getSupplyInstitution());
                }
                if (loggedInstitution.getAlternativeSupplyInstitution() != null
                        && !availableFuelStations.contains(loggedInstitution.getAlternativeSupplyInstitution())) {
                    availableFuelStations.add(loggedInstitution.getAlternativeSupplyInstitution());
                }
            }
        }
        return availableFuelStations;
    }

    public void setAvailableFuelStations(List<Institution> availableFuelStations) {
        this.availableFuelStations = availableFuelStations;
    }

    public String getBillNumberFilter() {
        return billNumberFilter;
    }

    public void setBillNumberFilter(String billNumberFilter) {
        this.billNumberFilter = billNumberFilter;
    }

    public Institution getFilterFromInstitution() {
        return filterFromInstitution;
    }

    public void setFilterFromInstitution(Institution filterFromInstitution) {
        this.filterFromInstitution = filterFromInstitution;
    }

    public Institution getFilterFuelStation() {
        return filterFuelStation;
    }

    public void setFilterFuelStation(Institution filterFuelStation) {
        this.filterFuelStation = filterFuelStation;
    }

    @FacesConverter(forClass = FuelTransaction.class)
    public static class FuelTransactionConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FuelRequestAndIssueController controller = (FuelRequestAndIssueController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "fuelRequestAndIssueController");
            return controller.find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof FuelTransaction) {
                FuelTransaction o = (FuelTransaction) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), FuelTransaction.class.getName()});
                return null;
            }
        }

    }

}
