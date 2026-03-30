# BarkBites Student Web App - Testing Guide

**Server Status:** ✅ Running on http://localhost:3000  
**Date Tested:** March 30, 2026

---

## Phase 2 Testing - Student Web Application

### Component Checklist

#### ✅ **1. Server & Configuration**
- [x] Node.js server running on port 3000
- [x] Firebase configuration loaded
- [x] Static files serving correctly
- [x] No startup errors

#### ✅ **2. Frontend Files**
- [x] index.html - 200+ lines with complete UI structure
- [x] style.css - 850+ lines with responsive design
- [x] script.js - 650+ lines with all app logic
- [x] firebase-config.js - Updated with actual Firebase credentials

#### ✅ **3. Firebase Integration**
- [x] Firebase SDK loaded (v9.23.0 from CDN)
- [x] Project ID configured: `barkbites-22cdf`
- [x] Database: Firestore (real-time)
- [x] Collections: users, menu_items, orders, inventory, wallets

#### ✅ **4. JavaScript Functions (All Defined)**
**Authentication:**
- [x] handleLogin() - Student ID login with auto-user creation
- [x] handleLogout() - Clear session and show login screen
- [x] showLoginScreen() / showAppScreen() - Screen switching

**Menu Management:**
- [x] loadMenuItems() - Fetch from Firestore
- [x] displayMenuItems() - Grid rendering with emojis
- [x] Filter functionality - All, Main Course, Dessert, Drink

**Cart Operations:**
- [x] addToCart() - Add items with quantity
- [x] removeFromCart() - Remove from cart
- [x] updateCartQuantity() - Adjust quantity
- [x] updateCartDisplay() - Real-time total calculation with 5% tax

**Checkout:**
- [x] openCheckoutModal() - Balance validation
- [x] confirmOrder() - Order creation & wallet deduction
- [x] deductInventory() - Track stock levels

**Orders:**
- [x] loadOrders() - Fetch student orders
- [x] displayOrders() - Show with status badges
- [x] Real-time listener - onStudentOrdersChange()

**Wallet:**
- [x] loadWallet() - Fetch balance & transactions
- [x] displayWallet() - Show balance, spending, order count
- [x] Real-time listener - onWalletChange()

**Notifications:**
- [x] showToast() - Animated notifications (success/error/info)

---

## Manual Testing Procedures

### Test 1: Login Screen
1. Open http://localhost:3000 in browser
2. **Expected:** Professional orange/gold gradient login screen with 🐾 icon
3. **Actions:**
   - Try without Student ID → Should show "Please enter a Student ID" error
   - Enter Student ID (e.g., "S12345") → Should login and show app

### Test 2: Menu Tab
1. After login, click "🍱 Menu" tab (should be active by default)
2. **Expected:** 
   - Grid of menu items with emoji icons
   - 4 filter buttons: All, Main Course, Dessert, Drink
   - Each card: name, category, price, "View & Order" button
3. **Actions:**
   - Click "Main Course" filter → Only shows main courses
   - Click "View & Order" → Opens modal with item details

### Test 3: Item Modal
1. Click any menu item's "View & Order" button
2. **Expected Modal Contains:**
   - Item name and description
   - Price display
   - Quantity selector (−/+buttons and input)
   - Real-time subtotal calculation
   - "Add to Cart" button
3. **Actions:**
   - Increase quantity to 3 → Subtotal updates to 3× price
   - Click "Add to Cart" → Toast says "Added X to cart" ✅

### Test 4: Shopping Cart Tab
1. Click "🛒 Cart" tab (badge shows item count)
2. **Expected:**
   - Left: List of cart items with quantity controls
   - Right: Sticky summary (Subtotal, Tax 5%, Total)
   - Wallet balance displayed
   - "Proceed to Checkout" and "Continue Shopping" buttons
3. **Actions:**
   - Adjust item quantity → Totals recalculate instantly
   - Click Remove → Item disappears, total updates
   - Click "Continue Shopping" → Back to Menu tab

### Test 5: Checkout
1. Cart has items, click "Proceed to Checkout"
2. **Expected Checkout Modal:**
   - Order summary (all items listed)
   - Subtotal, Tax (5%), Total
   - Current balance and "After Payment" amount
   - "Confirm & Pay" and "Cancel" buttons
3. **Validations:**
   - If balance too low → Toast "Insufficient balance" (red)
   - Empty cart → Toast "Your cart is empty" (info)
