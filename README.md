# 🐾 Bark Bites

School Canteen POS & Meal Pre-Order System  
A dual-stack solution (Web + Java) to eliminate long lines and modernize school dining.

![Maintained](https://img.shields.io/badge/Maintained%3F-yes-green.svg)
![Java](https://img.shields.io/badge/Java-11+-orange.svg)
![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-red.svg)
![Vercel](https://img.shields.io/badge/Hosting-Vercel-black.svg)

-----

## 📌 Overview

**Bark Bites** is a comprehensive meal pre-ordering system designed to bridge the gap between students and canteen staff. 

- **👥 Students** use a **web app** to browse the menu, add items to a cart, and place pre-orders
- **👨‍🍳 Canteen Staff** use a **Java Swing kiosk app** to manage the live order queue, menu items, inventory, and daily stats
- **☁️ Firebase Firestore** is the shared database (orders/menu/inventory/wallets)
- **🧩 Node/Express API gateway** serves the web app and provides REST endpoints for the Java kiosk (using the Firebase Admin SDK)

### Key Features

| Feature | Student Web App | Staff Kiosk App |
| :--- | :---: | :---: |
| 🛒 Browse menu + category filters | ✅ | ✅ |
| 🧺 Cart + checkout | ✅ | ❌ |
| 🕒 Pickup time selection | ✅ | ❌ |
| 📦 Place pre-orders | ✅ | ❌ |
| 🧾 Order status tracking (pending → ready, etc.) | ✅ | ✅ |
| 💳 Wallet balance + recharge (demo mode default) | ✅ | ❌ |
| ⏱️ Update order status | ❌ | ✅ |
| 🍽️ Menu management (add/delete items) | ❌ | ✅ |
| 📈 Inventory visibility (stock counts + low stock) | ✅ | ✅ |
| 📊 Dashboard analytics (revenue, best seller, last-24h chart) | ❌ | ✅ |
| 🔄 Live sync (Firestore listeners) | ✅ | ⚠️ (refresh-based) |

## ⚠️ The Problem

  * **Long Queues:** Students lose valuable break time waiting in line.
  * **Limited Choice:** Students often find their preferred food is sold out.
  * **Manual Tracking:** Canteen staff lack real-time inventory and order visibility.
  * **No Records:** Hard to track daily sales, popular items, or spend patterns.

## ✅ The Solution

  * **Pre-order Online:** Order food from anywhere on school campus.
  * **Zero Wait Time:** Pick up orders instantly without queuing.
  * **Smart Inventory:** Real-time stock levels visible to both students and staff.
  * **Digital Records:** Track all orders, spending, and inventory automatically.
  * **Efficient Operations:** Staff can prepare meals in order of receipt.

## 🛠️ Architecture

### Technology Stack

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Student Frontend** | HTML/CSS/JavaScript | Web browser access via Vercel |
| **Staff Frontend** | Java Swing/AWT | Desktop app for canteen kiosk |
| **API Gateway** | Node.js + Express | Hosts the web app + REST endpoints for the kiosk |
| **Backend Database** | Firebase Firestore | Single source of truth for all data |
| **Authentication** | Student ID + anonymous Firestore auth | Simple ID-based student UX + Firestore access |
| **Hosting (Web)** | Vercel | Serverless deployment |
| **Deployment (Java)** | Executable JAR | Single kiosk computer deployment |

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     FIREBASE FIRESTORE                       │
│  (Collections: users, menu_items, orders, inventory, wallets) │
└────┬────────────────────┬────────────────────────┬───────────┘
     │                    │                        │
     ▼                    ▼                        ▼
┌──────────────────┐       ┌──────────────────────────┐
│  WEB APP         │       │  NODE/EXPRESS SERVER      │
│  (Vercel/Local)  │       │  (API gateway for kiosk)  │
│                  │       │                          │
│ • Login          │       │ • Serves /public          │
│ • Browse Menu    │       │ • /api/menu_items         │
│ • Add to Cart    │       │ • /api/inventory          │
│ • Checkout       │       │ • /api/orders (+ status)  │
│ • Track Orders   │       │ • /api/setup seed         │
│ • Wallet (demo)  │       │                          │
└─────────┬────────┘       └──────────────┬───────────┘
    │                               │
    ▼                               ▼
   (Firestore SDK)                  (Admin SDK)
    │                               │
    ▼                               ▼
  ┌──────────────────┐         ┌──────────────────┐
  │  JAVA KIOSK APP   │         │  Admin Panel      │
  │                  │         │ (Firebase Console)│
  │ • Order Queue     │         │                   │
  │ • Update Status   │         │                   │
  │ • Menu Mgmt       │         │                   │
  │ • Inventory Mgmt  │         │                   │
  │ • Dashboard       │         │                   │
  └──────────────────┘         └───────────────────┘
```

## 🧭 Project Status

- **Phase 1**: Firebase setup & data models ✅ COMPLETE
- **Phase 2**: Student web app (login, menu, cart/checkout, orders, wallet demo) ✅ COMPLETE
- **Phase 3**: Staff kiosk app (order queue, inventory/menu management, dashboard) ✅ COMPLETE
- **Phase 4**: Integration hardening, testing, and deployment ⏭️ NEXT

## 🚀 Quick Start (5 min)

### Prerequisites
- **Node.js** v14+ ([download](https://nodejs.org))
- **Java JDK** 11+ ([download](https://oracle.com/java/technologies/))
- **Firebase Account** ([free](https://firebase.google.com))

### 1. Set up Firebase

- Create a Firebase project + Firestore database
- Put your service account key at `firebase-key.json` (project root) to enable the server + kiosk write access
- Update the web Firebase config in `public/firebase-config.js` if you’re using your own project

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Environment
```bash
copy .env.example .env
# Edit .env and add your Firebase credentials
```

### 4. Run Web App
```bash
npm start
# Visit http://localhost:3000
```

### 5. Run Java App
```bash
# Recommended (build + run kiosk)
npm run kiosk

# Or run separately
npm run build:kiosk
npm run run:kiosk
```

Notes:
- Keep the Node server running while using the kiosk (it’s the kiosk’s primary API gateway).
- The student wallet is in demo mode by default (localStorage). You can switch to Firestore wallet mode in `public/script.js`.

## 📁 Project Structure

```
BarkBites/
├── src/
│   ├── models/              # Data models (Java)
│   │   ├── User.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── Inventory.java
│   │   └── Wallet.java
│   ├── data/
│   │   └── FirebaseManager.java      # Firestore connector
│   └── gui/
│       └── BarkBitesApp.java         # Staff Java Swing app
├── public/                  # Web frontend (HTML/CSS/JS)
│   ├── index.html
│   ├── firebase-config.js
│   ├── script.js            # Student app logic
│   └── style.css
├── .env.example             # Environment template
├── server.js                # Express web server
├── package.json             # Node.js dependencies
└── README.md                # This file
```

## 🔐 Security

- **Student Data**: Encrypted and role-based access via Firestore security rules
- **Sensitive Keys**: `firebase-key.json` and `.env` files are in `.gitignore` (never committed)
- **Transactions**: All orders and wallet updates logged with timestamps
- **No Passwords**: Student ID-based login (demo-friendly), plus anonymous Firebase auth for Firestore access

## 🎓 Object-Oriented Principles (Java)

1. **Encapsulation**: Model classes protect data with getters/setters
2. **Singleton Pattern**: FirebaseManager ensures single database connection
3. **Abstraction**: FirebaseManager hides Firestore complexity
4. **Composition**: Order contains OrderItem objects; Wallet contains Transactions

## 💻 Development Workflow

1. **Local Development**: Run web app at `http://localhost:3000` and Java app locally
2. **Firebase Emulator** (optional): Use Firebase Emulator for offline development
3. **Real-time Sync**: Both apps listen to Firestore changes in real-time
4. **Testing**: Create test data in Firestore; verify both apps reflect changes
5. **Deployment**: Push web app to Vercel, package Java as executable JAR

## 📈 Next Steps

### For Developers
- **Phase 4**: Deploy to production (Vercel for web, JAR for desktop) + add automated tests and CI

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


