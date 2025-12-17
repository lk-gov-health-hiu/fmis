# Backend API Development - Summary

**Date:** 2025-12-17
**Status:** ✅ Backend APIs Created Successfully

---

## Files Created

### 1. JWT Utility Class
**File:** `src/main/java/lk/gov/health/phsp/util/JwtTokenUtil.java`

**Purpose:** Generate and validate JWT tokens for mobile app authentication

**Key Methods:**
- `generateToken(WebUser user)` - Creates JWT token with 24-hour validity
- `validateToken(String token)` - Verifies token is valid and not expired
- `getUserIdFromToken(String token)` - Extracts user ID from token
- `getUserNameFromToken(String token)` - Extracts username from token

**Note:** Change the SECRET_KEY in production! Current key is for development only.

---

### 2. REST Authentication Controller
**File:** `src/main/java/lk/gov/health/phsp/bean/RestAuthenticationController.java`

**Purpose:** Handle mobile app login and authentication

**Endpoints:**

#### POST /api/auth/login
Login with username and password

**Request:**
```json
{
  "username": "your_username",
  "password": "your_password"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "user123",
    "role": "CPC_FUEL_DISPENSOR",
    "personName": "John Doe",
    "institution": {
      "id": 5,
      "name": "Central Fuel Station"
    }
  }
}
```

#### GET /api/auth/validate
Validate if token is still valid

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "valid": true,
  "user": { ... }
}
```

#### GET /api/auth/profile
Get detailed user profile

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "role": "CPC_FUEL_DISPENSOR",
    ...
  }
}
```

---

### 3. REST QR Scan Controller
**File:** `src/main/java/lk/gov/health/phsp/bean/RestQrScanController.java`

**Purpose:** Process QR code scans and handle fuel dispensing

**Endpoints:**

#### POST /api/qr/scan
Scan QR code and identify vehicle or transaction

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request:**
```json
{
  "qrData": "vehicle-number-GA1234"
  // or transaction ID like "12345"
}
```

**Response (Vehicle Found):**
```json
{
  "success": true,
  "type": "vehicle",
  "id": 10,
  "vehicleNumber": "GA-1234",
  "model": "Toyota Corolla",
  "institution": {
    "id": 5,
    "name": "Hospital A"
  }
}
```

**Response (Transaction Found):**
```json
{
  "success": true,
  "type": "transaction",
  "id": 12345,
  "requestReferenceNumber": "REQ-2025-001",
  "requestQuantity": 50.0,
  "issuedQuantity": 45.0,
  "issued": true,
  "dispensed": false,
  "vehicle": {
    "vehicleNumber": "GA-1234"
  }
}
```

#### POST /api/qr/dispense
Mark fuel as dispensed after QR scan

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request:**
```json
{
  "transactionId": 12345,
  "dispensedQuantity": 45.0,
  "comments": "Fuel dispensed successfully"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Fuel dispensed successfully",
  "transaction": { ... }
}
```

#### GET /api/qr/transaction/{id}
Get transaction details by ID

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "transaction": {
    "id": 12345,
    "requestQuantity": 50.0,
    "issuedQuantity": 45.0,
    "dispensed": false,
    ...
  }
}
```

---

### 4. CORS Filter
**File:** `src/main/java/lk/gov/health/phsp/filter/CorsFilter.java`

**Purpose:** Allow mobile app to make cross-origin API requests

**Configuration:**
- Allows requests from any origin (*)
- Supports GET, POST, PUT, DELETE, OPTIONS methods
- Allows Authorization header for JWT tokens

**Note:** In production, update `Access-Control-Allow-Origin` to specific domain for security.

---

### 5. Updated Files

#### pom.xml
Added dependency:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

#### web.xml
Added REST API servlet mapping:
```xml
<servlet>
    <servlet-name>REST API Servlet</servlet-name>
    <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
    <init-param>
        <param-name>jersey.config.server.provider.packages</param-name>
        <param-value>lk.gov.health.phsp.bean</param-value>
    </init-param>
    <load-on-startup>2</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>REST API Servlet</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
```

---

## Next Steps

### 1. Compile and Deploy Backend

#### Option A: Using NetBeans
1. Open project in NetBeans
2. Right-click project → Clean and Build
3. Deploy to your application server (GlassFish/Payara)

#### Option B: Using Maven Command Line
```bash
# Navigate to project directory
cd D:\Dev\fmis

# Clean and compile
mvn clean install

# Deploy the generated WAR file from target/fmis-0.1.war
```

### 2. Test APIs with Postman

After deployment, test the endpoints:

**Base URL:** `http://localhost:8080/fmis/api`
(Replace with your actual server address)

#### Test Login:
```
POST http://localhost:8080/fmis/api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

#### Test Token Validation:
```
GET http://localhost:8080/fmis/api/auth/validate
Authorization: Bearer <token-from-login-response>
```

#### Test QR Scan:
```
POST http://localhost:8080/fmis/api/qr/scan
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "qrData": "123"
}
```

### 3. Note Your Server Details

For Flutter app configuration, you'll need:
- Server IP address or domain
- Port number (usually 8080)
- Context path (usually /fmis)

Example API base URL:
- Local: `http://localhost:8080/fmis/api`
- Production: `http://your-server-ip:8080/fmis/api`

---

## API Authentication Flow

1. **Mobile app sends username/password** to `/api/auth/login`
2. **Backend validates credentials** against database
3. **Backend generates JWT token** (valid for 24 hours)
4. **Mobile app stores token** securely
5. **For subsequent requests**, mobile app sends token in Authorization header:
   ```
   Authorization: Bearer <jwt-token>
   ```
6. **Backend validates token** before processing request

---

## Security Notes

### Important for Production:

1. **Change JWT Secret Key:**
   - Edit `JwtTokenUtil.java` line 20
   - Use a strong, random secret key
   - Keep it confidential

2. **Update CORS Settings:**
   - Edit `CorsFilter.java` line 36
   - Change `Access-Control-Allow-Origin` from `*` to specific domain
   - Example: `https://your-mobile-app-domain.com`

3. **Use HTTPS:**
   - Configure SSL certificate on your server
   - Update mobile app to use `https://` URLs

4. **Token Expiry:**
   - Current: 24 hours
   - Adjust in `JwtTokenUtil.java` line 19 if needed

---

## Error Handling

All API responses include:
- `success` (boolean) - Indicates if request succeeded
- `message` (string) - Human-readable status message

HTTP Status Codes:
- `200` - Success
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (invalid/missing token)
- `403` - Forbidden (user not activated)
- `404` - Not Found (resource doesn't exist)
- `500` - Internal Server Error

---

## Troubleshooting

### Compilation Errors
- Ensure all dependencies are downloaded: `mvn clean install`
- Check Java version is 8 or higher
- Verify Maven is configured correctly

### API Not Responding
- Check server is running
- Verify deployment was successful
- Check server logs for errors
- Ensure firewall allows connections on port 8080

### Authentication Fails
- Verify username/password are correct
- Check user is activated in database
- Ensure user is not retired

### Token Validation Fails
- Token may be expired (24-hour limit)
- Check Authorization header format: `Bearer <token>`
- Verify token was copied correctly (no spaces)

---

## Contact

For issues or questions, refer to:
- Main plan: `MOBILE_APP_DEVELOPMENT_PLAN.md`
- Code comments in each Java file

---

**Ready for Flutter Development?**

Once backend is tested and working:
1. Complete Flutter SDK installation (see main plan)
2. Configure API base URL in Flutter app
3. Start Phase 3: Flutter App Development
