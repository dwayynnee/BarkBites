## Plan: BarkBites Customer + Staff Flow (Simple In‑Memory)

Implement the requested CustomerForms and StaffForms flows by adding minimal input overlays (JTextField/JPasswordField) and a small set of in-memory “Manager” classes (arrays + simple methods). Keep existing navigation via FormNavigator (JFrame swapping) and wire missing actions (login validation, signup add, add-to-cart, place order checkout, staff PIN validation, staff manage screens). Add clear comments marking OOP pillars and a sepaarate README listing where each pillar is used.

**Steps**
1. Align terminology and IDs
   1. Treat the Customer UI “Student ID” as the unique login identifier (your spec says “email”). If not, rename variables only (keep UI text) but store it as `emailOrStudentId`.
   2. Decide initial wallet balance for new signups (recommend: 0) and whether sample users exist on startup (recommend: 1 sample user for demo).

2. Add simple data/models (no DB, arrays only)
   1. Create model classes:
      - Customer: studentId/email, name, mobile, password, walletBalance
      - MenuItem (Food): id, name, shortDescription, price
      - CartLine: menuItemId, quantity, addOns (optional boolean flags for Egg/Coke)
      - Order: orderId, customerId, lines[], total, pickupTimeSlot, paymentMethod, status
   2. Create Manager classes using fixed-size arrays + `count`:
      - CustomerManager: addCustomer(), validateLogin(), findById(), addWallet(), deductWallet()
      - MenuManager: fixed list of 4 items, getById()
      - CartManager (per session): addItem(), updateQty(), clear(), calculateTotal()
      - OrderManager: placeOrder(), listOrders(), updateStatus()
      - InventoryManager (simple): track ingredient counts per menu item OR “stock per menu item” (simplest)
      - SalesManager: record totals per day/item for StaffStatistics
   3. Add AppSession/AppState singleton:
      - currentCustomerId
      - currentCart (CartManager)
      - selectedMenuItemId (set by CustomerMenuPanel buttons)

3. Customer validation flow (landing → login options → login/signup)
   1. CustomerLandingPage: keep 4s auto-redirect to CustomerLoginOptions.
   2. CustomerLoginOptions: keep buttons routing to CustomerLoginPanel / CustomerSignupPanel.
   3. CustomerLoginPanel:
      - Overlay `JTextField` for Student ID and `JPasswordField` for password aligned to the PNG inputs.
      - Wire the “Sign in” button (currently `jButton1`) to:
        - Read inputs → call CustomerManager.validateLogin()
        - On success: set AppSession.currentCustomerId, redirect to CustomerHomePagePanel
        - On failure: show message dialog and stay on page
      - Keep existing “Sign up!” navigation (`jButton2`) and back (`jButton3`).
   4. CustomerSignupPanel:
      - Overlay fields matching the PNG: Student ID, Name, Mobile, Password.
      - Wire “Register!” button to:
        - Validate required fields
        - Ensure Student ID unique
        - Create Customer (wallet starts 0) and add to CustomerManager
        - Redirect to CustomerRegistrationCompletePanel
      - Keep “Log in!” link (`jButton1`) navigation.

