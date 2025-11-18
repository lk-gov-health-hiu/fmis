package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.bean.util.JsfUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.CaptureEvent;

/**
 * Controller for managing fuel dispensing operations
 * Used by CPC Fuel Dispensors to mark transactions as dispensed
 *
 * @author Dr M H B Ariyaratne
 */
@Named
@SessionScoped
public class FuelDispenseController implements Serializable {

    @EJB
    private FuelTransactionFacade fuelTransactionFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private VehicleController vehicleController;
    @Inject
    private QRCodeController qrCodeController;

    private List<FuelTransaction> transactions;
    private FuelTransaction selected;
    private Date fromDate;
    private Date toDate;
    private String scannedVehicleNumber;

    public FuelDispenseController() {
    }

    /**
     * List all issued but not yet dispensed fuel transactions
     */
    public void listTransactionsToDispense() {
        if (fromDate == null) {
            fromDate = getDefaultFromDate();
        }
        if (toDate == null) {
            toDate = new Date();
        }

        String jpql = "SELECT f FROM FuelTransaction f "
                + "WHERE f.retired = false "
                + "AND f.cancelled = false "
                + "AND f.rejected = false "
                + "AND f.issued = true "
                + "AND f.dispensed = false "
                + "AND f.issuedInstitution = :institution "
                + "AND f.issuedDate BETWEEN :fromDate AND :toDate "
                + "ORDER BY f.issuedDate DESC";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("institution", webUserController.getLoggedInstitution());
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);

        transactions = fuelTransactionFacade.findByJpql(jpql, parameters, TemporalType.DATE);

