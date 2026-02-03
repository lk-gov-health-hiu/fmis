package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.FuelTransactionImage;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.FuelTransactionImageFacade;
import lk.gov.health.phsp.facade.VehicleFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.util.JwtTokenUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

/**
 * REST API Controller for QR code scanning operations
 * Handles fuel dispensing via QR codes from mobile app
 *
 * @author Dr M H B Ariyaratne
 */
@Named
@RequestScoped
@Path("/qr")
public class RestQrScanController {

    @EJB
    private FuelTransactionFacade fuelTransactionFacade;

    @EJB
    private FuelTransactionImageFacade fuelTransactionImageFacade;

    @EJB
    private VehicleFacade vehicleFacade;

    @EJB
    private WebUserFacade webUserFacade;

    @Inject
    private CommonController commonController;

    /**
     * Process QR code scan from mobile app
     * POST /api/qr/scan
     *
     * Header: Authorization: Bearer <token>
     * Request body: {"qrData": "vehicle-id-123" or "transaction-id-456"}
     * Response: {"success": true, "type": "vehicle", "data": {...}}
     */
    @POST
    @Path("/scan")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response scanQrCode(@HeaderParam("Authorization") String authHeader, Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Debug: Log request details
            System.out.println("=== QR SCAN REQUEST ===");
            System.out.println("Request Body: " + requestBody);
            System.out.println("Request Body Keys: " + (requestBody != null ? requestBody.keySet() : "null"));

            // Validate authentication
            WebUser currentUser = authenticateUser(authHeader);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Get QR data
            String qrData = requestBody.get("qrData");
            if (qrData == null || qrData.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "QR data is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            qrData = qrData.trim();

            // Debug: Log the received QR data
            System.out.println("=== QR SCAN DEBUG ===");
            System.out.println("Received QR Data: [" + qrData + "]");
            System.out.println("QR Data Length: " + qrData.length());
            System.out.println("User ID: " + currentUser.getId());
            System.out.println("User Name: " + (currentUser.getPerson() != null ? currentUser.getPerson().getName() : "N/A"));

            // Check if QR data is JSON format (starts with '{' and ends with '}')
            if (qrData.startsWith("{") && qrData.endsWith("}")) {
                System.out.println("QR data appears to be JSON format, attempting to parse...");
                try {
                    // Parse JSON to extract id or vehicleNumber
                    // Simple JSON parsing without external libraries
                    String vehicleId = extractJsonValue(qrData, "id");
                    String vehicleNumber = extractJsonValue(qrData, "vehicleNumber");

                    System.out.println("Extracted from JSON - ID: [" + vehicleId + "], Vehicle Number: [" + vehicleNumber + "]");

                    // Try using the extracted ID first
                    if (vehicleId != null && !vehicleId.isEmpty()) {
                        System.out.println("Attempting to find vehicle with extracted ID: " + vehicleId);
                        try {
                            Long vehicleIdNum = Long.parseLong(vehicleId);
                            Vehicle vehicle = vehicleFacade.find(vehicleIdNum);

                            if (vehicle != null && !vehicle.isRetired()) {
                                System.out.println("Vehicle found using JSON ID - ID: " + vehicle.getId() + ", Number: " + vehicle.getVehicleNumber());
                                response = buildVehicleResponseWithTransactions(vehicle);
                                response.put("success", true);
                                response.put("type", "vehicle");
                                return Response.ok(response).build();
                            } else if (vehicle != null) {
                                System.out.println("Vehicle found but is retired");
                            } else {
                                System.out.println("No vehicle found with extracted ID: " + vehicleIdNum);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Extracted ID is not numeric: " + vehicleId);
                        }
                    }

                    // Try using the extracted vehicle number
                    if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
                        System.out.println("Attempting to find vehicle with extracted vehicle number: [" + vehicleNumber + "]");
                        String jpql = "SELECT v FROM Vehicle v WHERE v.vehicleNumber = :vehicleNumber AND v.retired = false";
                        Map<String, Object> params = new HashMap<>();
                        params.put("vehicleNumber", vehicleNumber);
                        Vehicle vehicle = vehicleFacade.findFirstByJpql(jpql, params);

                        if (vehicle != null) {
                            System.out.println("Vehicle found using JSON vehicle number - ID: " + vehicle.getId() + ", Number: " + vehicle.getVehicleNumber());
                            response = buildVehicleResponseWithTransactions(vehicle);
                            response.put("success", true);
                            response.put("type", "vehicle");
                            return Response.ok(response).build();
                        } else {
                            System.out.println("No vehicle found with extracted vehicle number: [" + vehicleNumber + "]");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing JSON QR data: " + e.getMessage());
                }
            }

            // Try to parse as transaction ID first
            try {
                Long transactionId = Long.parseLong(qrData);
                System.out.println("Attempting to find transaction with ID: " + transactionId);
                FuelTransaction transaction = fuelTransactionFacade.find(transactionId);

                if (transaction != null) {
                    System.out.println("Transaction found - ID: " + transaction.getId() + ", Retired: " + transaction.isRetired());
                    if (!transaction.isRetired()) {
                        System.out.println("Transaction matched successfully");
                        response = buildTransactionResponse(transaction);
                        response.put("success", true);
                        response.put("type", "transaction");
                        return Response.ok(response).build();
                    } else {
                        System.out.println("Transaction found but is retired");
                    }
                } else {
                    System.out.println("No transaction found with ID: " + transactionId);
                }
            } catch (NumberFormatException e) {
                System.out.println("QR data is not a numeric transaction ID: " + e.getMessage());
            }

            // Try to find vehicle by registration number
            System.out.println("Attempting to find vehicle by registration number: [" + qrData + "]");
            String jpql = "SELECT v FROM Vehicle v WHERE v.vehicleNumber = :vehicleNumber AND v.retired = false";
            Map<String, Object> params = new HashMap<>();
            params.put("vehicleNumber", qrData);
            Vehicle vehicle = vehicleFacade.findFirstByJpql(jpql, params);

            if (vehicle != null) {
                System.out.println("Vehicle found by registration number - ID: " + vehicle.getId() + ", Number: " + vehicle.getVehicleNumber());
                response = buildVehicleResponseWithTransactions(vehicle);
                response.put("success", true);
                response.put("type", "vehicle");
                return Response.ok(response).build();
            } else {
                System.out.println("No vehicle found with registration number: [" + qrData + "]");
            }

            // Try to find vehicle by ID
            try {
                Long vehicleId = Long.parseLong(qrData);
                System.out.println("Attempting to find vehicle with ID: " + vehicleId);
                vehicle = vehicleFacade.find(vehicleId);

                if (vehicle != null) {
                    System.out.println("Vehicle found - ID: " + vehicle.getId() + ", Retired: " + vehicle.isRetired());
                    if (!vehicle.isRetired()) {
                        System.out.println("Vehicle matched successfully");
                        response = buildVehicleResponseWithTransactions(vehicle);
                        response.put("success", true);
                        response.put("type", "vehicle");
                        return Response.ok(response).build();
                    } else {
                        System.out.println("Vehicle found but is retired");
                    }
                } else {
                    System.out.println("No vehicle found with ID: " + vehicleId);
                }
            } catch (NumberFormatException e) {
                System.out.println("QR data is not a numeric vehicle ID: " + e.getMessage());
            }

            // QR data not recognized
            System.out.println("QR code not recognized - no matches found for: [" + qrData + "]");
            System.out.println("=== END QR SCAN DEBUG ===");
            response.put("success", false);
            response.put("message", "QR code not recognized. Please scan a valid fuel transaction or vehicle QR code.");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();

        } catch (Exception e) {
            System.out.println("QR scan error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred while processing QR code");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    /**
     * Mark fuel as dispensed (WITHOUT image)
     * POST /api/qr/dispense
     *
     * NOTE: This endpoint is kept for backward compatibility.
     * For new implementations, use /dispense-with-image which requires invoice image upload.
     *
     * Header: Authorization: Bearer <token>
     * Request body: {"transactionId": 123, "dispensedQuantity": 50.0, "comments": "..."}
     * Response: {"success": true, "message": "Fuel dispensed successfully"}
     */
    @POST
    @Path("/dispense")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dispenseFuel(@HeaderParam("Authorization") String authHeader, Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate authentication
            WebUser currentUser = authenticateUser(authHeader);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Get transaction ID
            Object transactionIdObj = requestBody.get("transactionId");
            if (transactionIdObj == null) {
                response.put("success", false);
                response.put("message", "Transaction ID is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            Long transactionId = ((Number) transactionIdObj).longValue();
            FuelTransaction transaction = fuelTransactionFacade.find(transactionId);

            if (transaction == null) {
                response.put("success", false);
                response.put("message", "Transaction not found");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }

            // Validate transaction state
            if (transaction.isRetired()) {
                response.put("success", false);
                response.put("message", "Transaction is retired");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isCancelled()) {
                response.put("success", false);
                response.put("message", "Transaction is cancelled");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isRejected()) {
                response.put("success", false);
                response.put("message", "Transaction is rejected");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isDispensed()) {
                response.put("success", false);
                response.put("message", "Transaction already dispensed");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Note: Dispensing happens BEFORE issuing in this workflow
            // Issuing is the final confirmation step after dispensing

            // Get dispensed quantity
            Object quantityObj = requestBody.get("dispensedQuantity");
            Double dispensedQuantity;
            if (quantityObj != null) {
                dispensedQuantity = ((Number) quantityObj).doubleValue();
            } else {
                // Default to request quantity (since transaction may not be issued yet)
                dispensedQuantity = transaction.getRequestQuantity();
                if (dispensedQuantity == null || dispensedQuantity <= 0) {
                    // Fallback to issued quantity if request quantity is not available
                    dispensedQuantity = transaction.getIssuedQuantity();
                }
            }

            if (dispensedQuantity == null || dispensedQuantity <= 0) {
                response.put("success", false);
                response.put("message", "Invalid dispensed quantity");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Update transaction
            transaction.setDispensed(true);
            transaction.setDispensedAt(new Date());
            transaction.setDispensedDate(new Date());
            transaction.setDispensedBy(currentUser);
            transaction.setDispensedInstitution(currentUser.getInstitution());
            transaction.setDispensedQuantity(dispensedQuantity);

            String comments = (String) requestBody.get("comments");
            if (comments != null && !comments.trim().isEmpty()) {
                transaction.setDispensedComments(comments);
            }

            // Save transaction
            fuelTransactionFacade.edit(transaction);

            // Build success response
            response.put("success", true);
            response.put("message", "Fuel dispensed successfully");
            response.put("transaction", buildTransactionResponse(transaction));

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Dispense fuel error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred while dispensing fuel");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    /**
     * Mark fuel as dispensed with invoice image upload
     * POST /api/qr/dispense-with-image
     *
     * Header: Authorization: Bearer <token>
     * Content-Type: multipart/form-data
     * Form fields:
     *   - transactionId: Long (required)
     *   - dispensedQuantity: Double (required)
     *   - comments: String (optional)
     *   - invoiceImage: File (required - JPEG/PNG)
     * Response: {"success": true, "message": "...", "transaction": {...}}
     */
    @POST
    @Path("/dispense-with-image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dispenseFuelWithImage(
            @HeaderParam("Authorization") String authHeader,
            @FormDataParam("transactionId") Long transactionId,
            @FormDataParam("dispensedQuantity") Double dispensedQuantity,
            @FormDataParam("comments") String comments,
            @FormDataParam("invoiceImage") InputStream fileInputStream,
            @FormDataParam("invoiceImage") FormDataContentDisposition fileDetail) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== DISPENSE WITH IMAGE REQUEST ===");
            System.out.println("Transaction ID: " + transactionId);
            System.out.println("Dispensed Quantity: " + dispensedQuantity);
            System.out.println("Comments: " + (comments != null ? comments : "none"));

            // Validate authentication
            WebUser currentUser = authenticateUser(authHeader);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            System.out.println("User: " + currentUser.getId() + " - " + (currentUser.getPerson() != null ? currentUser.getPerson().getName() : "N/A"));

            // Validate transaction ID
            if (transactionId == null) {
                response.put("success", false);
                response.put("message", "Transaction ID is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Validate dispensed quantity
            if (dispensedQuantity == null || dispensedQuantity <= 0) {
                response.put("success", false);
                response.put("message", "Valid dispensed quantity is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Validate invoice image
            if (fileInputStream == null || fileDetail == null) {
                response.put("success", false);
                response.put("message", "Invoice image is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            String fileName = fileDetail.getFileName();
            String contentType = fileDetail.getType();
            long fileSize = fileDetail.getSize();

            System.out.println("Image: " + fileName + " (" + fileSize + " bytes, " + contentType + ")");

            // Validate image format
            if (contentType == null || (!contentType.toLowerCase().contains("image/jpeg")
                    && !contentType.toLowerCase().contains("image/jpg")
                    && !contentType.toLowerCase().contains("image/png"))) {
                response.put("success", false);
                response.put("message", "Invalid image format. Only JPEG and PNG are allowed");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Find transaction
            FuelTransaction transaction = fuelTransactionFacade.find(transactionId);
            if (transaction == null) {
                response.put("success", false);
                response.put("message", "Transaction not found");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }

            // Validate transaction state
            if (transaction.isRetired()) {
                response.put("success", false);
                response.put("message", "Transaction is retired");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isCancelled()) {
                response.put("success", false);
                response.put("message", "Transaction is cancelled");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isRejected()) {
                response.put("success", false);
                response.put("message", "Transaction is rejected");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (transaction.isDispensed()) {
                response.put("success", false);
                response.put("message", "Transaction already dispensed");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Read image bytes
            byte[] imageBytes = fileInputStream.readAllBytes();
            System.out.println("Image bytes read: " + imageBytes.length);

            // Update transaction
            transaction.setDispensed(true);
            transaction.setDispensedAt(new Date());
            transaction.setDispensedDate(new Date());
            transaction.setDispensedBy(currentUser);
            transaction.setDispensedInstitution(currentUser.getInstitution());
            transaction.setDispensedQuantity(dispensedQuantity);

            if (comments != null && !comments.trim().isEmpty()) {
                transaction.setDispensedComments(comments);
            }

            // Save transaction
            fuelTransactionFacade.edit(transaction);
            System.out.println("Transaction updated - ID: " + transaction.getId());

            // Save or update image
            FuelTransactionImage existingImage = fuelTransactionImageFacade.findByFuelTransaction(transaction);
            FuelTransactionImage image;

            if (existingImage != null) {
                // Update existing image
                System.out.println("Updating existing image - ID: " + existingImage.getId());
                image = existingImage;
            } else {
                // Create new image
                System.out.println("Creating new image record");
                image = new FuelTransactionImage();
                image.setFuelTransaction(transaction);
            }

            image.setImageData(imageBytes);
            image.setFileName(fileName);
            image.setContentType(contentType);
            image.setFileSize((long) imageBytes.length);
            image.setUploadedAt(new Date());
            image.setUploadedBy(currentUser);

            if (existingImage != null) {
                fuelTransactionImageFacade.edit(image);
            } else {
                fuelTransactionImageFacade.create(image);
            }

            System.out.println("Image saved successfully");
            System.out.println("=== DISPENSE WITH IMAGE SUCCESS ===");

            // Build success response
            response.put("success", true);
            response.put("message", "Fuel dispensed successfully with invoice image");
            response.put("transaction", buildTransactionResponse(transaction));

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Dispense with image error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred while dispensing fuel: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    /**
     * Get fuel transaction details by ID
     * GET /api/qr/transaction/{id}
     *
     * Header: Authorization: Bearer <token>
     * Response: {"success": true, "transaction": {...}}
     */
    @GET
    @Path("/transaction/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long transactionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate authentication
            WebUser currentUser = authenticateUser(authHeader);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            FuelTransaction transaction = fuelTransactionFacade.find(transactionId);

            if (transaction == null) {
                response.put("success", false);
                response.put("message", "Transaction not found");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }

            response.put("success", true);
            response.put("transaction", buildTransactionResponse(transaction));

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Get transaction error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred while fetching transaction");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    // Helper methods

    /**
     * Simple JSON value extractor without external libraries
     * Extracts value for a given key from a JSON string
     */
    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        // Look for "key":"value" pattern
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        // Also try without quotes around value (for numbers): "key":value
        pattern = "\"" + key + "\"\\s*:\\s*([^,}\\s]+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private WebUser authenticateUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (!JwtTokenUtil.validateToken(token)) {
            return null;
        }

        Long userId = JwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }

        WebUser user = webUserFacade.find(userId);
        if (user == null || user.isRetired()) {
            return null;
        }

        return user;
    }

    private Map<String, Object> buildTransactionResponse(FuelTransaction transaction) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", transaction.getId());
        data.put("requestReferenceNumber", transaction.getRequestReferenceNumber());
        data.put("issueReferenceNumber", transaction.getIssueReferenceNumber());
        data.put("requestQuantity", transaction.getRequestQuantity());
        data.put("issuedQuantity", transaction.getIssuedQuantity());
        data.put("dispensedQuantity", transaction.getDispensedQuantity());
        data.put("issued", transaction.isIssued());
        data.put("dispensed", transaction.isDispensed());
        data.put("cancelled", transaction.isCancelled());
        data.put("rejected", transaction.isRejected());
        data.put("requestedDate", transaction.getRequestedDate());
        data.put("issuedDate", transaction.getIssuedDate());
        data.put("dispensedDate", transaction.getDispensedDate());
        data.put("comments", transaction.getComments());

        if (transaction.getVehicle() != null) {
            Map<String, Object> vehicleData = new HashMap<>();
            vehicleData.put("id", transaction.getVehicle().getId());
            vehicleData.put("vehicleNumber", transaction.getVehicle().getVehicleNumber());
            vehicleData.put("model", transaction.getVehicle().getVehicleModel());
            data.put("vehicle", vehicleData);
        }

        if (transaction.getToInstitution() != null) {
            Map<String, Object> institutionData = new HashMap<>();
            institutionData.put("id", transaction.getToInstitution().getId());
            institutionData.put("name", transaction.getToInstitution().getName());
            data.put("institution", institutionData);
        }

        if (transaction.getRequestedInstitution() != null) {
            Map<String, Object> requestedInstitutionData = new HashMap<>();
            requestedInstitutionData.put("id", transaction.getRequestedInstitution().getId());
            requestedInstitutionData.put("name", transaction.getRequestedInstitution().getName());
            data.put("requestedInstitution", requestedInstitutionData);
        }

        return data;
    }

    private Map<String, Object> buildVehicleResponse(Vehicle vehicle) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", vehicle.getId());
        data.put("vehicleNumber", vehicle.getVehicleNumber());
        data.put("model", vehicle.getVehicleModel());
        data.put("make", vehicle.getVehicleMake());
        data.put("chassisNumber", vehicle.getChassisNumber());

        if (vehicle.getInstitution() != null) {
            Map<String, Object> institutionData = new HashMap<>();
            institutionData.put("id", vehicle.getInstitution().getId());
            institutionData.put("name", vehicle.getInstitution().getName());
            data.put("institution", institutionData);
        }

        return data;
    }

    private Map<String, Object> buildVehicleResponseWithTransactions(Vehicle vehicle) {
        Map<String, Object> data = buildVehicleResponse(vehicle);

        // Find pending transactions for this vehicle (not yet dispensed)
        System.out.println("Looking up pending transactions for vehicle ID: " + vehicle.getId());
        String jpql = "SELECT t FROM FuelTransaction t WHERE t.vehicle.id = :vehicleId "
                + "AND t.retired = false "
                + "AND t.dispensed = false "
                + "AND t.cancelled = false "
                + "AND t.rejected = false "
                + "ORDER BY t.requestedDate DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicle.getId());

        try {
            java.util.List<FuelTransaction> transactions = fuelTransactionFacade.findByJpql(jpql, params);
            System.out.println("Found " + (transactions != null ? transactions.size() : 0) + " pending transactions");

            if (transactions != null && !transactions.isEmpty()) {
                java.util.List<Map<String, Object>> transactionList = new java.util.ArrayList<>();
                for (FuelTransaction transaction : transactions) {
                    transactionList.add(buildTransactionResponse(transaction));
                }
                data.put("pendingTransactions", transactionList);
            } else {
                data.put("pendingTransactions", new java.util.ArrayList<>());
            }
        } catch (Exception e) {
            System.out.println("Error fetching pending transactions: " + e.getMessage());
            e.printStackTrace();
            data.put("pendingTransactions", new java.util.ArrayList<>());
        }

        return data;
    }
}