4. Customer homepage roles/actions (Menu → Food → Cart → Profile)
   1. CustomerHomePagePanel: keep as hub to Menu/Cart/Profile.
   2. CustomerMenuPanel:
      - Map the 4 food buttons (`jButton4`–`jButton7`) to 4 fixed MenuItems:
        - Set AppSession.selectedMenuItemId then redirect to CustomerShowFoodPanel.
   3. CustomerShowFoodPanel:
      - On open, load selected MenuItem from MenuManager.
      - “View description” requirement:
        - Easiest: when the user clicks the food/title area (or on open), show a dialog with the item description.
      - Add-to-cart (`jButton3`) should:
        - Read quantity (simple default 1, or add +/− buttons if you want)
        - Read add-ons (Egg/Coke) if implemented
        - Call CartManager.addItem(...)
        - Redirect to CustomerCartPanel
      - Back (`jButton4`) returns to CustomerMenuPanel.
   4. CustomerCartPanel:
      - Add an invisible “Place Order!” button over the yellow button in the PNG.
      - On “Place Order!” click:
        - Prompt for pickup slot (simple option dialog, e.g., 10:00/10:30/11:00…)
        - Prompt for payment method: Cash or Wallet
        - If Wallet: ensure walletBalance >= total; deduct; else error
        - Place order via OrderManager; clear cart
        - Show confirmation dialog (orderId + pickup slot + total)
      - Optional (only if desired): wire the +/− controls per item via additional invisible buttons; otherwise keep cart as “summary via dialogs”.
   5. CustomerProfilePanelVisible:
      - Overlay labels for customer name, mobile, and wallet balance (so it’s dynamic).
      - Cash in button (`jButton4`) → CustomerCashInPanel.
      - Scan QR (`jButton5`) → CustomerQrScannerPanel.
      - Generate QR (`jButton6`) → CustomerQrScanPanel.
   6. CustomerCashInPanel:
      - Overlay a label for Available Balance.
      - Add invisible buttons for quick cash (+20, +50, +100, +200) over the PNG buttons.
      - Overlay a `JTextField` for Enter Amount.
      - Add an invisible “Confirm cash in” click target (if the PNG has no explicit button, use Enter key on the field or clicking the amount area).
      - Update wallet via CustomerManager.addWallet() and refresh labels.
   7. CustomerQrScannerPanel:
      - Implement a simple “scan” simulation: input dialog for a code; validate; show result.
      - Option: treat QR as wallet cash-in voucher (adds fixed amount) to keep it useful without adding new payment flows.
   8. CustomerQrScanPanel:
      - Implement “generate QR”: show dialog with a generated code like `BB-{customerId}-{timestamp}`.

5. Staff validation + management flow
   1. StaffLandingPage: keep button → StaffPassword.
   2. StaffPassword:
      - Overlay either 4 small JTextFields or 1 JPasswordField limited to 4 digits, aligned to the four boxes.
      - Validate against hardcoded PIN (e.g., 1234) on click/enter.
      - Success → StaffOrders; failure → message dialog.
   3. StaffOrders / StaffMenu / StaffInventory:
      - Add a simple overlay component in the big blue content area:
        - A scrollable JTextArea or JTable showing current Orders/Menu/Inventory
        - 2–3 action buttons (Add/Edit/Delete or Mark Done) that use dialogs for inputs
      - All actions call the appropriate Manager methods.
   4. StaffStatistics:
      - Add a custom JPanel over the blue area that paints a very simple bar chart from SalesManager totals.
      - Keep it minimal (few bars, labeled), no external chart libraries.

6. OOP pillars comments + dedicated README
   1. In code: add short comments near the exact spots where each pillar is demonstrated:
      - Encapsulation: private arrays + public methods in Manager classes
      - Abstraction: UI forms call Manager methods (no loops/logic in UI)
      - Inheritance: all forms extend JFrame; model inheritance (optional) MenuItem base class if you add Food/Beverage
      - Polymorphism: optional—keep simple by using a base type (e.g., `MenuItem`) and having OrderManager accept it
   2. Add a new document `README_OOP_PILLARS.md` listing each pillar and the exact class/file locations where it appears.

**Relevant files**
- src/main/java/com/mycompany/barkbites/FormNavigator.java — keep redirect pattern
- src/main/java/com/mycompany/barkbites/BarkBites.java — app entry and window positioning
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerLoginPanel.java — add login inputs + validation
- src/main/java/com/mycompany/barkbites/CustomerForms/CustomerSignupPanel.java — add signup inputs + add-to-array
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
- New package: src/main/java/com/mycompany/barkbites/data (managers) and /models (POJOs)
- New doc: README_OOP_PILLARS.md

**Verification**
1. Run the app from BarkBites main and walk flows:
   - Customer: Landing → LoginOptions → Signup → RegistrationComplete → Home → Menu → ShowFood → AddToCart → Cart → PlaceOrder
   - Customer: Profile → CashIn quick buttons + manual amount → balance updates
   - Staff: Landing → Password (wrong pin blocked) → Orders → Menu/Inventory/Statistics
2. Confirm that: login blocks invalid users, signup persists in memory during runtime, wallet deductions occur for wallet payments, orders appear in StaffOrders, statistics bars change after orders.

**Decisions**
- Keep everything in-memory (no DB) and keep UI mostly unchanged (transparent overlays + dialogs).
- Prefer fixed-size arrays with counts to match “array” requirement and keep code simple.
- Use dialogs for pickup/payment selection to avoid heavy UI work.

**Further Considerations**
1. Confirm whether “Student ID” is the same as “email” for login, or if you want an actual email field.
2. Confirm whether QR should add wallet cash, or be used for payments (would add a third payment method).
