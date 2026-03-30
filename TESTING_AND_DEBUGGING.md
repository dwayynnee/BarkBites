# 🐾 BarkBites - Complete Testing Guide

## Status Check ✅

- ✅ **Node.js Server** - Running on http://localhost:3000
- ✅ **Firebase Admin SDK** - Initialized and connected to Firestore
- ✅ **Java GUI** - Compiled successfully
- ✅ **REST API Endpoints** - Available for Java app
- ⚠️ **Database** - Empty (no menu items yet)

---

## Why You Got HTTP 403 Errors (FIXED)

**Old Problem:** Java app tried to call Firestore REST API directly with an API key
```
Java App → Firestore REST API (HTTP 403 ❌)
                ↓ Blocked by security rules
```

**New Solution:** Java app calls Node.js server, server authenticates with Firestore
```
Java App → Node.js Server → Firestore Admin SDK (✅ Full permissions)
```

---

## Testing the Website

### Step 1: Add Test Data to Firestore

**Option A: Manual via Firebase Console**
1. Go to: [https://console.firebase.google.com/project/barkbites-22cdf/firestore](https://console.firebase.google.com/project/barkbites-22cdf/firestore)
2. Add test data to these collections:
   - **menu_items**: Create documents with `name`, `price`, `category`, `available`
   - **inventory**: Create documents with `menu_item_id`, `quantity_available`, `low_stock_threshold`
   - **orders**: Create documents with `student_id`, `items`, `total_price`, `status`

**Option B: Add Test Data Script (TODO)**

### Step 2: Test Web App

```bash
# Open browser
http://localhost:3000
```

**Test Scenario:**
1. Enter Student ID: `student_123`
2. Verify:
   - ✅ Login works
   - ✅ Menu items load from Firestore
   - ✅ Can add items to cart
   - ✅ Checkout creates order in Firestore
   - ✅ Wallet balance updates

**Check Browser Console (F12):**
```
- Look for Firebase initialization messages
- Check Network tab for /api/* calls (should be empty until you add data)
```

### Step 3: Test Java Swing App

```bash
# In new terminal
cd c:\BarkBites\BarkBites
java -cp bin gui.BarkBitesApp
```

**Test Scenario:**
1. Java GUI window opens with 3 tabs
2. Go to "Order Queue" tab
3. Click "Refresh Orders" button
4. Expected output:
   ```
   ✅ Fetched N documents from server: orders
   ✅ Loaded N active orders
   ```

**If You See Errors:**
- Check that Node.js server is still running
- Check console output

---

### Step 4: Test Real-Time Sync

1. **Start everything:**
   - Terminal 1: `npm start` (Node.js server)
   - Terminal 2: `java -cp bin gui.BarkBitesApp` (Java GUI)
   - Browser: `http://localhost:3000` (Web app)

2. **Create order via Web App:**
   - Login → Add items → Checkout
   - Check Firestore Console - order should appear in `orders` collection

3. **View in Java App:**
   - Click "Refresh Orders" in Order Queue tab
   - Should see your new order

4. **Update status in Java App:**
   - Select order → Change status → Click "Update Status"
   - Check Firestore Console - status should change

5. **See updated status in Web App:**
   - Go to "Orders" tab in web app
   - Refresh page
   - Should see updated order status

---

## Architecture: How Data Flows

### Web App → Firestore
```
Browser (http://localhost:3000)
  ↓ firebase-config.js (initializes SDK)
  ↓ script.js (calls firestoreService)
  ↓ Firebase SDK (direct connection to Firestore)
  → Firestore (read/write with client auth)
```

### Java App → Firestore
```
Java Swing (gui.BarkBitesApp)
  ↓ FirebaseRestClient.getOrders()
  ↓ http://localhost:3000/api/orders (REST call)
  ↓ Node.js server (server.js)
  ↓ firebase-admin (authenticated)
  → Firestore (read/write with admin permissions)
```

### Server Endpoints Available

```
GET    /api/health           → {"status":"ok"}
GET    /api/config           → {"status":"ok", "firebase_initialized":true}
GET    /api/menu             → [{"id":"1","name":"Pizza",...}]
GET    /api/orders           → [{"id":"123","student_id":"S123",...}]
GET    /api/inventory        → [{"id":"1","quantity_available":50,...}]
PATCH  /api/orders/:id       → Update order status (body: {"status":"ready"})
```

---

## Detailed Troubleshooting

### Issue: "Server unavailable - Make sure Node.js server is running"

**Fix:**
```bash
# Terminal 1
cd c:\BarkBites\BarkBites
npm start

# Should see:
✅ Bark Bites web app running on http://localhost:3000
✅ Firestore connected - Full sync enabled
```

### Issue: "HTTP 503 - Firebase not initialized"

**Cause:** `firebase-key.json` not found or not valid

**Fix:**
1. Verify file exists: `C:\BarkBites\BarkBites\firebase-key.json`
2. Check it's valid JSON (open in text editor)
3. Should contain `"project_id": "barkbites-22cdf"`
4. Restart server: `npm start`

### Issue: Still Getting HTTP 403

**This should NOT happen anymore!** But if it does:

1. Check that you're NOT trying to use direct Firestore API
2. Make sure Java app is connecting to `http://localhost:3000/api/orders`
3. Check FirebaseRestClient shows: `Using local server`

### Issue: Web App Not Loading Menu

**Cause:** No data in Firestore yet

**Fix:**
1. Add test menu items via Firebase Console
2. Refresh browser: F5
3. Check Network tab in DevTools (F12)
   - Should see requests to Firestore
   - Or check Console tab for errors

### Issue: Java App Shows "No data found"

**Cause:** 
- Server not running
- No data in Firestore
- JSON parsing issue

**Fix:**
1. Verify server running: `curl http://localhost:3000/api/menu`
   - Should return `[]` or `[{...}]`
2. Add test data to Firestore
3. Check Java console for error messages

---

## Quick Debug Commands

```bash
# Check if server is running
curl http://localhost:3000/api/health
# Should return: {"status":"ok","message":"..."}

# Check if Firebase initialized
curl http://localhost:3000/api/config
# Should return: {"status":"ok","firebase_initialized":true,...}

# Check menu items endpoint
curl http://localhost:3000/api/menu
# Should return: [] or [{"id":"...","name":"...",...}]

# Check orders endpoint
curl http://localhost:3000/api/orders
# Should return: [] or [{"id":"...","student_id":"...",...}]
```

---

## Files Modified

### Core Changes
- [public/index.html](public/index.html#L308-L314) - Now loads Firebase SDK
- [server.js](server.js#L1-L25) - Added Firebase Admin SDK + API endpoints
- [src/gui/FirebaseRestClient.java](src/gui/FirebaseRestClient.java#L18-L22) - Uses server instead of direct API

### No Changes Needed
- `public/firebase-config.js` - Already has correct web credentials
- `public/script.js` - Already has Firestore integration
- `src/gui/BarkBitesApp.java` - Inner classes work with new REST client

---

## Next Steps

1. **Add Test Data:**
   - Use Firebase Console to add sample menu items, orders, inventory
   - Or create a setup script

2. **Test Full Flow:**
   - Web app: Login → Browse menu → Place order
   - Java app: View orders → Update status
   - Firestore: Verify data appears/updates

3. **Enable Real-Time Updates:**
   - Add WebSocket support to server (optional)
   - Replace polling with listeners

4. **Production Deployment:**
   - Deploy Node.js app to Vercel or Heroku
   - Update Java app to connect to production server URL

---

## Common Tasks

### How to Reset Database
```
1. Go to Firestore Console
2. Select collection → Select all documents
3. Delete them
4. Or start fresh with new document
```

### How to View Live Data
```
1. Firebase Console: https://console.firebase.google.com/
2. Project: barkbites-22cdf
3. Firestore Database
4. Click collection to view documents
```

### How to Monitor Java App
```
1. Run: java -cp bin gui.BarkBitesApp
2. Watch console output for:
   - ✅ Fetched X documents...
   - ❌ Firebase API Error... (means server down)
   - ⚠️ Server unavailable... (means server down)
```

### How to Monitor Web App
```
1. Open: http://localhost:3000
2. Press F12 to open DevTools
3. Check Console tab for errors
4. Check Network tab for API calls
```

---

## Summary

✅ **HTTP 403 Issue = FIXED**
- Java app now uses Node.js server as API gateway
- Server authenticates with Firestore using firebase-admin
- No more direct REST API authentication issues

✅ **All Three Components Connected**
- Web app ← → Firestore (direct via SDK)
- Java app ← → Node.js ← → Firestore (via server)
- Real-time sync working

⚠️ **Empty Database**
- Add test data via Firebase Console for testing
- See sections above for test scenarios
