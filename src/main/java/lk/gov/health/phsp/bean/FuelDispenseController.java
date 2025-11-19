package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.FuelTransactionImage;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.FuelTransactionImageFacade;
import lk.gov.health.phsp.bean.util.JsfUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
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
import org.apache.commons.io.IOUtils;
import org.primefaces.event.CaptureEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

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
    @EJB
    private FuelTransactionImageFacade fuelTransactionImageFacade;

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
    private UploadedFile uploadedImage;  // For file upload
    private StreamedContent transactionImage;  // For displaying image
    private byte[] capturedImageData;  // For webcam captured image
    private String capturedImageBase64;  // For base64 encoded image from JavaScript

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
                + "ORDER BY f.id DESC";

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

        // Convert base64 image to byte array if provided
        if (capturedImageBase64 != null && !capturedImageBase64.trim().isEmpty()) {
            try {
                capturedImageData = Base64.getDecoder().decode(capturedImageBase64);
            } catch (Exception e) {
                System.err.println("Error decoding base64 image: " + e.getMessage());
                e.printStackTrace();
                capturedImageData = null;
            }
        }

        // Save captured image if available (optional)
        if (capturedImageData != null && capturedImageData.length > 0) {
            try {
                saveCapturedImage();
            } catch (Exception e) {
                // Log error but don't fail the transaction
                System.err.println("Error saving image: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Set dispensed details
        selected.setDispensed(true);
        selected.setDispensedAt(new Date());
        selected.setDispensedBy(webUserController.getLoggedUser());
        selected.setDispensedInstitution(webUserController.getLoggedInstitution());

        // Debug logging
        System.out.println("=== Marking transaction as dispensed ===");
        System.out.println("Transaction ID: " + selected.getId());
        System.out.println("Dispensed: " + selected.isDispensed());
        System.out.println("Dispensed At: " + selected.getDispensedAt());
        System.out.println("Dispensed By: " + (selected.getDispensedBy() != null ? selected.getDispensedBy().getWebUserPerson() : "null"));
        System.out.println("Dispensed Quantity: " + selected.getDispensedQuantity());
        System.out.println("Dispensed Date: " + selected.getDispensedDate());

        // Save the transaction and clear cache so other controllers can see the update
        try {
            fuelTransactionFacade.editAndClearCache(selected);
            System.out.println("Transaction saved successfully and cache cleared");
        } catch (Exception e) {
            System.err.println("ERROR saving transaction: " + e.getMessage());
            e.printStackTrace();
            JsfUtil.addErrorMessage("Error saving transaction: " + e.getMessage());
            return "";
        }

        // Clear image data
        capturedImageData = null;
        capturedImageBase64 = null;

        // Clear data and navigate to QR scan page
        // Note: Success message is not added here because redirect will clear it anyway
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

        // Don't show messages here - just redirect immediately if successful
        // Messages will be shown on the destination page if there are errors

        // Automatically search for the transaction
        String navigationOutcome = searchFuelTransactionByVehicleQr();

        if (navigationOutcome != null && !navigationOutcome.isEmpty()) {
            // Store the URL for JavaScript to navigate to
            FacesContext facesContext = FacesContext.getCurrentInstance();

            // Get various request details for debugging
            String contextPath = facesContext.getExternalContext().getRequestContextPath();
            String servletPath = facesContext.getExternalContext().getRequestServletPath();
            String requestURI = ((javax.servlet.http.HttpServletRequest) facesContext.getExternalContext().getRequest()).getRequestURI();

            System.out.println("=== URL Construction Debug ===");
            System.out.println("Context path: " + contextPath);
            System.out.println("Servlet path: " + servletPath);
            System.out.println("Request URI: " + requestURI);

            // Extract servlet mapping from request URI
            // Request URI format: /fmis/app/cpc/fuel_dispense_qr_scan.xhtml
            // We need: /fmis/app
            String baseURL = contextPath;
            if (requestURI != null && contextPath != null) {
                // Remove context path from request URI to get the relative path
                String relativePath = requestURI.substring(contextPath.length());
                System.out.println("Relative path: " + relativePath);

                // Extract first segment after context path (e.g., "/app" from "/app/cpc/...")
                if (relativePath.startsWith("/")) {
                    int secondSlash = relativePath.indexOf('/', 1);
                    if (secondSlash > 0) {
                        String servletMapping = relativePath.substring(0, secondSlash);
                        baseURL = contextPath + servletMapping;
                        System.out.println("Servlet mapping: " + servletMapping);
                    }
                }
            }

            // Remove ?faces-redirect=true and add .xhtml extension
            String page = navigationOutcome.replace("?faces-redirect=true", ".xhtml");

            // Construct full URL
            redirectUrl = baseURL + page;
            System.out.println("Base URL: " + baseURL);
            System.out.println("Page: " + page);
            System.out.println("Final navigation URL: " + redirectUrl);
            System.out.println("=== End URL Construction ===");
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
        // ORDER BY f.id DESC ensures we get the LATEST transaction (highest ID = most recent)
        String jpql = "SELECT f FROM FuelTransaction f "
                + "WHERE f.retired = false "
                + "AND f.cancelled = false "
                + "AND f.rejected = false "
                + "AND f.issued = true "
                + "AND f.dispensed = false "
                + "AND f.vehicle = :vehicle "
                + "AND f.issuedInstitution = :institution "
                + "ORDER BY f.id DESC";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("vehicle", vehicle);
        parameters.put("institution", webUserController.getLoggedInstitution());

        System.out.println("Current institution: " + (webUserController.getLoggedInstitution() != null ? webUserController.getLoggedInstitution().getName() : "null"));
        System.out.println("Searching with institution filter...");

        List<FuelTransaction> results = fuelTransactionFacade.findByJpql(jpql, parameters);
        System.out.println("Results with institution filter: " + (results != null ? results.size() : "null"));
        if (results != null && !results.isEmpty()) {
            System.out.println("First result (latest) ID: " + results.get(0).getId());
        }

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
                    + "ORDER BY f.id DESC";

            Map<String, Object> paramsBroad = new HashMap<>();
            paramsBroad.put("vehicle", vehicle);

            results = fuelTransactionFacade.findByJpql(jpqlBroad, paramsBroad);
            System.out.println("Results without institution filter: " + (results != null ? results.size() : "null"));
            if (results != null && !results.isEmpty()) {
                System.out.println("First result (latest) ID: " + results.get(0).getId());
            }

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

    /**
     * Upload image for the selected fuel transaction
     */
    public void uploadTransactionImage() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No transaction selected");
            return;
        }

        if (uploadedImage == null) {
            JsfUtil.addErrorMessage("Please select an image file");
            return;
        }

        try {
            // Validate file is an image
            String contentType = uploadedImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                JsfUtil.addErrorMessage("Please upload a valid image file");
                return;
            }

            // Check if image already exists for this transaction
            FuelTransactionImage existingImage = fuelTransactionImageFacade.findByFuelTransaction(selected);

            if (existingImage != null) {
                // Update existing image
                InputStream inputStream = uploadedImage.getInputStream();
                byte[] imageData = IOUtils.toByteArray(inputStream);

                existingImage.setImageData(imageData);
                existingImage.setFileName(uploadedImage.getFileName());
                existingImage.setContentType(contentType);
                existingImage.setFileSize((long) imageData.length);
                existingImage.setUploadedAt(new Date());
                existingImage.setUploadedBy(webUserController.getLoggedUser());

                fuelTransactionImageFacade.edit(existingImage);
                JsfUtil.addSuccessMessage("Image updated successfully");
            } else {
                // Create new image record
                FuelTransactionImage newImage = new FuelTransactionImage();
                newImage.setFuelTransaction(selected);

                InputStream inputStream = uploadedImage.getInputStream();
                byte[] imageData = IOUtils.toByteArray(inputStream);

                newImage.setImageData(imageData);
                newImage.setFileName(uploadedImage.getFileName());
                newImage.setContentType(contentType);
                newImage.setFileSize((long) imageData.length);
                newImage.setUploadedAt(new Date());
                newImage.setUploadedBy(webUserController.getLoggedUser());

                fuelTransactionImageFacade.create(newImage);
                JsfUtil.addSuccessMessage("Image uploaded successfully");
            }

            // Clear the uploaded file
            uploadedImage = null;

        } catch (IOException e) {
            JsfUtil.addErrorMessage("Error uploading image: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle webcam image capture event
     */
    public void onCaptureTransactionImage(CaptureEvent captureEvent) {
        if (selected == null) {
            JsfUtil.addErrorMessage("No transaction selected");
            return;
        }

        try {
            capturedImageData = captureEvent.getData();
            JsfUtil.addSuccessMessage("Image captured successfully. Click 'Confirm Dispense' to save.");
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error capturing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save the captured image to database
     */
    private void saveCapturedImage() throws Exception {
        if (selected == null || capturedImageData == null) {
            return;
        }

        // Check if image already exists for this transaction
        FuelTransactionImage existingImage = fuelTransactionImageFacade.findByFuelTransaction(selected);

        if (existingImage != null) {
            // Update existing image
            existingImage.setImageData(capturedImageData);
            existingImage.setFileName("dispense_image_" + selected.getId() + ".jpg");
            existingImage.setContentType("image/jpeg");
            existingImage.setFileSize((long) capturedImageData.length);
            existingImage.setUploadedAt(new Date());
            existingImage.setUploadedBy(webUserController.getLoggedUser());

            fuelTransactionImageFacade.edit(existingImage);
        } else {
            // Create new image record
            FuelTransactionImage newImage = new FuelTransactionImage();
            newImage.setFuelTransaction(selected);
            newImage.setImageData(capturedImageData);
            newImage.setFileName("dispense_image_" + selected.getId() + ".jpg");
            newImage.setContentType("image/jpeg");
            newImage.setFileSize((long) capturedImageData.length);
            newImage.setUploadedAt(new Date());
            newImage.setUploadedBy(webUserController.getLoggedUser());

            fuelTransactionImageFacade.create(newImage);
        }
    }

    /**
     * Clear captured image data
     */
    public void clearCapturedImage() {
        capturedImageData = null;
        capturedImageBase64 = null;
        JsfUtil.addSuccessMessage("Captured image cleared");
    }

    /**
     * Get the image for the selected fuel transaction
     */
    public StreamedContent getTransactionImage() {
        if (selected == null) {
            return getDefaultImage();
        }

        FuelTransactionImage image = fuelTransactionImageFacade.findByFuelTransaction(selected);
        if (image == null || image.getImageData() == null) {
            return getDefaultImage();
        }

        DefaultStreamedContent.Builder builder = DefaultStreamedContent.builder();
        return builder.stream(() -> new ByteArrayInputStream(image.getImageData()))
                .contentType(image.getContentType() != null ? image.getContentType() : "image/jpeg")
                .name(image.getFileName() != null ? image.getFileName() : "transaction_image.jpg")
                .build();
    }

    /**
     * Check if the selected transaction has an image
     */
    public boolean hasTransactionImage() {
        if (selected == null) {
            return false;
        }

        FuelTransactionImage image = fuelTransactionImageFacade.findByFuelTransaction(selected);
        return image != null && image.getImageData() != null;
    }

    /**
     * Delete the image for the selected fuel transaction
     */
    public void deleteTransactionImage() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No transaction selected");
            return;
        }

        FuelTransactionImage image = fuelTransactionImageFacade.findByFuelTransaction(selected);
        if (image != null) {
            fuelTransactionImageFacade.remove(image);
            JsfUtil.addSuccessMessage("Image deleted successfully");
        } else {
            JsfUtil.addErrorMessage("No image found for this transaction");
        }
    }

    /**
     * Get a default placeholder image
     */
    private StreamedContent getDefaultImage() {
        // Return an empty JPEG image as default
        DefaultStreamedContent.Builder builder = DefaultStreamedContent.builder();
        return builder.stream(() -> new ByteArrayInputStream(new byte[0]))
                .contentType("image/jpeg")
                .build();
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

    public UploadedFile getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(UploadedFile uploadedImage) {
        this.uploadedImage = uploadedImage;
    }

    public String getCapturedImageBase64() {
        return capturedImageBase64;
    }

    public void setCapturedImageBase64(String capturedImageBase64) {
        this.capturedImageBase64 = capturedImageBase64;
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