        if (transactions == null) {
            transactions = new ArrayList<>();
        }
    }

    /**
     * Navigate to the list of transactions to dispense
     */
    public String navigateToListTransactionsToDispense() {
        listTransactionsToDispense();
        return "/cpc/fuel_dispense_list?faces-redirect=true";
    }

    /**
     * Navigate to the dispense page for marking a transaction as dispensed
     */
    public String navigateToDispenseFuel() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a transaction");
            return "";
        }
        // Initialize dispensed fields with default values
        if (selected.getDispensedQuantity() == null) {
            selected.setDispensedQuantity(selected.getIssuedQuantity());
        }
        if (selected.getDispensedDate() == null) {
            selected.setDispensedDate(new Date());
        }
        return "/cpc/fuel_dispense_mark?faces-redirect=true";
    }

    /**
     * Mark a fuel transaction as dispensed
     */
    public String markAsDispensed() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No transaction selected");
            return "";
        }

        // Validation
        if (selected.getDispensedQuantity() == null || selected.getDispensedQuantity() <= 0) {
            JsfUtil.addErrorMessage("Please enter a valid dispensed quantity");
            return "";
        }

        if (selected.getDispensedQuantity() > selected.getIssuedQuantity()) {
            JsfUtil.addErrorMessage("Dispensed quantity cannot exceed issued quantity");
            return "";
        }

        if (selected.getDispensedDate() == null) {
            JsfUtil.addErrorMessage("Please select a dispensed date");
            return "";
        }

        // Validate dispensed date is not before issued date
        if (selected.getIssuedDate() != null && selected.getDispensedDate() != null) {
            Calendar issuedCal = Calendar.getInstance();
            issuedCal.setTime(selected.getIssuedDate());
            issuedCal.set(Calendar.HOUR_OF_DAY, 0);
            issuedCal.set(Calendar.MINUTE, 0);
            issuedCal.set(Calendar.SECOND, 0);
            issuedCal.set(Calendar.MILLISECOND, 0);

            Calendar dispensedCal = Calendar.getInstance();
            dispensedCal.setTime(selected.getDispensedDate());
            dispensedCal.set(Calendar.HOUR_OF_DAY, 0);
            dispensedCal.set(Calendar.MINUTE, 0);
            dispensedCal.set(Calendar.SECOND, 0);
            dispensedCal.set(Calendar.MILLISECOND, 0);

            if (dispensedCal.before(issuedCal)) {
                JsfUtil.addErrorMessage("Dispensed date cannot be before issued date");
                return "";
            }
        }

        // Set dispensed details
        selected.setDispensed(true);
        selected.setDispensedAt(new Date());
        selected.setDispensedBy(webUserController.getLoggedUser());
        selected.setDispensedInstitution(webUserController.getLoggedInstitution());

        // Save the transaction
        fuelTransactionFacade.edit(selected);

        JsfUtil.addSuccessMessage("Fuel dispensed successfully");

        // Refresh the list and navigate back
        return navigateToListTransactionsToDispense();
    }

    /**
     * Navigate to QR code scanning page
     */
    public String navigateToQrScan() {
        scannedVehicleNumber = null;
        selected = null;
        return "/cpc/fuel_dispense_qr_scan?faces-redirect=true";
    }

    /**
     * Handle QR code capture event
     */
    public void onCaptureOfVehicleQr(CaptureEvent captureEvent) {
        byte[] imageData = captureEvent.getData();
        scannedVehicleNumber = qrCodeController.scanQRCode(imageData);

        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty() || scannedVehicleNumber.equals("Error")) {
            JsfUtil.addErrorMessage("QR Code scanning failed. Please try again.");
            scannedVehicleNumber = null;
        } else {
            JsfUtil.addSuccessMessage("QR Code scanned successfully: " + scannedVehicleNumber);
        }
    }

    /**
     * Search for fuel transaction by scanned vehicle QR code
     */
    public String searchFuelTransactionByVehicleQr() {
        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Vehicle QR code not scanned properly. Please try again.");
            return "";
        }

        // Search for the vehicle
        List<Vehicle> vehicles = vehicleController.searchVehicles(scannedVehicleNumber);
        if (vehicles == null || vehicles.isEmpty()) {
            JsfUtil.addErrorMessage("No vehicle found with number: " + scannedVehicleNumber);
            return "";
        }

        Vehicle vehicle = vehicles.get(0);

        // First, try to find transactions for this vehicle at the current institution
        String jpql = "SELECT f FROM FuelTransaction f "
                + "WHERE f.retired = false "
                + "AND f.cancelled = false "
                + "AND f.rejected = false "
                + "AND f.issued = true "
                + "AND f.dispensed = false "
                + "AND f.vehicle = :vehicle "
                + "AND f.issuedInstitution = :institution "
                + "ORDER BY f.issuedDate DESC, f.issuedAt DESC";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("vehicle", vehicle);
        parameters.put("institution", webUserController.getLoggedInstitution());

        List<FuelTransaction> results = fuelTransactionFacade.findByJpql(jpql, parameters);

        // If no results at this institution, try searching without institution filter
        if (results == null || results.isEmpty()) {
            String jpqlBroad = "SELECT f FROM FuelTransaction f "
                    + "WHERE f.retired = false "
                    + "AND f.cancelled = false "
                    + "AND f.rejected = false "
                    + "AND f.issued = true "
                    + "AND f.dispensed = false "
                    + "AND f.vehicle = :vehicle "
                    + "ORDER BY f.issuedDate DESC, f.issuedAt DESC";

            Map<String, Object> paramsBroad = new HashMap<>();
            paramsBroad.put("vehicle", vehicle);

            results = fuelTransactionFacade.findByJpql(jpqlBroad, paramsBroad);

            if (results == null || results.isEmpty()) {
                JsfUtil.addErrorMessage("No pending fuel transactions found for vehicle: " + scannedVehicleNumber +
                        ". Please ensure fuel has been issued for this vehicle.");
                return "";
            } else {
                // Found transactions but at a different institution
                JsfUtil.addErrorMessage("Found transaction at " +
                        (results.get(0).getIssuedInstitution() != null ? results.get(0).getIssuedInstitution().getName() : "another location") +
                        ". You can only dispense fuel issued at your institution: " +
                        webUserController.getLoggedInstitution().getName());
                return "";
            }
        }

        // Get the most recent transaction
        selected = results.get(0);

        // Initialize dispensed fields
        if (selected.getDispensedQuantity() == null) {
            selected.setDispensedQuantity(selected.getIssuedQuantity());
        }
        if (selected.getDispensedDate() == null) {
            selected.setDispensedDate(new Date());
        }

        JsfUtil.addSuccessMessage("Transaction found for vehicle: " + scannedVehicleNumber);

        // Navigate to the dispense page
        return "/cpc/fuel_dispense_mark?faces-redirect=true";
    }

    /**
     * Get default from date (beginning of current month)
     */
    private Date getDefaultFromDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Getters and Setters
    public List<FuelTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FuelTransaction> transactions) {
        this.transactions = transactions;
    }

    public FuelTransaction getSelected() {
        return selected;
    }

    public void setSelected(FuelTransaction selected) {
        this.selected = selected;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getDefaultFromDate();
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

    public String getScannedVehicleNumber() {
        return scannedVehicleNumber;
    }

    public void setScannedVehicleNumber(String scannedVehicleNumber) {
        this.scannedVehicleNumber = scannedVehicleNumber;
    }

    /**
     * Debug method to check all transactions for a vehicle
     */
    public void debugCheckTransactions() {
        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("No vehicle number scanned");
            return;
        }

        List<Vehicle> vehicles = vehicleController.searchVehicles(scannedVehicleNumber);
        if (vehicles == null || vehicles.isEmpty()) {
            JsfUtil.addErrorMessage("Vehicle not found: " + scannedVehicleNumber);
            return;
        }

        Vehicle vehicle = vehicles.get(0);

        // Check all transactions for this vehicle
        String jpql = "SELECT f FROM FuelTransaction f WHERE f.vehicle = :vehicle ORDER BY f.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("vehicle", vehicle);

        List<FuelTransaction> allTransactions = fuelTransactionFacade.findByJpql(jpql, params);

        if (allTransactions == null || allTransactions.isEmpty()) {
            JsfUtil.addErrorMessage("NO TRANSACTIONS FOUND AT ALL for vehicle: " + scannedVehicleNumber);
        } else {
            FuelTransaction latest = allTransactions.get(0);
            String msg = "Found " + allTransactions.size() + " total transactions. Latest: ID=" + latest.getId() +
                    ", Issued=" + latest.isIssued() +
                    ", Dispensed=" + latest.isDispensed() +
                    ", Cancelled=" + latest.isCancelled() +
                    ", Rejected=" + latest.isRejected() +
                    ", IssuedInst=" + (latest.getIssuedInstitution() != null ? latest.getIssuedInstitution().getName() : "null") +
                    ", CurrentInst=" + (webUserController.getLoggedInstitution() != null ? webUserController.getLoggedInstitution().getName() : "null");
            JsfUtil.addSuccessMessage(msg);
        }
    }
}
