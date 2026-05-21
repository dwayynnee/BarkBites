## Plan: BarkBites Customer + Staff Flow (Firebase)

Implement the requested CustomerForms and StaffForms flows by adding minimal input overlays (JTextField/JPasswordField) and replacing the in-memory “Manager” classes with Firebase-backed services. Keep existing navigation via FormNavigator (JFrame swapping) and wire missing actions (signup/login via Firebase Auth, wallet + orders via Firestore, add-to-cart + checkout, staff access, staff management screens). Add clear comments marking OOP pillars and a separate README listing where each pillar is used.

**Steps**
1. Align terminology and IDs
   1. Treat the Customer UI “Student ID” as the unique login identifier (the spec mentions “email”). Keep the UI text as “Student ID” and store it consistently as `studentId` in code.
   2. Set initial wallet balance for new signups to 0. Do not create sample users on startup; the primary path is Signup → Login.
   3. Firebase Auth constraint: if using Email/Password sign-in, map `studentId` to a valid email for Firebase Auth (recommended: `studentId + "@barkbites.local"`). Keep showing “Student ID” in the UI; the email mapping stays internal.

2. Add Firebase setup + data models (Firestore)
   1. Choose Firebase products
      - Firebase Authentication: Customer signup + login (Email/Password via REST API is simplest for a desktop Swing app).
      - Cloud Firestore: Customers, orders, inventory, sales totals.
      - Optional: Firestore can also store carts if you want persistence across app restarts; otherwise keep cart in-memory per session.
   2. Add config + secrets handling
      - Store Firebase Web API Key (for Auth REST) in a local config file not committed (e.g., `src/main/resources/app.properties` in dev, or external file).
      - For Firestore access, use one of:
        - Service account JSON (Google Cloud / Firebase Admin SDK) loaded from an external path (recommended for school projects; do NOT commit the JSON).
        - OR Firestore REST API (works too, but more manual).
   3. Add Maven dependencies + initialization
      - Maven dependencies (typical):
        - Firebase Admin SDK (Firestore access via service account)
        - HTTP client (for Firebase Auth REST calls)
        - JSON parser/mapper
      - Initialize Firebase once at app startup (e.g., in `BarkBites.main`): create the Firestore client and share it via your Service layer.
      - Swing rule: do Firebase network calls off the EDT (use `SwingWorker` or a background thread), then update UI on the EDT.
   4. Create model classes (POJOs)
      - Customer: uid, studentId, name, mobile, walletBalance
      - MenuItem (Food): id, name, shortDescription, price
      - CartLine: menuItemId, quantity, addOns (optional boolean flags for Egg/Coke)
      - Order: orderId, customerUid, lines[], total, pickupTimeSlot, paymentMethod, status, createdAt
      - InventoryItem: menuItemId, stock
      - SalesSummary: dayKey, totalsByMenuItemId, totalRevenue
   5. Firestore collections (suggested)
      - `customers/{uid}`: { studentId, name, mobile, walletBalance }
      - `menu/{menuItemId}`: { name, shortDescription, price } (seed 4 items at startup if missing)
      - `orders/{orderId}`: { customerUid, lines, total, pickupTimeSlot, paymentMethod, status, createdAt }
      - `inventory/{menuItemId}`: { stock }
      - `sales/{dayKey}`: { totalsByMenuItemId, totalRevenue }
      - Optional: `vouchers/{code}` for QR “top-up codes” (or just keep it simulated)
   6. Replace “Manager” classes with Firebase-backed services (encapsulate all Firebase calls)
      - AuthService: signUp(studentId,password), signIn(studentId,password) → returns uid/idToken
      - CustomerService: getCustomer(uid), createCustomer(uid, studentId, name, mobile), addWallet(uid, amount), deductWallet(uid, amount)
      - MenuService: listMenu(), getById(id), seedDefaultsIfMissing()
      - CartService: in-memory per session (addItem, clear, total) OR Firestore-backed per user
      - OrderService: placeOrder(uid, cart, pickupSlot, paymentMethod), listOrders(), updateStatus(orderId,status)
      - InventoryService: getStock(menuItemId), deductStockForOrder(lines)
      - SalesService: recordOrder(order) and provide aggregates for StaffStatistics
   7. Add AppSession/AppState singleton
      - currentCustomerUid
      - currentCustomerIdToken (if needed for Auth REST workflows)
      - currentCart (in-memory by default)
      - selectedMenuItemId (set by CustomerMenuPanel buttons)

