# FMIS Mobile App Development Plan

**Date Created:** 2025-12-17
**Project:** FMIS Android Mobile App with QR Scanning
**Platform:** Flutter (Android)

---

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Phase 1: Backend API Development](#phase-1-backend-api-development)
5. [Phase 2: Flutter Environment Setup](#phase-2-flutter-environment-setup)
6. [Phase 3: Flutter App Development](#phase-3-flutter-app-development)
7. [Phase 4: Testing & Deployment](#phase-4-testing--deployment)
8. [Progress Tracking](#progress-tracking)

---

## Overview

### Goal
Develop an Android mobile application that allows users to:
1. Login with username/password
2. Scan QR codes for fuel dispensing or other operations

### Technology Stack
- **Mobile App:** Flutter (Dart language)
- **Backend:** Existing Java JSF application (fmis)
- **Communication:** REST APIs (JSON)
- **Database:** Existing database (no changes needed)

### Project Structure
```
D:\Dev\
├── fmis\                          # Existing Java web application
│   └── src\main\java\lk\gov\health\phsp\
│       ├── bean\                  # Controllers (add REST endpoints here)
│       └── entity\                # JPA entities
│
└── fmis-mobile\                   # NEW Flutter mobile app
    ├── lib\
    │   ├── main.dart
    │   ├── models\                # Data models
    │   ├── screens\               # UI screens
    │   ├── services\              # API communication
    │   └── widgets\               # Reusable UI components
    ├── android\
    └── pubspec.yaml
```

---

## Architecture

### Communication Flow
```
┌─────────────────┐                    ┌──────────────────┐
│  Flutter App    │◄───REST/JSON──────►│  Java Backend    │
│  (User's Phone) │                    │  (FMIS Server)   │
└─────────────────┘                    └──────────────────┘
        │                                       │
        │                                       │
    [Camera for                            [Database]
     QR Scanning]
```

### API Endpoints (to be created)

| Endpoint | Method | Purpose | Request | Response |
|----------|--------|---------|---------|----------|
| `/api/auth/login` | POST | User authentication | `{username, password}` | `{token, user, success}` |
| `/api/auth/validate` | GET | Validate token | Header: `Authorization: Bearer <token>` | `{valid, user}` |
| `/api/qr/scan` | POST | Process QR code | `{qrData, token}` | `{success, message, data}` |
| `/api/fuel/dispense` | POST | Dispense fuel | `{qrData, amount, token}` | `{success, transaction}` |
| `/api/user/profile` | GET | Get user details | Header: `Authorization: Bearer <token>` | `{user details}` |

---

## Prerequisites

### Software Requirements
1. **Java Development** (Already have):
   - JDK 8+
   - Maven
   - Your existing IDE (NetBeans/IntelliJ)

2. **Flutter Development** (Need to install):
   - Flutter SDK: https://flutter.dev/docs/get-started/install/windows
   - Android Studio: https://developer.android.com/studio
   - VS Code (optional, lightweight): https://code.visualstudio.com/

3. **Android Testing**:
   - Android Emulator (comes with Android Studio)
   - Or physical Android device with USB debugging enabled

### Hardware Requirements
- Windows PC (you already have)
- 8GB+ RAM recommended
- 10GB+ free disk space for Flutter SDK and Android tools

---

## Phase 1: Backend API Development

### 1.1 Create REST API Infrastructure

**Files to create/modify:**

1. **Create `RestAuthenticationController.java`**
   - Location: `src/main/java/lk/gov/health/phsp/bean/RestAuthenticationController.java`
   - Purpose: Handle mobile app login
   - Methods:
     - `login()` - Authenticate user, return JWT token
     - `validateToken()` - Verify token validity

2. **Create `RestQrScanController.java`**
   - Location: `src/main/java/lk/gov/health/phsp/bean/RestQrScanController.java`
   - Purpose: Handle QR code scanning operations
   - Methods:
     - `processScan()` - Process QR code data
     - `getFuelDispenseDetails()` - Get details for confirmation

3. **Create `JwtTokenUtil.java`**
   - Location: `src/main/java/lk/gov/health/phsp/util/JwtTokenUtil.java`
   - Purpose: Generate and validate JWT tokens
   - Methods:
     - `generateToken(WebUser user)` - Create JWT
     - `validateToken(String token)` - Verify JWT
     - `getUserFromToken(String token)` - Extract user info

4. **Create `CorsFilter.java`**
   - Location: `src/main/java/lk/gov/health/phsp/filter/CorsFilter.java`
   - Purpose: Allow mobile app to call APIs
   - Enable CORS headers

5. **Update `web.xml`**
   - Location: `src/main/webapp/WEB-INF/web.xml`
   - Register REST servlet mappings
   - Add CORS filter

### 1.2 Dependencies to Add

Add to `pom.xml`:
```xml
<!-- JWT for token-based authentication -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- JSON processing (if not already present) -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.9</version>
</dependency>

<!-- JAX-RS for REST APIs (if not already present) -->
<dependency>
    <groupId>javax.ws.rs</groupId>
    <artifactId>javax.ws.rs-api</artifactId>
    <version>2.1.1</version>
</dependency>
```

### 1.3 Testing Backend APIs
- Use Postman or similar tool to test each endpoint
- Verify token generation and validation
- Test QR code processing logic

**Status:** ✅ Completed - All backend files created, ready for testing

---

## Phase 2: Flutter Environment Setup

### 2.1 Install Flutter SDK

**Steps:**
1. Download Flutter SDK: https://docs.flutter.dev/get-started/install/windows
2. Extract to `C:\flutter` (or preferred location)
3. Add to PATH: `C:\flutter\bin`
4. Open Command Prompt and run: `flutter doctor`
5. Follow any recommendations from `flutter doctor`

### 2.2 Install Android Studio

**Steps:**
1. Download: https://developer.android.com/studio
2. Install with default settings
3. Open Android Studio → More Actions → SDK Manager
4. Install:
   - Android SDK Platform (latest)
   - Android SDK Build-Tools
   - Android Emulator
5. Create virtual device (AVD):
   - Tools → Device Manager → Create Device
   - Choose Pixel 5 or similar
   - System Image: Android 11+ (API 30+)

### 2.3 Verify Installation

Run in Command Prompt:
```bash
flutter doctor
```

Expected output:
```
[√] Flutter (Channel stable, version 3.x.x)
[√] Android toolchain - develop for Android devices
[√] Android Studio (version 2022.x or later)
[√] VS Code (optional)
```

**Status:** ⬜ Not Started

---

## Phase 3: Flutter App Development

### 3.1 Create Flutter Project

**Command:**
```bash
cd D:\Dev
flutter create fmis_mobile
cd fmis_mobile
```

**Project structure created:**
```
fmis_mobile\
├── lib\
│   └── main.dart              # Entry point
├── android\                   # Android-specific config
├── test\                      # Unit tests
└── pubspec.yaml              # Dependencies
```

**Status:** ⬜ Not Started

### 3.2 Add Dependencies

Edit `pubspec.yaml`:
```yaml
dependencies:
  flutter:
    sdk: flutter

  # HTTP requests
  http: ^1.1.0

  # QR Code scanning
  mobile_scanner: ^3.5.0

  # Secure storage for tokens
  flutter_secure_storage: ^9.0.0

  # State management
  provider: ^6.1.0

  # Permissions
  permission_handler: ^11.0.0
```

Run: `flutter pub get`

**Status:** ⬜ Not Started

### 3.3 App Structure Setup

**Create folder structure:**
```
lib\
├── main.dart
├── config\
│   └── api_config.dart        # API URLs and config
├── models\
│   ├── user.dart              # User model
│   ├── api_response.dart      # API response wrapper
│   └── qr_data.dart           # QR scan data model
├── services\
│   ├── auth_service.dart      # Authentication logic
│   ├── api_service.dart       # HTTP client
│   └── storage_service.dart   # Token storage
├── screens\
│   ├── splash_screen.dart     # Initial loading screen
│   ├── login_screen.dart      # Login UI
│   ├── home_screen.dart       # Main dashboard
│   └── qr_scanner_screen.dart # QR scanning UI
└── widgets\
    ├── custom_button.dart     # Reusable button
    └── loading_dialog.dart    # Loading indicator
```

**Status:** ⬜ Not Started

### 3.4 Implement Core Files

#### 3.4.1 API Configuration (`config/api_config.dart`)
```dart
class ApiConfig {
  // Update this with your actual server URL
  static const String baseUrl = 'http://your-server-ip:8080/fmis';

  static const String loginEndpoint = '/api/auth/login';
  static const String validateEndpoint = '/api/auth/validate';
  static const String qrScanEndpoint = '/api/qr/scan';
}
```

#### 3.4.2 Authentication Service (`services/auth_service.dart`)
- Login method
- Token storage and retrieval
- Logout
- Token validation

#### 3.4.3 Login Screen (`screens/login_screen.dart`)
- Username field
- Password field (obscured)
- Login button
- Error handling
- Loading indicator

#### 3.4.4 QR Scanner Screen (`screens/qr_scanner_screen.dart`)
- Camera preview
- QR code detection
- Vibration/sound feedback on scan
- Send scanned data to backend
- Display result

**Status:** ⬜ Not Started

### 3.5 Android Configuration

**Update `android/app/src/main/AndroidManifest.xml`:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
```

**Update `android/app/build.gradle`:**
```gradle
android {
    defaultConfig {
        minSdkVersion 21  // Required for QR scanner
        targetSdkVersion 33
    }
}
```

**Status:** ⬜ Not Started

### 3.6 Build and Test

**Run on emulator:**
```bash
flutter run
```

**Build APK:**
```bash
flutter build apk --release
```

APK location: `build\app\outputs\flutter-apk\app-release.apk`

**Status:** ⬜ Not Started

---

## Phase 4: Testing & Deployment

### 4.1 Testing Checklist

**Backend API:**
- [ ] Login API returns valid JWT token
- [ ] Token validation works
- [ ] QR scan endpoint processes data correctly
- [ ] Error handling for invalid requests

**Mobile App:**
- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials shows error
- [ ] Token is stored securely
- [ ] QR scanner opens camera
- [ ] QR code detection works
- [ ] Scanned data sent to backend
- [ ] Response displayed to user
- [ ] Logout clears token

**Integration:**
- [ ] App can reach backend server
- [ ] CORS allows cross-origin requests
- [ ] Token authentication works end-to-end

### 4.2 Deployment

**Backend:**
1. Deploy updated Java application to server
2. Ensure REST endpoints are accessible
3. Note server IP/domain for mobile app config

**Mobile App:**
1. Update `ApiConfig.baseUrl` with production server URL
2. Build release APK: `flutter build apk --release`
3. Test APK on physical device
4. Distribute APK to users (email, file sharing, or Google Play)

**Status:** ⬜ Not Started

---

## Progress Tracking

### Session History

#### Session 1 - 2025-12-17
- [x] Discussed requirements
- [x] Created development plan document
- [x] Created JWT utility class (`JwtTokenUtil.java`)
- [x] Created REST authentication controller (`RestAuthenticationController.java`)
- [x] Created REST QR scan controller (`RestQrScanController.java`)
- [x] Created CORS filter (`CorsFilter.java`)
- [x] Added Gson dependency to pom.xml
- [x] Updated web.xml with REST API servlet mappings
- [x] Phase 1 backend development completed
- [ ] Next: User to compile/deploy backend and install Flutter SDK

---

## Quick Reference Commands

### Flutter Commands
```bash
# Check Flutter installation
flutter doctor

# Create new project
flutter create project_name

# Get dependencies
flutter pub get

# Run app (debug)
flutter run

# Build APK (release)
flutter build apk --release

# Clean build
flutter clean
```

### Common Issues & Solutions

**Issue:** Flutter not recognized
**Solution:** Add Flutter bin folder to system PATH

**Issue:** Android licenses not accepted
**Solution:** Run `flutter doctor --android-licenses`

**Issue:** Emulator not starting
**Solution:** Enable virtualization in BIOS, or use physical device

**Issue:** Cannot connect to backend
**Solution:** Use `10.0.2.2` instead of `localhost` in emulator

---

## Next Steps

1. **Review this plan** - Make sure you understand the architecture
2. **Choose starting point:**
   - Option A: Backend first (create REST APIs)
   - Option B: Setup Flutter first (install environment)
   - Option C: Parallel (someone else does Flutter setup while working on backend)

3. **When ready to start:** Inform Claude which phase to begin with

---

## Notes & Modifications

(Add your notes here as you progress)

-
-
-

---

**Document Version:** 1.0
**Last Updated:** 2025-12-17
