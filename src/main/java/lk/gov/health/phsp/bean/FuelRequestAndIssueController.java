package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Calendar;
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
import lk.gov.health.phsp.entity.DataAlterationRequest;
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DataAlterationRequestType;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.facade.BillFacade;
import lk.gov.health.phsp.facade.DataAlterationRequestFacade;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.VehicleFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import org.primefaces.event.CaptureEvent;

@Named
@SessionScoped
public class FuelRequestAndIssueController implements Serializable {

    @EJB
    private FuelTransactionHistoryFacade fuelTransactionHistoryFacade;
    @EJB
    private FuelTransactionFacade fuelTransactionFacade;
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

    private DataAlterationRequest dataAlterationRequest;
    private List<DataAlterationRequest> dataAlterationRequests;

    private List<FuelTransaction> transactions = null;
    private List<Bill> bills;
    private List<FuelTransaction> selectedTransactions = null;
    private FuelTransaction selected;
    private String odoWarningMessage;
    private boolean odoWarningAcknowledged;
    private String issuedDateWarningMessage;
    private boolean issuedDateWarningAcknowledged;

    private FuelTransactionHistory selectedTransactionHistory;
    private List<FuelTransactionHistory> selectedTransactionHistories;
    private List<FuelTransactionHistory> transactionHistories;

    private Institution institution;
    private Institution fuelStation;
    private Institution selectedInstitution;
    private Vehicle vehicle;
    private WebUser webUser;
    private Date fromDate;
    private Date toDate;

    private Bill fuelPaymentRequestBill;