3. Customer validation flow (landing → login options → login/signup)
   1. CustomerLandingPage: keep 4s auto-redirect to CustomerLoginOptions.
   2. CustomerLoginOptions: keep buttons routing to CustomerLoginPanel / CustomerSignupPanel.
   3. CustomerLoginPanel:
      - Overlay `JTextField` for Student ID and `JPasswordField` for password aligned to the PNG inputs.
      - Wire the “Sign in” button (currently `jButton1`) to:
        - Read inputs → call AuthService.signIn(studentId, password)
        - On success: set AppSession.currentCustomerUid (+ optional idToken), redirect to CustomerHomePagePanel
        - On failure: show message dialog and stay on page
      - Keep existing “Sign up!” navigation (`jButton2`) and back (`jButton3`).
   4. CustomerSignupPanel:
      - Overlay fields matching the PNG: Student ID, Name, Mobile, Password.
      - Wire “Register!” button to:
        - Validate required fields
        - Create Firebase Auth user (AuthService.signUp)
        - Create/initialize Firestore customer doc (wallet starts 0)
        - Redirect to CustomerRegistrationCompletePanel
      - Keep “Log in!” link (`jButton1`) navigation.

4. Customer homepage roles/actions (Menu → Food → Cart → Profile)
   1. CustomerHomePagePanel: keep as hub to Menu/Cart/Profile.
   2. CustomerMenuPanel:
      - Map the 4 food buttons (`jButton4`–`jButton7`) to 4 fixed MenuItems:
        - Set AppSession.selectedMenuItemId then redirect to CustomerShowFoodPanel.
   3. CustomerShowFoodPanel:
      - On open, load selected MenuItem from MenuService.
      - “View description” requirement:
        - Show a dialog with the item description on panel open.
      - Add-to-cart (`jButton3`) should:
        - Use quantity = 1 (no additional quantity controls)
        - Read add-ons (Egg/Coke) if implemented
        - Call CartService.addItem(...)
        - Redirect to CustomerCartPanel
      - Back (`jButton4`) returns to CustomerMenuPanel.
   4. CustomerCartPanel:
      - Add an invisible “Place Order!” button over the yellow button in the PNG.
      - On “Place Order!” click:
        - Prompt for pickup slot (simple option dialog, e.g., 10:00/10:30/11:00…)
        - Prompt for payment method: Cash or Wallet
        - If Wallet: load customer from Firestore; ensure `walletBalance >= total`; then deduct via CustomerService (use a Firestore transaction/atomic update if possible)
        - Deduct inventory via InventoryService (minimal: stock per menu item)
        - Place order via OrderService (writes to Firestore)
        - Record sales via SalesService
        - Clear cart (CartService)
        - Show confirmation dialog (orderId + pickup slot + total)
      - Keep cart interactions minimal: show a summary via dialogs (no +/− controls).
   5. CustomerProfilePanelVisible:
      - Overlay labels for customer name, mobile, and wallet balance (so it’s dynamic).
      - Cash in button (`jButton4`) → CustomerCashInPanel.
      - Scan QR (`jButton5`) → CustomerQrScannerPanel.
      - Generate QR (`jButton6`) → CustomerQrScanPanel.
   6. CustomerCashInPanel:
      - Overlay a label for Available Balance.
      - Add invisible buttons for quick cash (+20, +50, +100, +200) over the PNG buttons.
      - Overlay a `JTextField` for Enter Amount.
      - Use Enter on the amount field as the “Confirm cash in” action.
      - Update wallet via CustomerService.addWallet() (Firestore update) and refresh labels.
   7. CustomerQrScannerPanel:
      - Implement a simple “scan” simulation: input dialog for a code; validate; on success add a small fixed amount to the wallet (voucher-style) via CustomerService and show the updated balance.
      - Optional: represent vouchers as Firestore docs (`vouchers/{code}`) with `amount` and `redeemed` flag.
   8. CustomerQrScanPanel:
      - Implement “generate QR”: show dialog with a generated code like `BB-{customerId}-{timestamp}`.

