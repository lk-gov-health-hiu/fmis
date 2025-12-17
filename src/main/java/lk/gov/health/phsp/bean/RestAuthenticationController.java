package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.util.JwtTokenUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for mobile app authentication
 * Provides endpoints for login, token validation
 *
 * @author Dr M H B Ariyaratne
 */
@Named
@RequestScoped
@Path("/auth")
public class RestAuthenticationController {

    @EJB
    private WebUserFacade webUserFacade;

    @Inject
    private CommonController commonController;

    /**
     * Login endpoint for mobile app
     * POST /api/auth/login
     *
     * Request body: {"username": "user", "password": "pass"}
     * Response: {"success": true, "token": "jwt-token", "user": {...}}
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }

            // Find user by username
            String jpql = "SELECT u FROM WebUser u WHERE LOWER(u.name) = :userName AND u.retired = :ret";
            Map<String, Object> params = new HashMap<>();
            params.put("userName", username.trim().toLowerCase());
            params.put("ret", false);

            WebUser user = webUserFacade.findFirstByJpql(jpql, params);

            if (user == null) {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Check if user is activated
            if (!user.isActivated()) {
                response.put("success", false);
                response.put("message", "User account is not activated");
                return Response.status(Response.Status.FORBIDDEN).entity(response).build();
            }

            // Verify password
            if (!commonController.matchPassword(password, user.getWebUserPassword())) {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Generate JWT token
            String token = JwtTokenUtil.generateToken(user);

            if (token == null) {
                response.put("success", false);
                response.put("message", "Failed to generate authentication token");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
            }

            // Build user info response
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getName());
            userInfo.put("role", user.getWebUserRole() != null ? user.getWebUserRole().toString() : null);

            if (user.getPerson() != null) {
                userInfo.put("personName", user.getPerson().getName());
            }

            if (user.getInstitution() != null) {
                Map<String, Object> institutionInfo = new HashMap<>();
                institutionInfo.put("id", user.getInstitution().getId());
                institutionInfo.put("name", user.getInstitution().getName());
                userInfo.put("institution", institutionInfo);
            }

            // Success response
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", userInfo);

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred during login");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    /**
     * Validate token endpoint
     * GET /api/auth/validate
     *
     * Header: Authorization: Bearer <token>
     * Response: {"valid": true, "user": {...}}
     */
    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("valid", false);
                response.put("message", "Missing or invalid Authorization header");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // Validate token
            if (!JwtTokenUtil.validateToken(token)) {
                response.put("valid", false);
                response.put("message", "Invalid or expired token");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Extract user info from token
            Long userId = JwtTokenUtil.getUserIdFromToken(token);
            Map<String, Object> claims = JwtTokenUtil.getAllClaimsFromToken(token);

            if (userId == null) {
                response.put("valid", false);
                response.put("message", "Invalid token data");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Get fresh user data from database
            WebUser user = webUserFacade.find(userId);

            if (user == null || user.isRetired()) {
                response.put("valid", false);
                response.put("message", "User not found or inactive");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Build user info
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getName());
            userInfo.put("role", user.getWebUserRole() != null ? user.getWebUserRole().toString() : null);

            if (user.getPerson() != null) {
                userInfo.put("personName", user.getPerson().getName());
            }

            if (user.getInstitution() != null) {
                Map<String, Object> institutionInfo = new HashMap<>();
                institutionInfo.put("id", user.getInstitution().getId());
                institutionInfo.put("name", user.getInstitution().getName());
                userInfo.put("institution", institutionInfo);
            }

            response.put("valid", true);
            response.put("user", userInfo);

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            e.printStackTrace();

            response.put("valid", false);
            response.put("message", "An error occurred during validation");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    /**
     * Get user profile endpoint
     * GET /api/auth/profile
     *
     * Header: Authorization: Bearer <token>
     * Response: {"success": true, "user": {...}}
     */
    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@HeaderParam("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract and validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Missing or invalid Authorization header");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            String token = authHeader.substring(7);

            if (!JwtTokenUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            Long userId = JwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid token data");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Get user from database
            WebUser user = webUserFacade.find(userId);

            if (user == null || user.isRetired()) {
                response.put("success", false);
                response.put("message", "User not found or inactive");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }

            // Build detailed user profile
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.getId());
            userProfile.put("username", user.getName());
            userProfile.put("email", user.getEmail());
            userProfile.put("telNo", user.getTelNo());
            userProfile.put("role", user.getWebUserRole() != null ? user.getWebUserRole().toString() : null);
            userProfile.put("activated", user.isActivated());

            if (user.getPerson() != null) {
                Map<String, Object> personInfo = new HashMap<>();
                personInfo.put("name", user.getPerson().getName());
                personInfo.put("phone", user.getPerson().getPhone1());
                userProfile.put("person", personInfo);
            }

            if (user.getInstitution() != null) {
                Map<String, Object> institutionInfo = new HashMap<>();
                institutionInfo.put("id", user.getInstitution().getId());
                institutionInfo.put("name", user.getInstitution().getName());
                institutionInfo.put("code", user.getInstitution().getCode());
                userProfile.put("institution", institutionInfo);
            }

            response.put("success", true);
            response.put("user", userProfile);

            return Response.ok(response).build();

        } catch (Exception e) {
            System.out.println("Get profile error: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "An error occurred while fetching profile");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }
}
