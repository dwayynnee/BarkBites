# Bark Bites

School Canteen POS & Management System — Java Swing prototype

This repository provides two complementary modes:

- Standalone mock mode: in-memory Manager classes manage data for quick demos and OOP assessments.
- Firebase-backed mode: optional Firebase Auth + Firestore via REST for a realistic end-to-end flow.

Pick the mode that suits you — both are supported by the same UI and core logic.

-----

## Overview

- **Customer Ordering GUI**: kiosk-style layout for quick ordering
- **Staff Management GUI**: admin views for inventory, orders and cash-in
- UI built with Swing (absolute positioning over background imagery) and screen-specific controller panels
- Business logic lives in Manager/Service classes to keep UI code focused on presentation

## Quick Start

### Prerequisites
- Java JDK 17+ (JDK 24 is recommended if you use modern language features)
- Maven (optional — useful for building from CLI)

### Configuration: Firebase (optional)

If you want the Firebase-backed mode, set values in `src/main/resources/firebase.properties` or via env vars:
- `FIREBASE_PROJECT_ID`
- `FIREBASE_WEB_API_KEY`

There is a small smoke-test tool at `src/main/java/com/mycompany/barkbites/tools/FirebaseSmokeTest.java` you can run to verify connectivity.

### Run (IDE)

Open the project in your IDE (VS Code / IntelliJ / NetBeans) and run the main class:

- `com.mycompany.barkbites.BarkBites` — starts the application windows

### Run (CLI, Maven)

Compile and run quickly via Maven (recommended for CLI users):

```bash
mvn compile
mvn -Dexec.mainClass="com.mycompany.barkbites.BarkBites" exec:java
```

The app will launch the customer + staff windows.

## OOP Structure (high-level)

### Encapsulation
- Manager/service classes hold application state and expose methods for safe access.

### Inheritance
- UI screens typically extend Swing frames/panels; domain objects use small inheritance hierarchies where appropriate.

### Polymorphism
- Order and cart processing accept base-type product arrays/collections so mixed product types are supported.

### Abstraction
- Persistence and remote calls are abstracted behind service/utility classes so UI code stays thin.

## Project Structure (snapshot)

```
BarkBites/
├── src/
│   ├── main/java/com/mycompany/barkbites/  (UI, forms, services)
│   ├── main/resources/firebase.properties
│   └── test/
└── README.md
```

## UI Assets

Optional background images (for nicer visuals):
- `images/customer-kiosk.png`
- `images/staff-management.png`

If images are missing, the UI falls back to simple backgrounds so the app still runs.

## Design notes

- The repo supports both a lightweight in-memory demo mode and a Firebase-backed mode for a more realistic flow.
- The payment flow is implemented in `CustomerPayment` and coordinated with order/inventory services — see `payment_flow_summary.md` for an annotated diagram.

## License

This project is MIT licensed. See the `LICENSE` file.

## Contributing

Fork, branch, implement, and open a pull request. Keep changes focused and run the app locally to verify UI flows.



