# Claude Development Notes

## QR Scanning Debug Logs - 2025-12-31

### Issue
Mobile app QR scanning returns error: "QR code not recognized. Please scan a valid fuel transaction or a vehicle QR code."

### Changes Made
Added comprehensive debug logging to `RestQrScanController.java` to diagnose QR scanning issues.

**File Modified:** `src/main/java/lk/gov/health/phsp/bean/RestQrScanController.java`

### Debug Logs Added

1. **Request Level Logging** (lines 62-64):
   - Logs the entire request body
   - Shows all request body keys received from mobile app

2. **QR Data Processing Logging** (lines 85-89):
   - Logs the exact QR data received (with brackets to show whitespace)
   - Shows QR data length
   - Logs user ID and name performing the scan

3. **Transaction ID Lookup Logging** (lines 88-104):
   - Attempts to parse QR data as transaction ID
   - Logs if transaction is found and its retired status
   - Logs if no transaction found with that ID

4. **Vehicle Registration Number Lookup Logging** (lines 110-124):
   - Attempts to find vehicle by registration number
   - Logs if vehicle is found with its details
   - Logs if no vehicle found with that registration number

5. **Vehicle ID Lookup Logging** (lines 128-148):
   - Attempts to parse QR data as vehicle ID
   - Logs if vehicle is found and its retired status
   - Logs if no vehicle found with that ID

6. **Final Failure Logging** (lines 151-152):
   - Logs when QR code is not recognized
   - Shows the exact QR data that failed to match

### How to Use These Logs

When the mobile app scans a QR code and gets the error, check the server logs for:
- `=== QR SCAN REQUEST ===` - Shows what the mobile app sent
- `=== QR SCAN DEBUG ===` - Shows the detailed matching attempts
- `=== END QR SCAN DEBUG ===` - Marks the end of the debug section

The logs will reveal:
1. What data format the mobile app is sending
2. Whether the QR data matches any transaction ID, vehicle registration, or vehicle ID
3. If matches are found but marked as retired
4. The exact point where the matching logic fails

### Root Cause Found
The mobile app sends QR data in JSON format:
```json
{"id":"99241","vehicleNumber":"104154","institutionName":"TH Karapitiya"}
```

But the web app expected a plain string (just "99241" or "104154").

### Solution Implemented
Added JSON parsing logic to `RestQrScanController.java`:

1. **JSON Detection** (lines 91-146): Detects if `qrData` is JSON format
2. **Value Extraction** (lines 97-98): Extracts `id` and `vehicleNumber` from JSON
3. **Vehicle Lookup by ID** (lines 103-123): Tries to find vehicle using extracted ID
4. **Vehicle Lookup by Number** (lines 126-142): Tries to find vehicle using extracted vehicle number
5. **Helper Method** (lines 405-429): Simple regex-based JSON parser to avoid external dependencies

The fix makes the web app **backward compatible** - it still accepts plain string QR codes while also supporting the JSON format from the mobile app.

### Compilation Fix
Fixed compilation error: Changed `getWebUserPerson()` to `getPerson()` on line 89.

### Workflow Clarification
The fuel transaction workflow is:
1. **REQUESTED** - User requests fuel (creates transaction)
2. **DISPENSED** - Fuel station dispenses fuel (QR scanning step) ← This happens FIRST
3. **ISSUED** - Final confirmation/approval ← This is the LAST step

### Enhancement: Include Pending Transactions
When scanning a vehicle QR code, the API now returns pending fuel transactions ready for dispensing.

**New Method Added** (lines 510-543): `buildVehicleResponseWithTransactions()`
- Queries for transactions that are:
  - **Not yet dispensed** (regardless of issued status)
  - Not cancelled, rejected, or retired
  - Associated with the scanned vehicle
- Returns them ordered by requested date (most recent first)
- Includes full transaction details for each pending transaction

**Dispense Endpoint Updated** (lines 290-311):
- Removed the check that required transactions to be issued before dispensing
- Added comment explaining that dispensing happens BEFORE issuing
- Changed default quantity to use `requestQuantity` instead of `issuedQuantity`

