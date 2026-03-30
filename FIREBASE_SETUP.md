# Firebase Setup Guide for BarkBites

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Name it `barkbites` and proceed
4. Enable Google Analytics (optional)
5. Create the project

## Step 2: Enable Firestore Database

1. In Firebase Console, navigate to **Build** → **Firestore Database**
2. Click **"Create database"**
3. Choose **Production mode** (you'll set security rules below)
4. Select a region close to your school location
5. Click **Enable**

## Step 3: Create Firestore Collections & Schema

Once Firestore is created, manually create these collections via the Firebase Console:

### Collection: `users`
Fields per document:
- `student_id` (String, primary identifier) - e.g., "S12345"
- `name` (String) - student name
- `email` (String) - optional, student email
- `created_at` (Timestamp) - account creation date
- `role` (String) - "student" or "staff"
- `last_login` (Timestamp)

**Document ID**: Use the student_id as the document ID (e.g., "S12345")

### Collection: `menu_items`
Fields per document:
- `name` (String) - e.g., "Chicken Biryani"
- `description` (String) - meal description
- `price` (Number) - in currency units (e.g., 5.00)
- `category` (String) - e.g., "Main Course", "Dessert", "Drink"
- `available` (Boolean) - true if available for ordering
- `inventory_id` (String) - reference to inventory collection
- `image_url` (String) - optional, image of the dish
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

### Collection: `inventory`
Fields per document:
- `menu_item_id` (String) - reference to menu_items document
- `quantity_available` (Number) - current stock count
- `quantity_sold_today` (Number) - how many sold
- `low_stock_threshold` (Number) - alert when below this count
- `is_out_of_stock` (Boolean) - quick flag for UI
- `last_updated` (Timestamp) - when stock was last updated

### Collection: `orders`
Fields per document:
- `student_id` (String) - who placed the order
- `items` (Array of Maps):
  ```
  [
    { menu_item_id: "item1", quantity: 2, price: 5.00 },
    { menu_item_id: "item2", quantity: 1, price: 3.50 }
  ]
  ```
- `total_price` (Number) - total order cost
- `status` (String) - "pending", "in_progress", "ready", "completed", "cancelled"
- `created_at` (Timestamp) - order placed time
- `ready_at` (Timestamp) - when staff marked ready (null until ready)
- `picked_up_at` (Timestamp) - when student picked up (null until picked up)
- `order_number` (String) - human-readable order ID for calling out

### Collection: `wallets`
Fields per document:
- `student_id` (String) - linked to users collection
- `balance` (Number) - current balance in currency
- `total_spent` (Number) - lifetime spending
- `transactions` (Array of Maps):
  ```
  [
    { type: "order", amount: 5.00, date: Timestamp, order_id: "..." },
    { type: "recharge", amount: 20.00, date: Timestamp, admin: "..." }
  ]
  ```
- `last_transaction` (Timestamp)

## Step 4: Set Firestore Security Rules

In Firebase Console, go to **Firestore Database** → **Rules** and replace the default with:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users can read their own profile, staff can read all users
    match /users/{userId} {
      allow read: if request.auth != null && 
                     (request.auth.uid == userId || 
                      request.auth.uid in get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'staff');
      allow write: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth.uid in ['admin'];
    }
    
    // Anyone can read menu items
    match /menu_items/{itemId} {
      allow read: if request.auth != null;
      allow write: if false; // Only via admin SDK
    }
    
    // Anyone can read inventory levels
    match /inventory/{invId} {
      allow read: if request.auth != null;
      allow write: if false; // Only via admin SDK
    }
    
    // Students can read their orders, staff can read all
    match /orders/{orderId} {
      allow read: if request.auth != null && 
                     (resource.data.student_id == request.auth.uid || 
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'staff');
      allow create: if request.auth != null && request.resource.data.student_id == request.auth.uid;
      allow update: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'staff';
    }
    
    // Students can read/write their own wallet
    match /wallets/{walletId} {
      allow read: if request.auth != null && request.auth.uid == walletId;
      allow write: if false; // Only via admin SDK or backend functions
    }
    
    // Deny everything else
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**Note**: These are basic rules. For production, you'll need to integrate authentication (currently using Student ID without auth).

## Step 5: Get Firebase Configuration

1. In Firebase Console, go to **Project Settings** (gear icon)
2. Under **Your apps**, click the web icon (</>) to create a web app
3. Name it `BarkBites Web`
4. Copy the Firebase config object - it looks like:
   ```javascript
   const firebaseConfig = {
     apiKey: "YOUR_API_KEY",
     authDomain: "YOUR_PROJECT.firebaseapp.com",
     projectId: "YOUR_PROJECT_ID",
     storageBucket: "YOUR_PROJECT.appspot.com",
     messagingSenderId: "YOUR_SENDER_ID",
     appId: "YOUR_APP_ID"
   };
   ```
5. Paste this into `.env` file (see .env.example)

## Step 6: Get Java Firebase SDK Setup

For Java Swing app:

1. Download Firebase Admin SDK JAR files from [Firebase Java SDK Release](https://github.com/firebase/firebase-admin-java/releases)
2. Add to your Java project classpath:
   - `firebase-admin.jar`
   - All required dependencies (Google Cloud libraries)
3. Download a **Service Account Key**:
   - In Firebase Console → **Project Settings** → **Service Accounts**
   - Click **Generate New Private Key**
   - Save as `firebase-key.json` in project root (add to `.gitignore`)

## Step 7: Environment Variables

Create a `.env` file in project root (copy from `.env.example`):

```
# Firebase Web Config (for Vercel/Web)
REACT_APP_FIREBASE_API_KEY=your_api_key
REACT_APP_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
REACT_APP_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
REACT_APP_FIREBASE_APP_ID=your_app_id

# Java Swing Config
FIREBASE_SERVICE_ACCOUNT_KEY_PATH=./firebase-key.json
```

## Step 8: Verify Setup

### Web Frontend:
```bash
npm install firebase
npm start
# Should connect to Firestore without errors
```

### Java:
```bash
javac -cp ".:firebase-admin.jar:*" src/gui/BarkBitesApp.java
java -cp ".:firebase-admin.jar:*" src/gui/BarkBitesApp
# Should print Firebase connection status
```

## Next Steps

Once Firebase is confirmed working, start with:
1. **Web App** (Phase 2) - Build student login & menu browsing UI
2. **Java App** (Phase 3) - Build staff order management interface

Both will share the same Firestore database!
