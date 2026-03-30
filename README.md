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

- **👥 Students** use a **web app** (via Vercel) to browse menus and pre-order meals
- **👨‍🍳 Canteen Staff** use a **Java Swing desktop app** to manage orders and inventory
- **☁️ Both apps** share a **Firebase Firestore** database for real-time synchronization

### Key Features

| Feature | Student App | Staff App |
| :--- | :---: | :---: |
| 🛒 Browse Menu | ✅ | ✅ |
| 📦 Pre-order Meals | ✅ | ❌ |
| 💳 Digital Wallet | ✅ | ❌ |
| 📊 Order Queue | ❌ | ✅ |
| ⏱️ Update Order Status | ❌ | ✅ |
| 📈 Inventory Management | ❌ | ✅ |
| 📊 Dashboard & Analytics | ❌ | ✅ |
| 🔔 Real-time Notifications | ✅ | ✅ |

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
| **Backend Database** | Firebase Firestore | Single source of truth for all data |
| **Authentication** | Student ID | Simple role-based access |
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
┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐
│  WEB APP         │  │  JAVA APP        │  │  Admin Panel      │
│  (Vercel)        │  │  (Kiosk)         │  │  (Firebase Web UI)│
│                  │  │                  │  │                   │
│ • Login          │  │ • Order Queue    │  │ • Menu Mgmt       │
│ • Browse Menu    │  │ • Update Status  │  │ • User Mgmt       │
│ • Add to Cart    │  │ • Inventory Mgmt │  │ • Analytics       │
│ • Checkout       │  │ • Dashboard      │  │                   │
│ • Track Orders   │  │                  │  │                   │
│ • Wallet         │  │                  │  │                   │
└──────────────────┘  └──────────────────┘  └───────────────────┘
```

## 📚 Documentation

### Getting Started
- **[QUICKSTART.md](./QUICKSTART.md)** - Set up Firebase, web app, and Java app in 15 minutes
- **[FIREBASE_SETUP.md](./FIREBASE_SETUP.md)** - Detailed Firebase project and Firestore configuration

### Development
- **Phase 1** (Current): Firebase setup & data models ✅ COMPLETE
- **Phase 2** (Next): Student web app (login, menu, orders, wallet)
- **Phase 3** (Next): Staff Java app (order management, inventory, dashboard)
- **Phase 4** (Final): Integration, testing, and deployment

## 🚀 Quick Start (5 min)

### Prerequisites
- **Node.js** v14+ ([download](https://nodejs.org))
- **Java JDK** 11+ ([download](https://oracle.com/java/technologies/))
- **Firebase Account** ([free](https://firebase.google.com))

### 1. Set up Firebase
Follow [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) - creates project, Firestore, and gets credentials

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
# Place firebase-key.json in project root
# Add Firebase Admin SDK JARs to lib/ folder
javac -cp ".:lib/*" -d bin src/models/*.java src/data/*.java src/gui/*.java
java -cp ".:bin;lib/*" src.gui.BarkBitesApp
```

**See [QUICKSTART.md](./QUICKSTART.md) for complete setup guide.**

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
├── FIREBASE_SETUP.md        # Firebase configuration guide
├── QUICKSTART.md            # Getting started guide
├── server.js                # Express web server
├── package.json             # Node.js dependencies
└── README.md                # This file
```

## 🔐 Security

- **Student Data**: Encrypted and role-based access via Firestore security rules
- **Sensitive Keys**: `firebase-key.json` and `.env` files are in `.gitignore` (never committed)
- **Transactions**: All orders and wallet updates logged with timestamps
- **No Passwords**: Student ID-based authentication (school manages ID uniqueness)

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

### For Students/Teachers
- Read [QUICKSTART.md](./QUICKSTART.md) to set everything up

### For Developers
- **Phase 2**: Build student web UI (menu browsing, ordering, wallet display)
- **Phase 3**: Build staff Java UI (order queue, inventory management)
- **Phase 4**: Deploy to production (Vercel for web, JAR for desktop)

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
1. Check [QUICKSTART.md](./QUICKSTART.md) for common solutions
2. Review [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) for Firebase-specific help
3. Check browser console (F12) for web app errors
4. Check terminal output for Java app errors

---

**Ready to get started?** 👉 **[Go to QUICKSTART.md](./QUICKSTART.md)**


