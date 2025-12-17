package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

            // Try to parse as transaction ID first
            try {
                Long transactionId = Long.parseLong(qrData);
                FuelTransaction transaction = fuelTransactionFacade.find(transactionId);

                if (transaction != null && !transaction.isRetired()) {
                    response = buildTransactionResponse(transaction);
                    response.put("success", true);
                    response.put("type", "transaction");
                    return Response.ok(response).build();
                }
            } catch (NumberFormatException e) {
                // Not a transaction ID, continue to check other options
            }

            // Try to find vehicle by registration number
            String jpql = "SELECT v FROM Vehicle v WHERE v.vehicleNumber = :vehicleNumber AND v.retired = false";
            Map<String, Object> params = new HashMap<>();
            params.put("vehicleNumber", qrData);
            Vehicle vehicle = vehicleFacade.findFirstByJpql(jpql, params);

            if (vehicle != null) {
                response = buildVehicleResponse(vehicle);
                response.put("success", true);
                response.put("type", "vehicle");
                return Response.ok(response).build();
            }

            // Try to find vehicle by ID
            try {
                Long vehicleId = Long.parseLong(qrData);
                vehicle = vehicleFacade.find(vehicleId);

                if (vehicle != null && !vehicle.isRetired()) {
                    response = buildVehicleResponse(vehicle);
                    response.put("success", true);
                    response.put("type", "vehicle");
                    return Response.ok(response).build();
                }
            } catch (NumberFormatException e) {
                // Not a vehicle ID
            }

            // QR data not recognized
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
     * Mark fuel as dispensed
     * POST /api/qr/dispense
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

            if (!transaction.isIssued()) {
                response.put("success", false);
                response.put("message", "Transaction not yet issued");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Get dispensed quantity
            Object quantityObj = requestBody.get("dispensedQuantity");
            Double dispensedQuantity;
            if (quantityObj != null) {
                dispensedQuantity = ((Number) quantityObj).doubleValue();
            } else {
                // Default to issued quantity
                dispensedQuantity = transaction.getIssuedQuantity();
                if (dispensedQuantity == null) {
                    dispensedQuantity = transaction.getRequestQuantity();
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
            vehicleData.put("model", transaction.getVehicle().getModel());
            data.put("vehicle", vehicleData);
        }

        if (transaction.getToInstitution() != null) {
            Map<String, Object> institutionData = new HashMap<>();
            institutionData.put("id", transaction.getToInstitution().getId());
            institutionData.put("name", transaction.getToInstitution().getName());
            data.put("institution", institutionData);
        }

        return data;
    }

    private Map<String, Object> buildVehicleResponse(Vehicle vehicle) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", vehicle.getId());
        data.put("vehicleNumber", vehicle.getVehicleNumber());
        data.put("model", vehicle.getModel());
        data.put("makeYear", vehicle.getMakeYear());
        data.put("chasisNumber", vehicle.getChasisNumber());

        if (vehicle.getInstitution() != null) {
            Map<String, Object> institutionData = new HashMap<>();
            institutionData.put("id", vehicle.getInstitution().getId());
            institutionData.put("name", vehicle.getInstitution().getName());
            data.put("institution", institutionData);
        }

        return data;
    }
}