4. **Actions:**
   - Click "Confirm & Pay" →
     - Wallet deducted ✅
     - Order created in Firestore ✅
     - Inventory updated ✅
     - Cart cleared ✅
     - Toast "Order placed successfully! 🎉" (green) ✅

### Test 6: Orders Tab
1. Click "📦 Orders" tab
2. **Expected:**
   - Show order with status badge (PENDING = yellow)
   - List all items in order with quantities
   - Total price and date
3. **Real-Time Update Test:**
   - Staff updates order status in Firebase
   - Orders page should auto-update without refresh

### Test 7: Wallet Tab
1. Click "💳 Wallet" tab
2. **Expected:**
   - Large balance display (gradient card)
   - Stats: Total Spent, Orders count
   - Transaction history with dates
   - Each transaction shows: type (🍱 Order or 💰 Recharge), amount (+/−)
3. **Verify:**
   - Total Spent = Sum of all order transactions ✅
   - Orders = Count of "Order" type transactions ✅

### Test 8: Logout
1. Click red "Logout" button in navbar
2. **Expected:**
   - Return to login screen
   - Cart cleared
   - Session data cleared
   - Toast "Logged out successfully" (blue)

### Test 9: Responsive Design
1. Resize browser window to mobile size (~375px width)
2. **Expected:**
   - Menu grid changes to 2 columns
   - Cart layout stacks vertically
   - Navbar wraps buttons
   - All content still readable
   - Touch-friendly button sizes

### Test 10: Error Scenarios
| Scenario | Expected Behavior |
|----------|-------------------|
| Firebase offline | Toast: "Failed to load menu items" (red) |
| Student ID with special chars | Login fails gracefully |
| Closing item modal (click X) | Modal hides, item not added |
| Click modal backdrop | Modal closes |
| Negative quantity | Becomes 0 and item removed |

---

## Verification Checklist

### Database (Firestore)
- [ ] Collection: `users` - Contains created student records
- [ ] Collection: `menu_items` - Populated with test items (at least 5)
- [ ] Collection: `wallets` - Student ID has wallet with balance: 50.00
- [ ] Collection: `orders` - After checkout, contains order record
- [ ] Collection: `inventory` - quantity_available decremented after purchase

### Local State Management
- [ ] Cart persists when switching tabs
- [ ] Cart clears after successful order
- [ ] Wallet balance updates after checkout
- [ ] Order appears immediately in Orders tab

### Real-Time Sync
- [ ] Update order status in Firebase Console
- [ ] Orders tab auto-updates without page refresh
- [ ] Wallet balance updates when wallet doc changes

### UI/UX
- [ ] All buttons responsive to clicks
- [ ] Toast notifications display and auto-hide after 3 seconds
- [ ] Colors match brand (Orange #FF6B35, Gold #F7931E)
- [ ] No console errors in browser DevTools

---

## Sample Test Data

To fully test the app, Firestore should contain:

### menu_items Collection
```json
{
  "name": "Spaghetti Carbonara",
  "category": "Main Course",
  "price": 8.50,
  "description": "Creamy Italian pasta",
  "emoji": "🍝",
  "available": true
}
```

### wallets Collection
```json
{
  "student_id": "S12345",
  "balance": 50.00,
  "created_at": "2026-03-30",
  "transactions": []
}
```

### inventory Collection
```json
{
  "menu_item_id": "item_id",
  "quantity_available": 100,
  "quantity_sold_today": 0,
  "low_stock_threshold": 10,
  "out_of_stock": false
}
```

---

## Success Criteria

✅ **Phase 2 Testing Complete** when:
1. Login/logout flows work smoothly
2. Menu loads and filters work
3. Shopping cart calculates totals correctly
4. Checkout deducts wallet and creates orders
5. Orders display with real-time updates
6. Wallet shows accurate balance and history
7. Responsive design works on mobile
8. No JavaScript errors in console
9. Toast notifications appear correctly
10. Firebase integration verified

---

## Next Steps (Phase 3)

Once Phase 2 is fully tested and working:
- [ ] Build staff Java Swing GUI
- [ ] Create order queue panel
- [ ] Add order status update interface
- [ ] Add inventory management screen
- [ ] Implement real-time order updates
- [ ] Add staff dashboard/analytics

---

**Notes:**
- Initial test run: ✅ Server started successfully
- Firebase config: ✅ Credentials loaded
- Files created: ✅ HTML (200+), CSS (850+), JS (650+)
- All methods defined: ✅ 25+ functions implemented
- Real-time listeners: ✅ Firestore onSnapshot ready

**Ready for manual testing:** YES ✅
