package lk.gov.health.phsp.util;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;
import lk.gov.health.phsp.entity.WebUser;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for JWT token generation and validation
 * Used for mobile app authentication
 *
 * @author Dr M H B Ariyaratne
 */
public class JwtTokenUtil {

    // Secret key for signing tokens - CHANGE THIS IN PRODUCTION!
    private static final String SECRET_KEY = "fmis-mobile-app-secret-key-change-in-production-2025";

    // Token validity duration in hours
    private static final int TOKEN_VALIDITY_HOURS = 24;

    /**
     * Generate JWT token for authenticated user
     *
     * @param user The authenticated WebUser
     * @return JWT token string
     */
    public static String generateToken(WebUser user) {
        try {
            Signer signer = HMACSigner.newSHA256Signer(SECRET_KEY);

            JWT jwt = new JWT()
                    .setIssuer("fmis-server")
                    .setSubject(user.getName())
                    .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusHours(TOKEN_VALIDITY_HOURS));

            // Add custom claims
            jwt.addClaim("userId", user.getId());
            jwt.addClaim("userName", user.getName());
            jwt.addClaim("userRole", user.getWebUserRole().toString());

            if (user.getInstitution() != null) {
                jwt.addClaim("institutionId", user.getInstitution().getId());
                jwt.addClaim("institutionName", user.getInstitution().getName());
            }

            if (user.getPerson() != null && user.getPerson().getName() != null) {
                jwt.addClaim("personName", user.getPerson().getName());
            }

            // Sign and encode the JWT to a JSON string
            return JWT.getEncoder().encode(jwt, signer);

        } catch (Exception e) {
            System.out.println("Error generating JWT token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate JWT token and check if it's expired
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            Verifier verifier = HMACVerifier.newVerifier(SECRET_KEY);
            JWT jwt = JWT.getDecoder().decode(token, verifier);

            // Check if token is expired
            ZonedDateTime expiration = jwt.expiration;
            if (expiration != null && expiration.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from JWT token
     *
     * @param token JWT token string
     * @return User ID or null if extraction fails
     */
    public static Long getUserIdFromToken(String token) {
        try {
            Verifier verifier = HMACVerifier.newVerifier(SECRET_KEY);
            JWT jwt = JWT.getDecoder().decode(token, verifier);

            Object userIdObj = jwt.getAllClaims().get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error extracting user ID from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract username from JWT token
     *
     * @param token JWT token string
     * @return Username or null if extraction fails
     */
    public static String getUserNameFromToken(String token) {
        try {
            Verifier verifier = HMACVerifier.newVerifier(SECRET_KEY);
            JWT jwt = JWT.getDecoder().decode(token, verifier);
            return jwt.subject;
        } catch (Exception e) {
            System.out.println("Error extracting username from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract all user claims from JWT token
     *
     * @param token JWT token string
     * @return Map of claims or null if extraction fails
     */
    public static Map<String, Object> getAllClaimsFromToken(String token) {
        try {
            Verifier verifier = HMACVerifier.newVerifier(SECRET_KEY);
            JWT jwt = JWT.getDecoder().decode(token, verifier);
            return new HashMap<>(jwt.getAllClaims());
        } catch (Exception e) {
            System.out.println("Error extracting claims from token: " + e.getMessage());
            return null;
        }
    }
}
