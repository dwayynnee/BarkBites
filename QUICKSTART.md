# BarkBites - Quick Start Guide

This guide will help you set up and run both the **Student Web App** (Vercel) and **Staff Java Swing App**.

---

## Prerequisites

### For Web App
- **Node.js** (v14 or higher) - [Download](https://nodejs.org)
- **npm** (comes with Node.js)
- **Firebase Account** - [Create Free Account](https://firebase.google.com)

### For Java Swing App
- **Java JDK 11+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Firebase Admin SDK JAR files**
- **Firebase Service Account Key** (from Firebase Console)

---

## Step 1: Firebase Project Setup (5 min)

**Follow the detailed guide:** [FIREBASE_SETUP.md](./FIREBASE_SETUP.md)

This includes:
1. Creating a Firebase project
2. Setting up Firestore database
3. Creating collections
4. Downloading configuration files

**By the end, you should have:**
- ✅ Firebase project ID
- ✅ Web API key and credentials
- ✅ Service account JSON key file (`firebase-key.json`)
- ✅ Firestore collections created

---

## Step 2: Set Up Environment Variables

### Copy the example `.env` file:

```bash
# Windows PowerShell
copy .env.example .env

# Or macOS/Linux
cp .env.example .env
```

### Edit `.env` and replace with your Firebase credentials:

```env
# Firebase Web Config (from Firebase Console > Project Settings)
REACT_APP_FIREBASE_API_KEY=your_api_key_from_firebase
REACT_APP_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
REACT_APP_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
REACT_APP_FIREBASE_APP_ID=your_app_id

# Java Config
FIREBASE_SERVICE_ACCOUNT_KEY_PATH=./firebase-key.json
```

---

## Step 3: Run the Web App Locally

### Install dependencies:

```bash
npm install
```

**This installs:**
- ✅ Express.js (web server)
- ✅ Firebase SDK (for browser)
- ✅ dotenv (environment variables)

### Start the development server:

```bash
npm start
```

**Expected output:**
```
Bark Bites web app running on http://localhost:3000
Environment: development
```

### Open in browser:

Navigate to: **http://localhost:3000**

You should see the Bark Bites placeholder page. Next, we'll build the actual UI (Phase 2).

### Test Firebase connection:

Open browser console (F12) and check for any Firebase errors.

Try this in the console:
```javascript
firestoreService.getAllMenuItems().then(items => console.log("Menu items:", items));
```

If you see an array (may be empty), Firebase is connected! ✅

---

## Step 4: Set Up Java Swing App

### Prerequisites:

1. **Download Firebase Admin SDK**
   - Go to: https://github.com/firebase/firebase-admin-java/releases
   - Download `firebase-admin-{version}.jar` and all dependencies
   - Create folder: `lib/` in project root
   - Place all JARs in `lib/` folder

2. **Save Service Account Key**
   - Copy your `firebase-key.json` to project root
   - This file is in `.gitignore` (don't commit it!)

### Compile Java Code:

#### On Windows (PowerShell):
```bash
# Compile
javac -cp ".:lib/*" -d bin src/models/*.java src/data/*.java src/gui/*.java

# Run
java -cp ".:bin;lib/*" src.gui.BarkBitesApp
```

#### On macOS/Linux:
```bash
# Compile
javac -cp ".:lib/*" -d bin src/models/*.java src/data/*.java src/gui/*.java

# Run
java -cp ".:bin:lib/*" src.gui.BarkBitesApp
```

### Expected Output:
```
Firebase initialized successfully!
Bark Bites Java Swing App Started
```

A window should appear with the BarkBitesApp.

---

## Step 5: Verify Both Apps Are Connected

### Test 1: Web App → Firestore

1. Open **http://localhost:3000** in browser
2. Press **F12** to open Developer Console
3. Run:
   ```javascript
   firestoreService.getAllMenuItems().then(items => console.log(items));
   ```
4. You should see an array (may be empty for now)

### Test 2: Java App → Firestore

1. Run the Java app (see Step 4)
2. Check console for:
   ```
   Firebase initialized successfully!
   ```
3. If you see errors, check:
   - Is `firebase-key.json` in same directory as Java app?
   - Is the service account key valid? (Download fresh from Firebase Console)
   - Are all JARs in `lib/` folder?

### Test 3: Real-Time Sync

1. Open web app at **http://localhost:3000**
2. In browser console, create a test order:
   ```javascript
   const order = {
     student_id: "S12345",
     items: [{ menu_item_id: "item1", quantity: 1, price: 5.00 }],
     total_price: 5.00
   };
   firestoreService.createOrder(order).then(id => console.log("Order created:", id));
   ```
3. Check Java app - it should have a listener that logs new orders
4. Vice versa: Update order status in Java app, refresh web app to see change

---

## Project Structure

```
BarkBites/
├── src/
│   ├── models/
│   │   ├── User.java              # User model
│   │   ├── MenuItem.java          # Menu item model
│   │   ├── Order.java             # Order model
│   │   ├── Inventory.java         # Inventory model
│   │   └── Wallet.java            # Wallet model
│   ├── data/
│   │   └── FirebaseManager.java   # Firestore connector (singleton)
│   └── gui/
│       └── BarkBitesApp.java      # Main Java Swing window
├── public/
│   ├── index.html                 # Student web app HTML
│   ├── script.js                  # Student app logic (to be built)
│   ├── style.css                  # Student app styling
│   └── firebase-config.js         # Firebase configuration
├── lib/                           # Firebase Admin SDK JARs (add manually)
├── bin/                           # Compiled Java classes (created by javac)
├── .env                           # Environment variables (CREATE THIS)
├── .env.example                   # Example env file
├── .gitignore                     # Git ignore rules
├── server.js                      # Express web server
├── package.json                   # Node.js dependencies
├── FIREBASE_SETUP.md              # Detailed Firebase setup
├── QUICKSTART.md                  # This file
└── README.md                      # Project overview
```

---

## Common Issues

### Issue: "Cannot find module 'firebase'"
**Solution:** Run `npm install` to install dependencies

### Issue: Java app crashes with "Cannot initialize Firebase"
**Solution:** 
- Check `firebase-key.json` exists in project root
- Verify all JARs are in `lib/` folder
- Download fresh service account key from Firebase Console

### Issue: "FIREBASE_SERVICE_ACCOUNT_KEY_PATH not found"
**Solution:** 
- Create `.env` file (copy from `.env.example`)
- Fill in your actual Firebase credentials

### Issue: Web app can't connect to Firestore
**Solution:**
- Check `.env` file has all Firebase config values
- Verify values match your Firebase project
- Check browser console for errors (F12)
- Ensure Firestore security rules allow reads

### Issue: Firestore says "Missing or insufficient permissions"
**Solution:**
- Go to Firebase Console → Firestore → Rules
- Make sure rules match the ones in FIREBASE_SETUP.md
- Wait 1-2 minutes for rules to deploy

---

## Next Steps (Phase 2 & 3)

Once Phase 1 is verified working:

### Phase 2: Build Student Web App
- [ ] Student login screen (accepts Student ID)
- [ ] Menu browsing page
- [ ] Shopping cart
- [ ] Checkout
- [ ] Order tracking
- [ ] Wallet/balance display

### Phase 3: Build Staff Java Swing App
- [ ] Order queue display (real-time)
- [ ] Update order status buttons
- [ ] Inventory management
- [ ] Staff dashboard & analytics

### Phase 4: Integration
- [ ] End-to-end testing
- [ ] Deploy web app to Vercel
- [ ] Package Java app as executable JAR

---

## Getting Help

If you encounter issues:

1. **Check the console for errors**
   - Browser: F12 → Console tab
   - Java: Check terminal output

2. **Verify all files exist**
   - `firebase-key.json` in project root
   - All JAR files in `lib/` folder
   - `.env` file created with values

3. **Firebase Console Checks**
   - Firestore collections created?
   - API keys enabled?
   - Security rules deployed?

4. **Re-read** [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) for detailed Firebase instructions

---

## Commands Cheat Sheet

### Web App
```bash
npm install          # Install dependencies
npm start            # Start development server
npm run dev          # Same as start
```

### Java (Windows)
```bash
# Compile
javac -cp ".:lib/*" -d bin src/models/*.java src/data/*.java src/gui/*.java

# Run
java -cp ".:bin;lib/*" src.gui.BarkBitesApp
```

### Java (macOS/Linux)
```bash
# Compile
javac -cp ".:lib/*" -d bin src/models/*.java src/data/*.java src/gui/*.java

# Run
java -cp ".:bin:lib/*" src.gui.BarkBitesApp
```

---

## Success Checklist ✅

- [ ] Firebase project created
- [ ] `.env` file filled with Firebase credentials
- [ ] `firebase-key.json` saved in project root
- [ ] `npm install` completed
- [ ] Web app runs at `http://localhost:3000`
- [ ] Browser console shows no Firebase errors
- [ ] Java app compiles without errors
- [ ] Java app connects to Firebase
- [ ] Both apps can read from Firestore
- [ ] Real-time listeners work (orders appear in both apps)

**When all checks pass, you're ready for Phase 2! 🎉**