    private String searchingFuelRequestVehicleNumber;

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
        return "/issues/issue";
    }

    public String navigateToMarkVehicleFuelRequest() {
        issuedDateWarningMessage = null;
        issuedDateWarningAcknowledged = false;
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
        return "/issues/issued";
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
        return "/depot/receive";
    }

    public String navigateToSelectToIssueVehicleFuelRequest() {
        return "/issues/select_issue";
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
        if (!isDateNotInFuture(selected.getRequestedDate())) {
            JsfUtil.addErrorMessage("Requested Date cannot be a future date");
            return "";
        }
        if (selected.getToInstitution() == null) {
            JsfUtil.addErrorMessage("Select Fuel Station");
            return "";
        }
        if(selected.getRequestReferenceNumber()==null){
            JsfUtil.addErrorMessage("Enter a referance number");
            return "";
        }
        if(selected.getRequestReferenceNumber().trim().equals("")){
            JsfUtil.addErrorMessage("Enter a referance number");
            return "";
        }
        selected.setRequestReferenceNumber(selected.getRequestReferenceNumber().trim().toUpperCase());
        if (selected.getOdoMeterReading() == null) {
            JsfUtil.addErrorMessage("ODO Meter Reading is required");
            return "";
        }
        if (selected.getRequestQuantity() == null) {
            JsfUtil.addErrorMessage("Request Quantity is required");
            return "";
        }

        // Validation 1: Reference number must be unique within the same month for this institution
        if (!isReferenceNumberUnique(selected.getRequestReferenceNumber(), webUserController.getLoggedInstitution(), selected.getRequestedDate())) {
            JsfUtil.addErrorMessage("Reference number '" + selected.getRequestReferenceNumber() + "' has already been used this month for this institution");
            return "";
        }

        // Validation 2: Reference number, ODO meter and request quantity must be different numbers
        if (!areFieldValuesDistinct(selected.getRequestReferenceNumber(), selected.getOdoMeterReading(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Request Reference Number, ODO Meter Reading and Request Quantity must be different numbers");
            return "";
        }

        // Validation 3: ODO reading must be greater than the previous reading.
        // Not blocked outright - the user is warned and can confirm to proceed anyway.
        Double previousOdoReading = getPreviousOdoReading(selected.getVehicle());
        if (previousOdoReading != null && selected.getOdoMeterReading() != null
                && selected.getOdoMeterReading() <= previousOdoReading && !odoWarningAcknowledged) {
            odoWarningMessage = "ODO Meter Reading (" + selected.getOdoMeterReading() + ") is not greater than the previous reading (" + previousOdoReading + "). Do you want to continue anyway?";
            return "";
        }
        odoWarningMessage = null;

        // Validation 4: Request quantity must not exceed the vehicle type's maximum
        if (!isRequestQuantityWithinTypeLimit(selected.getVehicle(), selected.getRequestQuantity())) {
            VehicleType vt = selected.getVehicle().getVehicleType();
            JsfUtil.addErrorMessage("Requested quantity (" + selected.getRequestQuantity() + " liters) exceeds the maximum allowed for vehicle type " + vt.getLabel() + " (" + vt.getMaxRequestQuantity() + " liters)");
            return "";
        }

        selected.setRequestAt(new Date());
        save(selected);
        JsfUtil.addSuccessMessage("Fuel request submitted successfully. Reference number: " + selected.getRequestReferenceNumber());
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
        if (!isDateNotInFuture(selected.getRequestedDate())) {
            JsfUtil.addErrorMessage("Requested Date cannot be a future date");
            return "";
        }
        if (selected.getRequestReferenceNumber() == null || selected.getRequestReferenceNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("Enter a referance number");
            return "";
        }
        selected.setRequestReferenceNumber(selected.getRequestReferenceNumber().trim().toUpperCase());
        if (selected.getOdoMeterReading() == null) {
            JsfUtil.addErrorMessage("ODO Meter Reading is required");
            return "";
        }
        if (selected.getRequestQuantity() == null) {
            JsfUtil.addErrorMessage("Request Quantity is required");
            return "";
        }

        // Validation 1: Reference number must be unique within the same month for this institution
        if (!isReferenceNumberUnique(selected.getRequestReferenceNumber(), selected.getVehicle().getInstitution(), selected.getRequestedDate())) {
            JsfUtil.addErrorMessage("Reference number '" + selected.getRequestReferenceNumber() + "' has already been used this month for this institution");
            return "";
        }

        // Validation 2: Reference number, ODO meter and request quantity must be different numbers
        if (!areFieldValuesDistinct(selected.getRequestReferenceNumber(), selected.getOdoMeterReading(), selected.getRequestQuantity())) {
            JsfUtil.addErrorMessage("Request Reference Number, ODO Meter Reading and Request Quantity must be different numbers");
            return "";
        }

        // Validation 3: ODO reading must be greater than the previous reading.
        // Not blocked outright - the user is warned and can confirm to proceed anyway.
        Double previousOdoReading = getPreviousOdoReading(selected.getVehicle());
        if (previousOdoReading != null && selected.getOdoMeterReading() != null
                && selected.getOdoMeterReading() <= previousOdoReading && !odoWarningAcknowledged) {
            odoWarningMessage = "ODO Meter Reading (" + selected.getOdoMeterReading() + ") is not greater than the previous reading (" + previousOdoReading + "). Do you want to continue anyway?";
            return "";
        }
        odoWarningMessage = null;

        // Validation 4: Request quantity must not exceed the vehicle type's maximum
        if (!isRequestQuantityWithinTypeLimit(selected.getVehicle(), selected.getRequestQuantity())) {
            VehicleType vt = selected.getVehicle().getVehicleType();
            JsfUtil.addErrorMessage("Requested quantity (" + selected.getRequestQuantity() + " liters) exceeds the maximum allowed for vehicle type " + vt.getLabel() + " (" + vt.getMaxRequestQuantity() + " liters)");
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
        JsfUtil.addSuccessMessage("Special fuel request submitted successfully. Reference number: " + selected.getRequestReferenceNumber());
        return navigateToViewInstitutionFuelRequestToSltbDepot();
    }

    public String acknowledgeOdoWarningAndSubmitVehicleFuelRequest() {
        odoWarningAcknowledged = true;
        return submitVehicleFuelRequest();
    }

    public String acknowledgeOdoWarningAndSubmitSpecialVehicleFuelRequest() {
        odoWarningAcknowledged = true;
        return submitSpecialVehicleFuelRequest();
    }

    public String cancelOdoWarning() {
        odoWarningMessage = null;
        odoWarningAcknowledged = false;
        return "";
    }

    public String getOdoWarningMessage() {
        return odoWarningMessage;
    }

    public boolean isOdoWarningPending() {
        return odoWarningMessage != null;
    }

    public String acknowledgeIssuedDateWarningAndSubmitMark() {
        issuedDateWarningAcknowledged = true;
        return submitMarkVehicleFuelRequestIssue();
    }

    public String cancelIssuedDateWarning() {
        issuedDateWarningMessage = null;
        issuedDateWarningAcknowledged = false;
        return "";
    }

    public String getIssuedDateWarningMessage() {
        return issuedDateWarningMessage;
    }

    public boolean isIssuedDateWarningPending() {
        return issuedDateWarningMessage != null;
    }

    // ===== Fuel order validation helpers =====

    private boolean isReferenceNumberUnique(String referenceNumber, Institution institution, Date referenceDate) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty() || institution == null) {
            return true;
        }

        // Determine the calendar month of the order (start inclusive, next month start exclusive)
        Date basisDate = (referenceDate != null) ? referenceDate : new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(basisDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date nextMonthStart = cal.getTime();

        // Count orders with the same reference number in the same calendar month for this
        // institution, excluding retired (deleted) orders so a reference number freed up
        // by deleting a request can be reused.
        String jpql = "SELECT COUNT(ft) FROM FuelTransaction ft "
                + "WHERE ft.requestReferenceNumber = :refNum "
                + "AND ft.fromInstitution = :institution "
                + "AND ft.requestedDate >= :monthStart "
                + "AND ft.requestedDate < :nextMonthStart "
                + "AND ft.retired = false";

        Map<String, Object> params = new HashMap<>();
        params.put("refNum", referenceNumber.trim());
        params.put("institution", institution);
        params.put("monthStart", monthStart);
        params.put("nextMonthStart", nextMonthStart);

        Long count = fuelTransactionFacade.countByJpql(jpql, params);
        return count == null || count == 0;
    }

    private boolean isDateNotInFuture(Date date) {
        if (date == null) {
            return true;
        }

        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 23);
        endOfToday.set(Calendar.MINUTE, 59);
        endOfToday.set(Calendar.SECOND, 59);
        endOfToday.set(Calendar.MILLISECOND, 999);

        return !date.after(endOfToday.getTime());
    }

    private boolean isSameMonth(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return true;
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

    private long getDifferenceInDays(Date d1, Date d2) {
        long diffMillis = Math.abs(d2.getTime() - d1.getTime());
        return diffMillis / (1000 * 60 * 60 * 24);
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
        if (vehicle == null || vehicle.getId() == null) {
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

    private boolean isRequestQuantityWithinTypeLimit(Vehicle vehicle, Double requestQuantity) {
        // Skip validation if vehicle or request quantity is null
        if (vehicle == null || requestQuantity == null) {
            return true;
        }

        VehicleType vehicleType = vehicle.getVehicleType();

        // Skip validation if no type, or the type has no configured limit (null = no limit)
        if (vehicleType == null || vehicleType.getMaxRequestQuantity() == null) {
            return true;
        }

        // Check if request quantity exceeds the maximum allowed for this vehicle type
        return requestQuantity <= vehicleType.getMaxRequestQuantity();
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
        if (selected.getRequestedDate() != null && selected.getIssuedDate().before(selected.getRequestedDate())) {
            JsfUtil.addErrorMessage("Issued Date cannot be before the Requested Date");
            return "";
        }

        // Validation: Issued Date should not be far from the Requested Date.
        // Not blocked outright - the user is warned and can confirm to proceed anyway.
        if (selected.getRequestedDate() != null && !issuedDateWarningAcknowledged) {
            long daysDiff = getDifferenceInDays(selected.getIssuedDate(), selected.getRequestedDate());
            if (daysDiff > 3) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
                issuedDateWarningMessage = "The Issued Date and Requested Date differ by " + daysDiff
                        + " days. Requested At: " + sdf.format(selected.getRequestedDate())
                        + " — Issued Date: " + sdf.format(selected.getIssuedDate())
                        + ". Do you want to continue anyway?";
                return "";
            }
        }
        issuedDateWarningAcknowledged = false;

        if (selected.getIssueReferenceNumber() != null && selected.getRequestReferenceNumber() != null
                && selected.getIssueReferenceNumber().trim().equalsIgnoreCase(selected.getRequestReferenceNumber().trim())) {
            JsfUtil.addErrorMessage("Issue Reference Number (Invoice Number) cannot be the same as the Request Reference Number. They are two separate numbers.");
            return "";
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
        return "/depot/depot_receive_list";
    }

    public String navigateToSltbReportsFuelRequests() {
        return "/sltb/reports/requests";
    }

    public String navigateToSltbReportsFuelIssues() {
        return "/sltb/reports/issues";
    }

    public String navigateToSltbReportsFuelRejections() {
        return "/sltb/reports/rejections";
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
        return "/issues/list";
    }

    public String navigateToViewVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        return "/issues/requested";
    }

    public String navigateToAddVehicleFuelRequest() {
        odoWarningMessage = null;
        odoWarningAcknowledged = false;
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setToInstitution(webUserController.getLoggedInstitution().getSupplyInstitution());
        if (webUserController.getInstitutionVehicles().size() == 1) {
            selected.setVehicle(webUserController.getInstitutionVehicles().get(0));
        }
        if (webUserController.getInstitutionDrivers().size() == 1) {
            selected.setDriver(webUserController.getInstitutionDrivers().get(0));
        }
        return "/requests/request";
    }

    public String navigateToAddCpcFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.DepotFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(institutionApplicationController.findCpc());
        return "/moh/request";
    }

    public String navigateToViewDeleteRequestsToAttend() {
        dataAlterationRequests = findDataAlterationRequests(null, null, null, false, false, DataAlterationRequestType.DELETE_REQUEST);
        return "/national/admin/delete_requests_to_attend";
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
        return "/national/admin/delete_requests_to_attend";
    }

    public String navigateToAddSpecialVehicleFuelRequest() {
        odoWarningMessage = null;
        odoWarningAcknowledged = false;
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.SpecialVehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setToInstitution(webUserController.getLoggedInstitution().getSupplyInstitution());
        return "/requests/special_request";
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

    public String navigateToCreateNewDeleteRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a transaction");
            return null;
        }
        dataAlterationRequest = new DataAlterationRequest();
        dataAlterationRequest.setFuelTransaction(selected);
        dataAlterationRequest.setDataAlterationRequestType(DataAlterationRequestType.DELETE_REQUEST);
        return "/requests/requested_to_delete";
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
        return "/requests/requested_to_delete";
    }

    public String navigateToListInstitutionRequestsToMark() {
        listInstitutionRequestsToMark();
        return "/requests/list_to_mark";
    }

    public String navigateToListSltbRequestsFromCpc() {
        return "/moh/list";
    }

    public String onCaptureOfVehicleQr(CaptureEvent captureEvent) {
        byte[] imageData = captureEvent.getData();
        searchingFuelRequestVehicleNumber = qrCodeController.scanQRCode(imageData);
        return searchFuelRequestToIssueByVehicleNumber();
    }

    public void listPaymentBills() {
        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.fromInstitution IN :institutions "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        params.put("institutions", webUserController.findAutherizedInstitutions());
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        System.out.println("params = " + params);
        System.out.println("j = " + j);

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listPaymentBillsForInstitutionLevel() {
        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.fromInstitution IN :institutions "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        List<Institution> allowedInstitutions = webUserController.findAutherizedInstitutions();
        List<Institution> requestingInstitutions;
        if (selectedInstitution == null || !allowedInstitutions.contains(selectedInstitution)) {
            requestingInstitutions = allowedInstitutions;
        } else {
            requestingInstitutions = Arrays.asList(selectedInstitution);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("institutions", requestingInstitutions);
        if (fuelStation != null) {
            j += " AND b.toInstitution=:fs ";
            params.put("fs", fuelStation);
        }
        params.put("fromDate", fromDate); // fromDate should be set beforehand
        params.put("toDate", toDate);     // toDate should be set beforehand

        List<Bill> tmpBills = billFacade.findByJpql(j, params);
        bills = tmpBills;
    }

    public void listPaymentBillsForNationalLevel() {
        String j = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + " AND b.billDate BETWEEN :fromDate AND :toDate";

        Map<String, Object> params = new HashMap<>();
        if (institution != null) {
            j += " AND b.fromInstitution=:institution ";
            params.put("institution", institution);
        } else {
            j += " AND b.fromInstitution IN :institutions ";
            params.put("institutions", webUserController.findAutherizedInstitutions());
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

    public void listInstitutionRequests() {
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), null, null, null);
    }

    boolean paymentRequestStarted = false;
    private boolean paymentRequestReprint = false;

    public boolean isPaymentRequestReprint() {
        return paymentRequestReprint;
    }

    public String makePaymentRequest() {
        if (paymentRequestStarted) {
            JsfUtil.addErrorMessage("Already started");
            return null;
        }
        paymentRequestStarted = true;
        if (!isSameMonth(getFromDate(), getToDate())) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            paymentRequestStarted = false;
            return null;
        }
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
        billFacade.create(fuelPaymentRequestBill);

        double qty = 0.0;

        for (FuelTransaction sft : selectedTransactions) {
            sft.setSubmittedToPayment(true);
            sft.setSubmittedToPaymentAt(new Date());
            sft.setSubmittedToPaymentBy(webUserController.getLoggedUser());
            sft.setPaymentBill(fuelPaymentRequestBill);
            if (sft.getIssuedQuantity() != null) {
                qty += sft.getIssuedQuantity();
            }
            fuelTransactionFacade.edit(sft);
        }

        fuelPaymentRequestBill.setTotalQty(qty);
        billFacade.edit(fuelPaymentRequestBill);
        paymentRequestStarted = false;
        paymentRequestReprint = false;

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

        paymentRequestReprint = true;

        return "/requests/list_payment?faces-redirect=true";

    }

    public void listInstitutionRequestsToPay() {
        if (!isSameMonth(getFromDate(), getToDate())) {
            JsfUtil.addErrorMessage("From Date and To Date must be within the same month");
            return;
        }
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
                        false, // submittedToPayment, specifically asking for those not submitted
                        null, // txTypes
                        null, // type
                        true // filterByIssuedDate: From/To Date filters apply to the issued date, not the requested date
                );
        // Sort by issued date (most recently issued first), not by the ordered/requested date.
        // Not-yet-issued requests (null issued date) are listed last.
        if (transactions != null) {
            transactions.sort(Comparator.comparing(FuelTransaction::getIssuedDate,
                    Comparator.nullsLast(Comparator.reverseOrder())));
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

    public void listPendingPaymentRequests() {
        transactions = findFuelTransactions(null, webUserController.getLoggedInstitution(), null, null, getFromDate(), getToDate(), null, null, null);
    }

    public void listInstitutionRequestsToMark() {
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
            j += " AND ft.requestedDate >= :fromDateTime";
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            j += " AND ft.requestedDate <= :toDateTime";
            params.put("toDateTime", toDateTime);
        }
        if (issued != null) {
            j += " AND ft.issued = :issued ";
            params.put("issued", issued);
        }
        if (cancelled != null) {
            j += " AND ft.cancelled = :cancelled ";
            params.put("cancelled", cancelled);
        }
        if (rejected != null) {
            j += " AND ft.rejected = :rejected ";
            params.put("rejected", rejected);
        }
        if (txTypes != null) {
            j += " AND ft.transactionType in :ftxs ";
            params.put("ftxs", txTypes);
        }
        List<FuelTransaction> fuelTransactions = getFacade().findByJpql(j, params);
        if (fuelTransactions != null) {
        }
        return fuelTransactions;
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
        return findFuelTransactions(institution, fromInstitution, toInstitution, vehicles, fromDateTime, toDateTime,
                issued, cancelled, rejected, submittedToPayment, txTypes, type, false);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            Boolean submittedToPayment, // New boolean parameter
            List<FuelTransactionType> txTypes,
            FuelTransactionType type,
            boolean filterByIssuedDate) { // when true, fromDateTime/toDateTime filter on issuedDate instead of requestedDate
        String j = "SELECT ft "
                + " FROM FuelTransaction ft "
                + " WHERE ft.retired = false";
        Map<String, Object> params = new HashMap<>();
        String dateField = filterByIssuedDate ? "ft.issuedDate" : "ft.requestedDate";

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
            j += " AND " + dateField + " >= :fromDateTime";
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            j += " AND " + dateField + " <= :toDateTime";
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
        if (type != null) {
            j += " AND ft.transactionType = :ftxs";
            params.put("ftxs", type);
        }
        List<FuelTransaction> fuelTransactions = getFacade().findByJpql(j, params);
        return fuelTransactions;
    }

    public String navigateToListInstitutionIssues() {
        return "/issues/list";
    }

    public String navigateToIssueMultipleRequests() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/issues/issue_multiple";
    }

    public String navigateToListToIssueRequestsForDepot() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/sltb/reports/list_to_issue_depot";
    }

    public String navigateToListIssuedRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillIssuedRequestsFromDepotList();
        return "/sltb/reports/list_issued_from_depot";
    }

    public String navigateToListRejectedIssueRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillRejectedIssueRequestsFromDepotList();
        return "/sltb/reports/list_rejected_issue_requests_from_depot";
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

    public String navigateToViewInstitutionFuelRequestToSltbDepot() {
        return "/requests/requested";
    }

    public String navigateToViewDepotFuelRequestToCpc() {
        return "/moh/requested";
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

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public Institution getFuelStation() {
        return fuelStation;
    }

    public void setFuelStation(Institution fuelStation) {
        this.fuelStation = fuelStation;
    }

    public Institution getSelectedInstitution() {
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        this.selectedInstitution = selectedInstitution;
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