5. Staff validation + management flow
   1. StaffLandingPage: keep button → StaffPassword.
   2. StaffPassword:
      - Overlay either 4 small JTextFields or 1 JPasswordField limited to 4 digits, aligned to the four boxes.
      - Validate against hardcoded PIN (e.g., 1234) OR a Firestore doc (`config/staffPin`).
      - Success → StaffOrders; failure → message dialog.
   3. StaffOrders / StaffMenu / StaffInventory:
      - Add a simple overlay component in the big blue content area:
        - A scrollable JTextArea or JTable showing current Orders/Menu/Inventory
            - 1–2 action buttons that use dialogs for inputs (keep minimal; prefer Mark Done for orders)
      - All actions call the appropriate Firebase-backed Service methods.
   4. StaffStatistics:
      - Add a custom JPanel over the blue area that paints a very simple bar chart from SalesService totals (from Firestore `sales/{dayKey}`).
      - Keep it minimal (few bars, labeled), no external chart libraries.

6. OOP pillars comments + dedicated README
   1. In code: add short comments near the exact spots where each pillar is demonstrated:
      - Encapsulation: Firebase client + Firestore access hidden inside Service/Repository classes
      - Abstraction: UI forms call Service methods (no Firebase queries/transactions in UI)
      - Inheritance: all forms extend JFrame
      - Polymorphism: depend on interfaces (e.g., `CustomerRepository`) with Firebase implementation; keep `MenuItem` flowing through cart/order APIs
   2. Add a new document `README_OOP_PILLARS.md` listing each pillar and the exact class/file locations where it appears.

**Relevant files**
- src/main/java/com/mycompany/barkbites/FormNavigator.java — keep redirect pattern
- src/main/java/com/mycompany/barkbites/BarkBites.java — app entry and window positioning
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerLoginPanel.java — add login inputs + validation
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerSignupPanel.java — add signup inputs + Firebase Auth + create Firestore customer
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerMenuPanel.java — map 4 buttons to 4 items
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerShowFoodPanel.java — description + add-to-cart
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCartPanel.java — “Place Order” + checkout dialogs
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerProfilePanelVisible.java — dynamic labels + wallet/QR links
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerCashInPanel.java — quick cash + enter amount
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerQrScannerPanel.java — scan simulation
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerQrScanPanel.java — generate QR simulation
- src/main/java/com/mycompany/barkbites/StaffForms/StaffPassword.java — 4-digit PIN validation
- src/main/java/com/mycompany/barkbites/StaffForms/StaffOrders.java — show/manage orders
- src/main/java/com/mycompany/barkbites/StaffForms/StaffMenu.java — show/manage menu
- src/main/java/com/mycompany/barkbites/StaffForms/StaffInventory.java — show/manage inventory
- src/main/java/com/mycompany/barkbites/StaffForms/StaffStatistics.java — bar chart panel
- New packages: src/main/java/com/mycompany/barkbites/models (POJOs) and /data (Firebase-backed services/repositories)
- New doc: README_OOP_PILLARS.md

**Verification**
1. Run the app from BarkBites main and walk flows:
   - Customer: Landing → LoginOptions → Signup → RegistrationComplete → Home → Menu → ShowFood → AddToCart → Cart → PlaceOrder
   - Customer: Profile → CashIn quick buttons + manual amount → balance updates
   - Staff: Landing → Password (wrong pin blocked) → Orders → Menu/Inventory/Statistics
2. Confirm that: login blocks invalid users (Firebase Auth), signup persists across app restarts (Firestore), wallet deductions occur for wallet payments, orders appear in StaffOrders (Firestore query), statistics bars change after orders (sales totals updated).

**Decisions**
- Keep UI mostly unchanged (transparent overlays + dialogs).
- Use Firebase Auth + Firestore for persistence.
- Use dialogs for pickup/payment selection to avoid heavy UI work.

**Further Considerations**
1. Keep “Student ID” as the login identifier throughout.
2. Keep QR as wallet top-up vouchers (not a payment method).
3. Security note: do not commit service account JSON or API keys; load them from local config/environment.
