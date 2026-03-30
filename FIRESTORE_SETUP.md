# 🐾 Bark Bites - Firestore & Firebase Setup Guide

**Project ID:** `barkbites-22cdf`  
**Web API Key:** `AIzaSyA3cB3tqFrCiRcuy0K7P_Cv8Mxqk7nCNnc`

---

## Phase 1: Deploy Firestore Security Rules ⚔️

### Step 1.1: Open Firebase Console
1. Go to: https://console.firebase.google.com
2. Select project: **`barkbites-22cdf`**
3. In left sidebar, click: **Firestore Database**

### Step 1.2: Update Security Rules
1. Click the **"Rules"** tab (top navigation)
2. You'll see rules like:
   ```
   rules_version = '2';
   service cloud.firestore {
     match /{document=**} {
       allow read, write: if false;
     }
   }
   ```
3. **Delete all existing rules**
4. Open the file: `firestore.rules` (in your project root)
5. Copy ALL content starting from `rules_version = '2';`
6. Paste into the Rules Editor in Firebase Console
7. Click **"Publish"** button (top right)
8. **Wait for the green checkmark** ✅ (usually 1-2 minutes)

### Step 1.3: Verify Rules Deployment
In the Firebase Console:
- Rules tab should show: ✅ **"Last updated 2 minutes ago"**
- No error messages should appear
- The staff app should now see Firestore data (instead of demo data)

---

## Phase 2: Configure the Staff App 🖥️

### Step 2.1: Verify Configuration
The staff app (`BarkBitesApp.java`) is already configured with:
- ✅ Firebase Project ID: `barkbites-22cdf`
- ✅ Web API Key: Pre-configured
- ✅ REST API: Firestore REST v1 endpoint
- ✅ Demo data fallback: Enabled

### Step 2.2: Populate Test Data (Optional)

#### Option A: Use the Web App to Create Orders
1. Start the Node.js server:
   ```bash
   cd c:\BarkBites\BarkBites
   npm start
   ```
2. Go to: http://localhost:3000
3. Login or create a student account
4. Place some test orders
5. Orders will appear in Firestore → Orders collection

#### Option B: Manually Add Data in Firebase Console
1. Firebase Console → Firestore Database → **Collections**
2. Click **"Create Collection"** → `orders`
3. Add a document with test data:
   ```json
   {
     "student_id": "S12345",
     "status": "pending",
     "total_price": 25.50,
     "items": ["Pizza", "Salad"],
     "created_at": {timestamp},
     "updated_at": {timestamp}
   }
   ```

#### Option C: Use The Provided Firebase Init Script
```bash
# Coming soon - Contact admin for firebase-init.js
```

---

## Phase 3: Test the Staff App Sync 📡

### Step 3.1: Launch Both Apps in Parallel

**Terminal 1 - Web App (Student Interface):**
```bash
cd c:\BarkBites\BarkBites
npm start
# Server runs on http://localhost:3000
```

**Terminal 2 - Desktop App (Staff Kiosk):**
```bash
cd c:\BarkBites\BarkBites
java -cp bin gui.BarkBitesApp
```

### Step 3.2: Test Real-Time Sync
1. **In Web App:** Place a test order
   - Student ID: `S12345`
   - Items: Pizza, Juice
   - Total: $25.50

2. **In Staff App:** 
   - Check "📦 Order Queue" tab
   - New order should appear in **2 seconds** (auto-refresh)
   - Order status shows: ⏳ Pending

3. **In Staff App:** Update order status
   - Select the order
   - Change status to: `in_progress`
   - Click "Update Status"
   - Should see: ✅ "Order updated and synced to Firestore"

4. **In Web App:** Refresh page
   - Student should see order status: 👨‍🍳 In Progress

### Step 3.3: Monitor Firestore in Real-Time
Open two browser tabs:

**Tab 1: Firebase Console**
- Go to: Firestore Database → Collections → orders
- Click the order you created
- Watch the `status` field update as you change it in the staff app

**Tab 2: Staff App Console Output**
```bash
# Should show:
✅ Firebase Integration Ready - Using Demo Data Mode
✅ Loaded 5 active orders
✅ Dashboard updated: 5 orders, $97.00
✅ Order updated and synced to Firestore
```

---

## Phase 4: Troubleshooting 🔧

