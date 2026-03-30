# 🐾 BarkBites Firebase Setup - Complete Fix Guide

## What Was Fixed

✅ **HTML/Web App** - Now properly loads Firebase SDK and connects to Firestore
✅ **Node.js Server** - Now initializes Firebase Admin SDK for real-time sync  
✅ **Java Swing App** - Missing panel classes created (OrderQueuePanel, InventoryPanel, DashboardPanel)
✅ **All Platforms** - Now use the same Firebase project credentials

---

## Quick Start: Getting Everything Connected

### Step 1: Install Node Dependencies

```bash
cd C:\BarkBites\BarkBites
npm install
```

This installs the new `firebase-admin` package needed for server-side Firestore access.

### Step 2: Verify Firebase Credentials

Ensure you have your `firebase-key.json` file in the project root:
- **Location:** `C:\BarkBites\BarkBites\firebase-key.json`
- **Get it from:** Firebase Console → Project Settings → Service Accounts → Generate New Key

### Step 3: Compile Java Files

```bash
cd src
javac -d ../bin gui/*.java data/*.java models/*.java
cd ..
```

### Step 4: Start the Node.js Server

```bash
npm start
```

You should see:
```
✅ Bark Bites web app running on http://localhost:3000
✅ Firestore connected - Full sync enabled
```

### Step 5: Run the Java Swing App

```bash
java -cp bin gui.BarkBitesApp
```

The Java GUI will open and connect to Firestore.

### Step 6: Open Web App

Visit **http://localhost:3000** in your browser

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Firestore Database                       │
│              (barkbites-22cdf / Production)                 │
└─────────────┬────────────────┬────────────────┬─────────────┘
              │                │                │
      ┌───────▼──────┐  ┌──────▼────────┐  ┌───▼──────────┐
      │  Node.js     │  │  Java Swing   │  │ Web Browser  │
      │  Server      │  │  GUI (Staff)  │  │ (Students)   │
      │              │  │               │  │              │
      │ • REST API   │  │ • REST API    │  │ • Firebase   │
      │ • Admin SDK  │  │ • Direct REST │  │   SDK (v10)  │
      │ • Sync       │  │ • Direct REST │  │ • Real-time  │
      └──────────────┘  └───────────────┘  └──────────────┘
```

---

## Platform-Specific Details

### Web App (http://localhost:3000)

**How it connects to Firebase:**
1. Loads Firebase SDK from CDN (v10.7.0)
2. Loads `firebase-config.js` with web credentials
3. Uses Firestore client SDK for real-time sync
4. Collections: `users`, `menu_items`, `orders`, `inventory`, `wallets`

**Data Flow:**
```
Browser (script.js) 
  → firebase.firestore() 
  → Firestore 
  ← Real-time listeners
  → Update DOM
```

### Node.js Server (Port 3000)

**API Endpoints for Java Swing App:**
- `GET /api/menu` - Get all menu items
- `GET /api/inventory` - Get all inventory
- `GET /api/orders` - Get all orders
- `PATCH /api/orders/:id` - Update order status
- `POST /api/orders` - Create new order

**How it connects:**
```
express server.js
  → firebase-admin.initializeApp()
  → Firestore
  ← Read/Write data
  → REST endpoints
```

### Java Swing App (Staff Kiosk)

**How it connects:**
```
BarkBitesApp
  → FirebaseRestClient
  → Firestore REST API (https://firestore.googleapis.com/v1/...)
  → Uses API key for authentication
  → JSONParsing of responses
```

**Panels:**
- **OrderQueuePanel** - View orders, change status (pending→ready→completed)
- **InventoryPanel** - View stock levels, restock items, low stock alerts
- **DashboardPanel** - Analytics (total orders, revenue, pending, average value)

---

## Troubleshooting Connection Issues

### Issue 1: "firebase-key.json not found"

**Symptom:** Server shows `⚠️ Running without Firestore`

**Fix:**
1. Get your Firebase service account key from Firebase Console
2. Save as `firebase-key.json` in project root
3. Restart server: `npm start`

### Issue 2: "Firestore initialization error" in Browser Console

**Symptom:** Web app won't login

**Fix:**
1. Open browser DevTools (F12)
2. Check if `firebase-config.js` loaded successfully
3. Verify Firebase credentials match your project in `public/firebase-config.js`

### Issue 3: Java App Can't Fetch Data

**Symptom:** "❌ Firebase API Error: HTTP 401"

**Fix:**
1. Verify API key in `FirebaseRestClient.java` is correct
2. Check Firestore Rules allow public read access
3. Try: `curl https://firestore.googleapis.com/v1/projects/barkbites-22cdf/databases/default/documents/orders?key=YOUR_KEY`

### Issue 4: Data Not Syncing Between Web and Java

**Fix:**
1. Both must use same Firebase project ID: `barkbites-22cdf`
2. Check `firebase-config.js` has matching `projectId`
3. Check `FirebaseRestClient.java` has matching `PROJECT_ID`

---

## File Changes Summary

### Modified Files:
- **public/index.html** - Now loads Firebase SDK + proper scripts
- **server.js** - Added Firebase Admin SDK + REST API endpoints
- **package.json** - Added `firebase-admin` dependency

### New Files Created:
- **src/gui/OrderQueuePanel.java** - Order management for staff
- **src/gui/InventoryPanel.java** - Stock management
- **src/gui/DashboardPanel.java** - Analytics dashboard

---

## Testing the Connection

### Test 1: Web App
```
1. Go to http://localhost:3000
2. Enter Student ID: "S12345"
3. See menu items load from Firestore
4. Add items to cart → Checkout
5. Should deduct from wallet in Firestore
```

### Test 2: Java App
```
1. Run: java -cp bin gui.BarkBitesApp
2. Switch to "Order Queue" tab
3. Orders should load from Firestore
4. Check "Order Queue" shows real orders
```

### Test 3: Server
```
curl http://localhost:3000/api/menu
# Should return JSON array of menu items from Firestore
```

---

## Real-Time Sync Details

**Web App → Firestore:**
- Logs in → Creates/updates user in `users` collection
- Adds to cart → Watches `orders` collection in real-time
- Checkouts → Deducts from wallet, creates order

**Java App → Firestore:**
- Fetches orders every 2 seconds
- Updates order status on clicks
- Fetches inventory every 5 seconds

**Server → Firestore:**
- REST endpoints available for ALL platforms
- Handles JSON parsing/serialization
- Can be extended with additional endpoints

---

## Next Steps (Optional Enhancements)

1. **Add WebSocket Support** - Replace polling with real-time WebSocket updates
2. **Add Authentication** - Secure endpoints with Firebase Auth tokens
3. **Add Logging** - Log all Firestore operations to file
4. **Add Caching** - Cache menu items locally to reduce API calls
5. **Production Deployment** - Deploy to Vercel (web) with Firestore
6. **Native Packaging** - Package Java app as .exe for Windows kiosk

---

## Need Help?

Check these files for configuration:
- **Web credentials:** [public/firebase-config.js](public/firebase-config.js)
- **Java REST client:** [src/gui/FirebaseRestClient.java](src/gui/FirebaseRestClient.java#L6-L7)
- **Server setup:** [server.js](server.js#L7-L15)
- **Firestore rules:** [FIRESTORE_SETUP.md](FIRESTORE_SETUP.md)
