# Mony (App + Server)

This project now wires the Android app to the existing Ktor + Postgres backend. The app uses SMS.ir OTP for signup and uses password-based login against the server, plus server-backed expense storage.

## What changed (high level)
- The Android app now uses SMS.ir OTP for signup and calls the Ktor API for password login and expenses.
- Session tokens are stored locally and used automatically for expense requests.
- A logout action clears the session token.

## How to run (simple steps)

### 1) Start the backend
From the repo root:

```bash
cd server
docker compose up -d
./gradlew run
```

The server listens on `http://localhost:8080/` by default.

### 2) Run the Android app
Open the `app` module in Android Studio and run it on an emulator.

> **Note:** The app is configured to talk to `http://10.0.2.2:8080/` (Android emulator loopback to your machine).

If you run on a physical device, update `API_BASE_URL` in `app/build.gradle.kts` to your machine’s LAN IP, for example:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.50:8080/\"")
```

### 3) Use the app
1. Enter a phone number and password, tap **Send Signup OTP**, then verify the code to create the account.
2. Returning users can log in with **Log In** using their password (no OTP required).
3. Add expenses from the main screen.
4. Use **Settings → Log out** to clear the session.

#### Test account
For quick testing you can log in with:
- Phone: `0912345678`
- Password: `TEST`
This account is hardcoded in the server and will be created on first login if it does not exist.
