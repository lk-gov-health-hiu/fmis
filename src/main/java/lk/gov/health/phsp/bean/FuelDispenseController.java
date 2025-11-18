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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
    private Long scannedVehicleId;
    private String redirectUrl;  // For automatic navigation after QR scan

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

        // Clear data and navigate to QR scan page
        return navigateToQrScan();
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
     * Navigate to home page
     */
    public String navigateToHome() {
        scannedVehicleNumber = null;
        selected = null;
        return "/index?faces-redirect=true";
    }

    /**
     * Handle QR code capture event
     */
    public void onCaptureOfVehicleQr(CaptureEvent captureEvent) {
        byte[] imageData = captureEvent.getData();
        String qrData = qrCodeController.scanQRCode(imageData);

        if (qrData == null || qrData.trim().isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "QR Code scanning failed", "No data received. Please try again.");
            scannedVehicleNumber = null;
        } else if (qrData.equals("QR_NOT_FOUND")) {
            addAjaxMessage(FacesMessage.SEVERITY_WARN, "QR Code not found", "No QR code detected in the image. Please position the QR code clearly in front of the camera and try again.");
            scannedVehicleNumber = null;
        } else if (qrData.equals("IMAGE_ERROR")) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Image error", "Failed to read camera image. Please try again.");
            scannedVehicleNumber = null;
        } else {
            // Parse JSON if the QR contains JSON data
            scannedVehicleNumber = parseVehicleNumberFromQr(qrData);
            scannedVehicleId = parseVehicleIdFromQr(qrData);

            if (scannedVehicleNumber != null && !scannedVehicleNumber.isEmpty()) {
                addAjaxMessage(FacesMessage.SEVERITY_INFO, "Success", "QR Code scanned successfully: " + scannedVehicleNumber +
                    (scannedVehicleId != null ? " (ID: " + scannedVehicleId + ")" : ""));
            } else {
                addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Parse Error", "Could not extract vehicle number from QR code: " + qrData);
                scannedVehicleNumber = null;
                scannedVehicleId = null;
            }
        }
    }

    /**
     * Parse vehicle number from QR code data (handles both plain text and JSON)
     */
    private String parseVehicleNumberFromQr(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            return null;
        }

        // Check if it's JSON (starts with { or [)
        String trimmed = qrData.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                // Try to parse as JSON and extract "number" or "vehicleNumber" field
                // Simple JSON parsing without external library
                String numberField = extractJsonField(trimmed, "number");
                if (numberField != null && !numberField.isEmpty()) {
                    return numberField;
                }

                numberField = extractJsonField(trimmed, "vehicleNumber");
                if (numberField != null && !numberField.isEmpty()) {
                    return numberField;
                }

                // If no specific field found, return the whole JSON
                // This might happen if the format is different
                return trimmed;
            } catch (Exception e) {
                // If JSON parsing fails, return as is
                return trimmed;
            }
        }

        // Not JSON, return as plain text
        return trimmed;
    }

    /**
     * Parse vehicle ID from QR code JSON
     */
    private Long parseVehicleIdFromQr(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            return null;
        }

        String trimmed = qrData.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                String idField = extractJsonField(trimmed, "id");
                if (idField != null && !idField.isEmpty()) {
                    return Long.parseLong(idField);
                }
            } catch (Exception e) {
                // If parsing fails, return null
            }
        }
        return null;
    }

    /**
     * Simple JSON field extractor (without external JSON library)
     */
    private String extractJsonField(String json, String fieldName) {
        // Try with quotes (for strings)
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        // Try without quotes (for numbers)
        pattern = "\"" + fieldName + "\"\\s*:\\s*([0-9]+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    /**
     * Add message for AJAX responses (doesn't use flash scope)
     */
    private void addAjaxMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    /**
     * Process automatically scanned QR code from JavaScript scanner
     * This method is called via AJAX when the continuous scanner detects a QR code
     */
    public void processScannedQr() {
        System.out.println("=== processScannedQr called ===");
        System.out.println("Scanned QR data: " + scannedVehicleNumber);

        // Reset redirect URL
        redirectUrl = null;

        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Error", "No QR code data received");
            return;
        }

        // Parse the QR code data (handles both plain text and JSON)
        String parsedNumber = parseVehicleNumberFromQr(scannedVehicleNumber);
        Long parsedId = parseVehicleIdFromQr(scannedVehicleNumber);

        if (parsedNumber == null || parsedNumber.isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Parse Error",
                "Could not extract vehicle number from QR code: " + scannedVehicleNumber);
            return;
        }

        // Update the scanned values with parsed data
        scannedVehicleNumber = parsedNumber;
        scannedVehicleId = parsedId;

        System.out.println("Parsed vehicle number: " + parsedNumber);
        if (parsedId != null) {
            System.out.println("Parsed vehicle ID: " + parsedId);
        }

        addAjaxMessage(FacesMessage.SEVERITY_INFO, "QR Detected",
            "Processing vehicle: " + parsedNumber);

        // Automatically search for the transaction
        String navigationOutcome = searchFuelTransactionByVehicleQr();

        if (navigationOutcome != null && !navigationOutcome.isEmpty()) {
            // Store the URL for JavaScript to navigate to
            // Convert JSF navigation outcome to actual URL with proper context path
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String contextPath = facesContext.getExternalContext().getRequestContextPath();

            // Remove ?faces-redirect=true and add .xhtml extension
            String page = navigationOutcome.replace("?faces-redirect=true", ".xhtml");

            // Construct full URL with context path
            redirectUrl = contextPath + page;
            System.out.println("Navigation URL set: " + redirectUrl);
        }
    }

    /**
     * Search for fuel transaction by scanned vehicle QR code
     */
    public String searchFuelTransactionByVehicleQr() {
        System.out.println("=== searchFuelTransactionByVehicleQr called ===");
        System.out.println("Scanned vehicle number: " + scannedVehicleNumber);

        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Vehicle QR code not scanned properly. Please try again.");
            System.out.println("ERROR: Empty vehicle number");
            return "";
        }

        // Search for the vehicle
        List<Vehicle> vehicles = vehicleController.searchVehicles(scannedVehicleNumber);
        System.out.println("Vehicles found: " + (vehicles != null ? vehicles.size() : "null"));

        if (vehicles == null || vehicles.isEmpty()) {
            JsfUtil.addErrorMessage("No vehicle found with number: " + scannedVehicleNumber);
            System.out.println("ERROR: No vehicle found");
            return "";
        }

        Vehicle vehicle = vehicles.get(0);
        System.out.println("Vehicle ID: " + vehicle.getId() + ", Number: " + vehicle.getVehicleNumber());

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

        System.out.println("Current institution: " + (webUserController.getLoggedInstitution() != null ? webUserController.getLoggedInstitution().getName() : "null"));
        System.out.println("Searching with institution filter...");

        List<FuelTransaction> results = fuelTransactionFacade.findByJpql(jpql, parameters);
        System.out.println("Results with institution filter: " + (results != null ? results.size() : "null"));

        // If no results at this institution, try searching without institution filter
        if (results == null || results.isEmpty()) {
            System.out.println("No results at this institution, searching all institutions...");
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
            System.out.println("Results without institution filter: " + (results != null ? results.size() : "null"));

            if (results == null || results.isEmpty()) {
                System.out.println("ERROR: No transactions found anywhere for this vehicle");
                JsfUtil.addErrorMessage("No pending fuel transactions found for vehicle: " + scannedVehicleNumber +
                        ". Please ensure fuel has been issued for this vehicle.");
                return "";
            } else {
                // Found transactions but at a different institution
                System.out.println("ERROR: Found " + results.size() + " transaction(s) at different institution");
                System.out.println("Transaction issued at: " + (results.get(0).getIssuedInstitution() != null ? results.get(0).getIssuedInstitution().getName() : "null"));
                JsfUtil.addErrorMessage("Found transaction at " +
                        (results.get(0).getIssuedInstitution() != null ? results.get(0).getIssuedInstitution().getName() : "another location") +
                        ". You can only dispense fuel issued at your institution: " +
                        webUserController.getLoggedInstitution().getName());
                return "";
            }
        }

        // Get the most recent transaction
        selected = results.get(0);
        System.out.println("SUCCESS: Transaction found! ID=" + selected.getId());

        // Initialize dispensed fields
        if (selected.getDispensedQuantity() == null) {
            selected.setDispensedQuantity(selected.getIssuedQuantity());
        }
        if (selected.getDispensedDate() == null) {
            selected.setDispensedDate(new Date());
        }

        JsfUtil.addSuccessMessage("Transaction found for vehicle: " + scannedVehicleNumber);

        System.out.println("Navigating to: /cpc/fuel_dispense_mark?faces-redirect=true");
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * Debug method to check all transactions for a vehicle
     */
    public void debugCheckTransactions() {
        if (scannedVehicleNumber == null || scannedVehicleNumber.trim().isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Debug", "No vehicle number scanned");
            return;
        }

        List<Vehicle> vehicles = vehicleController.searchVehicles(scannedVehicleNumber);
        if (vehicles == null || vehicles.isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Debug", "Vehicle not found: " + scannedVehicleNumber);
            return;
        }

        Vehicle vehicle = vehicles.get(0);

        // Check all transactions for this vehicle
        String jpql = "SELECT f FROM FuelTransaction f WHERE f.vehicle = :vehicle ORDER BY f.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("vehicle", vehicle);

        List<FuelTransaction> allTransactions = fuelTransactionFacade.findByJpql(jpql, params);

        if (allTransactions == null || allTransactions.isEmpty()) {
            addAjaxMessage(FacesMessage.SEVERITY_ERROR, "Debug", "NO TRANSACTIONS FOUND AT ALL for vehicle: " + scannedVehicleNumber);
        } else {
            FuelTransaction latest = allTransactions.get(0);
            String msg = "Found " + allTransactions.size() + " total transactions. Latest: ID=" + latest.getId() +
                    ", Issued=" + latest.isIssued() +
                    ", Dispensed=" + latest.isDispensed() +
                    ", Cancelled=" + latest.isCancelled() +
                    ", Rejected=" + latest.isRejected() +
                    ", IssuedInst=" + (latest.getIssuedInstitution() != null ? latest.getIssuedInstitution().getName() : "null") +
                    ", CurrentInst=" + (webUserController.getLoggedInstitution() != null ? webUserController.getLoggedInstitution().getName() : "null");
            addAjaxMessage(FacesMessage.SEVERITY_INFO, "Debug", msg);
        }
    }
}
