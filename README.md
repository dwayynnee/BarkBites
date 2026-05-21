# Bark Bites

School Canteen POS & Management System (Standalone Java Swing Prototype)

This repository is intentionally **localized and standalone**:
- No databases
- No cloud services
- No external storage

All data lives in **in-memory arrays** managed by OOP "Manager" classes.

-----

## Overview

Bark Bites is a high-fidelity mock-up designed for OOP assessment.

- **Customer Ordering GUI**: 360×650 (kiosk-style)
- **Staff Management GUI**: 1600×900 (operations/admin)
- UI built with **absolute positioning** (`null` layout) over background images
- Business logic (array traversal, stock deduction, totals) is hidden behind Manager classes

## Quick Start

### Prerequisites
- Java JDK 24+

### Firebase (no per-device setup)

This project is set up to use **Firebase Auth + Firestore via REST** using only:
- Firebase **Project ID**
- Firebase **Web API Key**

These values are configured in [src/main/resources/firebase.properties](src/main/resources/firebase.properties) and are safe to share in the repo.

Optional dev overrides (environment variables):
- `FIREBASE_PROJECT_ID`
- `FIREBASE_WEB_API_KEY`

### Verify Firebase is connected

Run the manual smoke test main class:
- [src/main/java/com/mycompany/barkbites/tools/FirebaseSmokeTest.java](src/main/java/com/mycompany/barkbites/tools/FirebaseSmokeTest.java)

It will:
- prompt for Student ID + password
- sign in using Firebase Auth REST
- write a small document to Firestore (`smoketests/{uid}`)

### Run (VS Code)
- Use the task: **BarkBites: Run Standalone Mock**

This compiles the project into `bin/` and launches both the customer + staff windows.

## OOP Structure (4 Pillars)

### Encapsulation
- Manager classes hold data in **private arrays** and expose safe methods only.

### Inheritance
- `Product` is the base class.
- `FoodItem` and `BeverageItem` extend `Product`.

### Polymorphism
- `OrderManager.calculateCartTotalCents(Product[] cartItems)` accepts a base-type array and works for mixed subclasses.

### Abstraction
- GUIs never do complex loops/stock updates directly.
- Checkout rules live in `OrderManager.placeOrder(...)`.

## Project Structure

```
BarkBites/
├── images/
│   └── logo.png
├── src/
│   ├── data/
│   │   ├── BarkBitesSystem.java
│   │   ├── CartManager.java
│   │   ├── InventoryManager.java
│   │   └── OrderManager.java
│   ├── gui/
│   │   ├── CustomerKioskFrame.java
│   │   ├── StaffManagementFrame.java
│   │   └── StandaloneMockApp.java
│   └── models/
│       ├── Product.java
│       ├── FoodItem.java
│       ├── BeverageItem.java
│       ├── CartLine.java
│       ├── OrderLine.java
│       ├── OrderStatus.java
│       └── PosOrder.java
└── README.md
```

## UI Assets

Optional background images (your Canva exports):
- `images/customer-kiosk.png`
- `images/staff-management.png`

If the images are missing, the frames render a simple fallback background so the prototype still runs.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📧 Support

For questions or issues:
1. Check browser console (F12) for web app errors
2. Check terminal output for Node/Java errors
3. Confirm Firestore rules allow the intended access patterns

---

**Ready to get started?** Run `npm install` then `npm start`.