**Response Format:**
```json
{
  "success": true,
  "type": "vehicle",
  "id": 99241,
  "vehicleNumber": "104154",
  "model": "...",
  "make": "...",
  "institution": {...},
  "pendingTransactions": [
    {
      "id": 12345,
      "requestReferenceNumber": "...",
      "issueReferenceNumber": "...",
      "issuedQuantity": 50.0,
      "dispensed": false,
      "issued": true,
      ...
    }
  ]
}
```

### Testing
Scan a vehicle QR code from the mobile app. The web app should now:
1. Detect the JSON format
2. Extract the vehicle ID (99241) and vehicle number (104154)
3. Look up the vehicle in the database
4. Find all pending transactions for that vehicle
5. Return vehicle details + pending transactions to the mobile app

The mobile app can now show which fuel requests are ready to be dispensed for the scanned vehicle.

Check server logs to verify:
- JSON parsing is working
- Pending transactions are being found
- Transaction count is logged

### Cache Refresh Solution
Added a refresh button to the fuel request view page to bypass JPA cache.

**Problem:**
When viewing a transaction on the web app and the mobile app marks it as dispensed via API, the web page doesn't automatically update because the ReportController (SessionScoped) caches the FuelTransaction entity.

**Solution:**
1. **New Method** in `ReportController.java` (lines 437-448): `refreshCurrentFuelTransaction()`
   - Calls `fuelTransactionFacade.findFresh()` to bypass cache
   - Uses `EntityManager.refresh()` to get latest data from database
   - Shows success/error message to user

2. **New Button** in `request_view.xhtml` (lines 28-34): "Refresh" button
   - Blue info button with refresh icon
   - Tooltip: "Refresh transaction data from database (use after mobile app dispense)"
   - Updates the form to show refreshed data
   - No page reload required (AJAX update)

**Usage:**
1. Open a fuel request in the web app view page
2. Scan QR code with mobile app and mark as dispensed
3. Click the "Refresh" button on the web page
4. The dispensed status and details will update without reloading the page

---

## Enhanced Mobile Fuel Dispensing - 2025-12-31

### Overview
Implemented comprehensive mobile fuel dispensing workflow with invoice image upload support.

### New Features

#### 1. Enhanced Transaction Response
**File:** `RestQrScanController.java:484-489`

Added `requestedInstitution` to transaction response so mobile app can display which institution requested the fuel.

**Response Format:**
```json
{
  "id": 12345,
  "requestQuantity": 100.0,
  "requestedInstitution": {
    "id": 456,
    "name": "TH Karapitiya"
  },
  "vehicle": {
    "id": 99241,
    "vehicleNumber": "104154"
  }
}
```

#### 2. New Multipart Dispense Endpoint
**File:** `RestQrScanController.java:360-538`

**Endpoint:** `POST /api/qr/dispense-with-image`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `transactionId` - Long (required)
- `dispensedQuantity` - Double (required, user can edit from requested quantity)
- `comments` - String (optional)
- `invoiceImage` - File (required, JPEG/PNG only)

**Workflow:**
1. Validates authentication
2. Validates transaction exists and not already dispensed
3. Validates dispensed quantity > 0
4. Validates invoice image is present and correct format
5. Reads image bytes from upload
6. Updates FuelTransaction:
   - Sets `dispensed = true`
   - Sets `dispensedQuantity` (user-entered value)
   - Sets `dispensedAt`, `dispensedDate` = current date
   - Sets `dispensedBy` = current user
   - Sets `dispensedInstitution` = user's institution
7. Creates/Updates FuelTransactionImage:
   - Stores image bytes in database
   - Records filename, content type, file size
   - Sets `uploadedAt` = current date
   - Sets `uploadedBy` = current user
8. Returns success response with updated transaction data

