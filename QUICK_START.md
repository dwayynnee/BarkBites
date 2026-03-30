# 🚀 QUICK START - Firestore Setup (3 Steps)

## ⏱️ Expected Time: 5-10 minutes

---

## STEP 1️⃣ Deploy Firestore Rules (2 min)

1. Go to: https://console.firebase.google.com/project/barkbites-22cdf/firestore/rules
2. Delete the existing rules (lines with `if false`)
3. Open file: `firestore.rules` from your project
4. Copy ALL content and paste into the Rules Editor
5. Click **"Publish"** button (wait for ✅ green checkmark)

✅ **You'll know it worked when:** Rules show "Last updated X minutes ago"

---

## STEP 2️⃣ Create Test Data (1 min)

**Option A: Via Web App (Recommended)**
```bash
npm start
# Open http://localhost:3000
# Create a test order
```

**Option B: Via Firebase Console**
- Firestore Database → Collections → Create `orders` collection
- Add test document:
  ```
  student_id: "S12345"
  status: "pending"
  total_price: 25.50
  items: ["Pizza", "Juice"]
  ```

✅ **You'll know it worked when:** New order appears in Firebase Console

---

## STEP 3️⃣ Test the Apps (2 min)

**Terminal 1 - Web App:**
```bash
npm start
# Runs on http://localhost:3000
```

**Terminal 2 - Desktop App:**
```bash
java -cp bin gui.BarkBitesApp
```

✅ **You'll know it worked when:** 
- Staff app shows order in "📦 Order Queue" tab
- Order status shows: ⏳ Pending
- Updates appear within 2-5 seconds (auto-refresh)

---

## 🧪 Test Status Update Sync

1. In **staff app**: Select order → Change status to `in_progress` → Click "Update Status"
2. In **Firebase Console**: Watch order document `status` field change to `in_progress`
3. In **web app**: Refresh page → See status updated to 👨‍🍳 In Progress

---

## ❌ Common Issues

| Problem | Solution |
|---------|----------|
| "HTTP 403" error | Rules not published yet - wait 2-3 min, restart app |
| No orders showing | Create test data via web app or Firebase Console |
| Updates not syncing | Check both apps running, refresh web app manually |

---

## 📋 Status Checklist

- [ ] Rules published to Firebase Console
- [ ] Test order created in Firestore
- [ ] Web app running on localhost:3000
- [ ] Desktop app running and showing orders
- [ ] Order status updates working
- [ ] Both apps connected to real Firestore data ✅

---

**Questions?** Check `FIRESTORE_SETUP.md` for detailed troubleshooting