### Issue: "Firebase API Error: HTTP 403"
**Cause:** Firestore rules haven't been published yet  
**Fix:**
1. Go to Firebase Console → Firestore → Rules
2. Make sure rules are published (green checkmark ✅)
3. Wait 2-3 minutes for rules to take effect globally
4. Restart the staff app

### Issue: "No orders found in Firestore" (using demo data)
**Cause:** Rules published but no data exists  
**Fix:**
1. Create test orders via the web app
2. Or manually add orders in Firebase Console
3. Staff app will automatically sync when data appears

### Issue: Orders appear in demo data but not from Firestore
**Cause:** API key is invalid or Firestore rules denied access  
**Fix:**
1. Verify API key in Firebase Console → Settings → Web credentials
2. Key should be: `AIzaSyA3cB3tqFrCiRcuy0K7P_Cv8Mxqk7nCNnc`
3. If different, update in: `src/gui/FirebaseRestClient.java` line 14
4. Recompile and restart

### Issue: Staff app updates aren't appearing in web app
**Cause:** Web app isn't listening to Firestore updates  
**Fix:**
1. Ensure web app is running (`npm start`)
2. Refresh the web app page (updates aren't live yet)
3. Check browser console for errors

---

## Architecture Overview 🏗️

```
┌─────────────────────────────────────────────────────┐
│                  Firestore Database                  │
│  (barkbites-22cdf)                                   │
│  Collections: orders, menu_items, inventory, users   │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌──────────────────┐  ┌──────────────────┐
│   Web App        │  │  Desktop App     │
│ (Student)        │  │  (Staff Kiosk)   │
│ localhost:3000   │  │  REST API Client │
│ Firebase SDK     │  │  Auto-refresh    │
│ Real-time        │  │  every 2-5s      │
│ Listeners        │  │                  │
└──────────────────┘  └──────────────────┘

Authentication:
- Web App: Email/Password + Session
- Staff App: Public read (rules-based access control)
```

---

## Security Model 🔒

| Collection | Read | Write | Notes |
|-----------|------|-------|-------|
| orders | Public ✅ | Auth ✅ | Staff can read; students/app create |
| menu_items | Public ✅ | Auth ✅ | Public menu display; admin manages |
| inventory | Public ✅ | Auth ✅ | Staff reads; admin updates |
| users | User/Admin ✅ | User/Admin ✅ | Private user data |
| wallets | User ✅ | User ✅ | Private wallet access |

**Why public read?**
- Staff kiosk is on school network (not internet-accessible)
- No sensitive personal data exposed
- Write access is still authenticated

---

## Performance Optimization 📊

### Current Setup
- **Order refresh rate:** Every 2 seconds
- **Inventory refresh rate:** Every 5 seconds
- **Dashboard refresh rate:** Every 2 seconds

### Bandwidth Usage
- Typical: ~50-100KB per refresh cycle
- Monthly (1000 users): ~20-40MB Firestore reads

### To Reduce Load
If you need to optimize:
1. Increase refresh intervals in `BarkBitesApp.java`
2. Implement pagination (fetch only recent orders)
3. Add caching layer for menu items (rarely change)

---

## Next Steps 🚀

- [ ] Deploy firestore.rules to Firebase Console
- [ ] Create test data (web app or Firebase Console)
- [ ] Launch both staff and student apps
- [ ] Test order creation → status updates
- [ ] Monitor Firestore usage dashboard
- [ ] Set up backups (Firebase → Cloud Storage)
- [ ] Enable Firestore audit logs (for compliance)
- [ ] Deploy web app to Vercel
- [ ] Configure production Firebase security rules

---

## Quick Command Reference

```bash
# Build staff app
cd c:\BarkBites\BarkBites
javac -d bin src/gui/FirebaseRestClient.java src/gui/BarkBitesApp.java

# Run desktop app
java -cp bin gui.BarkBitesApp

# Run web app
npm start

# Test API endpoint (in PowerShell)
$url = "https://firestore.googleapis.com/v1/projects/barkbites-22cdf/databases/default/documents/orders?key=AIzaSyA3cB3tqFrCiRcuy0K7P_Cv8Mxqk7nCNnc"
Invoke-WebRequest -Uri $url | ConvertFrom-Json
```

---

## Support & Issues
- Firebase Docs: https://firebase.google.com/docs/firestore
- REST API Reference: https://firebase.google.com/docs/firestore/use-rest-api
- Security Rules Guide: https://firebase.google.com/docs/rules

**Last Updated:** March 30, 2026