**Debug Logging:**
```
=== DISPENSE WITH IMAGE REQUEST ===
Transaction ID: 517129
Dispensed Quantity: 95.5
Image: invoice_20251231.jpg (234567 bytes, image/jpeg)
User: 493503 - MACO SHED
Image bytes read: 234567
Transaction updated - ID: 517129
Creating new image record
Image saved successfully
=== DISPENSE WITH IMAGE SUCCESS ===
```

#### 3. Backward Compatibility
**File:** `RestQrScanController.java:238-258`

Kept existing `POST /api/qr/dispense` endpoint (JSON-only, no image) for backward compatibility with any existing clients.

Added comment recommending new endpoint for future implementations.

#### 4. Jersey Multipart Support
**Files Modified:**
- `pom.xml:55-60` - Added `jersey-media-multipart` dependency (version 2.33)
- `RestApplication.java:28-29` - Registered `MultiPartFeature` class
- `RestQrScanController.java:20, 24-25` - Added multipart imports

### Mobile App Integration Guide

#### After QR Scan Success
Mobile app receives response with `pendingTransactions` array:
```json
{
  "success": true,
  "type": "vehicle",
  "vehicleNumber": "104154",
  "pendingTransactions": [{
    "id": 517129,
    "requestQuantity": 100.0,
    "requestedInstitution": {
      "name": "TH Karapitiya"
    }
  }]
}
```

Display to user:
- Vehicle Number: `104154`
- Requested By: `TH Karapitiya`
- Ordered Quantity: `100.0`
- Input field for dispensed quantity (default: `100.0`, editable)

#### On Dispense Button Click
Create multipart form request:
```
POST /api/qr/dispense-with-image
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Fields:
- transactionId: 517129
- dispensedQuantity: 95.5  (user can edit this)
- comments: "Partial dispensing due to stock"  (optional)
- invoiceImage: [File - JPEG/PNG image of invoice]
```

#### Success Response
```json
{
  "success": true,
  "message": "Fuel dispensed successfully with invoice image",
  "transaction": {
    "id": 517129,
    "dispensed": true,
    "dispensedQuantity": 95.5,
    "dispensedDate": "2025-12-31"
  }
}
```

#### Error Responses
- `400` - Missing required fields, invalid quantity, invalid image format, already dispensed
- `401` - Invalid/missing authentication token
- `404` - Transaction not found
- `500` - Server error

### Database Schema
No schema changes needed. Existing tables used:
- `fueltransaction` - Transaction data with dispensed fields
- `fuel_transaction_image` - Image storage (OneToOne relationship)

### Testing
Test the new endpoint:
```bash
curl -X POST http://localhost:8080/fmis/api/qr/dispense-with-image \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "transactionId=517129" \
  -F "dispensedQuantity=100.0" \
  -F "comments=Test dispense" \
  -F "invoiceImage=@/path/to/invoice.jpg"
```

Verify in database:
```sql
-- Check transaction updated
SELECT id, dispensed, dispensed_quantity, dispensed_at, dispensed_by_id
FROM fueltransaction
WHERE id = 517129;

-- Check image saved
SELECT id, file_name, content_type, file_size, length(image_data) as image_bytes
FROM fuel_transaction_image
WHERE fuel_transaction_id = 517129;
```

### Files Modified
1. `src/main/java/lk/gov/health/phsp/bean/RestQrScanController.java`
   - Added `FuelTransactionImage` import
   - Added `FuelTransactionImageFacade` import
   - Added multipart imports (FormDataParam, FormDataContentDisposition, InputStream)
   - Added `@EJB FuelTransactionImageFacade` injection
   - Enhanced `buildTransactionResponse()` with requestedInstitution
   - Added new `dispenseFuelWithImage()` method
   - Updated `dispenseFuel()` doc comment for backward compatibility note

2. `pom.xml`
   - Added `jersey-media-multipart` dependency (version 2.33)

3. `src/main/java/lk/gov/health/phsp/RestApplication.java`
   - Registered `MultiPartFeature` class
   - Updated class doc comment

4. `CLAUDE.md` (this file)
   - Documented all changes
