# BarkBites — Methods Summary


## Navigation & App Entry

- `FormNavigator.redirect(JFrame from, JFrame to)` — Smoothly transitions between frames, preserves position/state, and closes the source window.
- `BarkBites.main(String[] args)` — Application entry point that initializes look-and-feel and launches the UI.
- `BarkBites.positionSideBySide(JFrame leftFrame, JFrame rightFrame)` — Utility to place two frames side-by-side (used for demos or multi-window views).
- `BarkBites.setNimbusLookAndFeelIfAvailable()` — Sets Nimbus L&F if present on JVM.

## Staff Forms (primary UI screens)
Note: `initComponents()` and auto-generated small action handlers are excluded.

### StaffLandingPage
- `StaffLandingPage()` (constructor) — Prepares the landing page UI and attaches handlers.
- `openStaffPassword()` — Opens the staff PIN/password entry screen.
- `makeButtonInvisible(javax.swing.JButton button)` — Helper to hide buttons when appropriate.

### StaffPassword
- `StaffPassword()` (constructor) — Sets up the PIN entry UI.
- `setupPinFields()` — Configures PIN input fields (focus movement, filters).
- `setPinInputsEnabled(boolean enabled)` — Enable/disable PIN inputs while loading or verifying.
- `loadStaffPinAsync()` — Loads configured PIN asynchronously from backend/storage.
- `checkPinAndMaybeLogin()` — Verifies entered PIN and navigates to the staff UI if valid.
- `clearPin()` — Clears entered PIN digits.
- `makePinFieldTransparent(JTextField field)` — UI helper for pin field appearance.
- `configurePinField(JTextField field, JTextField next, JTextField prev)` — Wire up navigation between PIN fields.

### StaffCashIn
- `StaffCashIn()` (constructor) — Prepares Cash-In UI.
- `configureUi()` — Sets up UI controls, table model, and listeners.
- `loadUsersAsync()` — Loads list of users (wallets) asynchronously.
- `applySearchFilter()` — Filters table rows per search input.
- `populateEditorFromSelection()` — Populate editor fields from selected user row.
- `confirmCashIn()` — Validate and confirm cash-in action before committing.
- `performCashIn(CashInUser user, long cashInAmountCents)` — Performs the cash-in operation (backend call) asynchronously.
- `getSelectedUser()` — Returns currently selected `CashInUser` from table.
- `parseMoneyToCents(String amountText)` — Parses a user-entered currency string to cents (long).
- `clearEditorFields()` and `clearTableAndEditor()` — Helpers to reset UI state.
- `formatWallet(Long walletBalanceCents)` — Formats wallet balance to display string.

### StaffInventory
- `StaffInventory()` (constructor) — Sets up inventory management UI.
- `configureUi()` — Customize scrollbars, card UI, and form behavior.
- `loadInventoryAsync()` — Loads inventory items from backend asynchronously.
- `populateFormFromSelection()` — Fill the edit form with selected item values.
- `clearForm()` — Reset the edit form to empty values.
- `saveInventoryItem()` — Create/update inventory item (performs async save to DB).
- `deleteInventoryItem()` — Remove selected inventory item (async delete).
- `setBusy(boolean busy)` — Toggles busy indicator and disables UI when busy.
- `generateDocumentId()` — Helper to create new document IDs for items.
- `formatInventoryItem(StaffInventoryItem item)` — Format item summary for display.
- `openStaffOrders()`, `openStaffMenu()`, `openStaffStatistics()`, `openStaffHistory()`, `openStaffCashIn()` — Navigation helpers to switch screens.
- `logout()` — Log out current user / return to landing page.
- `makeButtonInvisible(javax.swing.JButton button)`, `makeFieldBackgroundInvisible(javax.swing.JTextField field)` — UI helpers.

### StaffMenu
- `StaffMenu()` (constructor) — Prepares menu CRUD UI for staff.
- `configureCrudUi()` — Configure cards view, editors, and controls.
- `loadMenuItemsAsync()` — Loads menu items from backend.
- `populateFormFromSelection()` — Fill menu item editor with selected item.
- `clearMenuForm()` — Reset menu edit form.
- `saveMenuItem()` — Create/update menu item in backend.
- `deleteSelectedMenuItem()` — Delete a selected menu item.
- `setBusy(boolean busy)` — Toggle busy indicator while saving/loading.
- `renderMenuCards()` — Render grid of product/menu cards in UI.
- `applyCard(StaffMenuItem item, JPanel panel, JLabel nameLabel, JLabel priceLabel, JLabel quantityLabel, JLabel imageLabel, int index)` — Populate a card UI with item data.
- `installCardClickTarget(Component component, int itemIndex)` — Attach click handler to card components.
- `selectMenuItem(StaffMenuItem item)` — Select an item for editing/viewing.
- `findSelectedMenuItem()` — Return currently selected `StaffMenuItem`.
- `formatPrice(long priceCents)` — Format price for display.
- `loadMenuImage(String imagePath, int width, int height)` and `resolveImageResource(String imagePath)` — Helpers to load/cached images for cards.
- `openStaffOrders()`, `openStaffInventory()`, `openStaffStatistics()`, `openStaffLandingPage()`, `openStaffHistory()`, `openStaffCashIn()` — Navigation helpers.
- `makeButtonInvisible(javax.swing.JButton button)`, `makeTextFieldInvisible(JTextField textField)` — UI helpers.

### StaffOrders
- `StaffOrders()` (constructor) — Prepares the orders UI and card view.
- `openStaffInventory()`, `openStaffMenu()`, `openStaffStatistics()`, `openStaffLandingPage()`, `openStaffHistory()`, `openStaffCashIn()` — Navigation helpers.
- `makeButtonInvisible(javax.swing.JButton button)` — UI helper.
- `configureOrderCards()` — Setup cards UI for orders and click handlers.
- `createCardLabel()` — Helper to create reusable JLabel for card UI.
- `loadOrdersAsync()` — Loads order records asynchronously.
- `renderOrderCards()` — Render order cards in the UI from loaded data.
- `selectCard(int cardIndex)` — Select a card in the UI.
- `updateSelectionStyles()` — Apply selection visuals to cards.
- `updateSelectedOrderStatusAsync()` — Change status of selected order via background task.
- `setOrderActionsEnabled(boolean enabled)` — Enable/disable order action controls.
- `safeText(String value)` — Null-safe text helper.

### StaffHistory
- `StaffHistory()` (constructor) — Prepares transaction history UI.
- `configureHistoryPanel()` — Setup table model and columns.
- `loadHistoryAsync()` — Loads history entries from backend asynchronously.
- `makeButtonInvisible(javax.swing.JButton button)` — UI helper.
- `openStaffOrders()`, `openStaffInventory()`, `openStaffMenu()`, `openStaffStatistics()`, `openStaffLandingPage()`, `openStaffCashIn()` — Navigation helpers.
- `formatTotal(long totalCents)` — Format total amounts for display.
- `formatCreatedAt(long createdAtMillis)` — Format epoch millis into human-readable date/time.
- `safeText(String value)` — Null-safe string helper.

### StaffStatistics
- `StaffStatistics()` (constructor) — Prepares the statistics/dashboard UI.
- `configureUi()` — Setup charts/labels and UI look.
- `loadSummaryAsync()` — Load daily/aggregate statistics asynchronously.
- `setBusy(boolean busy)` — Toggle busy indicator while loading.
- `formatPesos(long cents)` — Format money values into peso representation.
- Navigation helpers: `openStaffOrders()`, `openStaffInventory()`, `openStaffMenu()`, `openStaffLandingPage()`, `openStaffHistory()`, `openStaffCashIn()`.
- `makeButtonInvisible(javax.swing.JButton button)` — UI helper.

## Data & Services

## Customer Forms
Note: GUI-generated `initComponents()` and trivial auto-generated action stubs are omitted.

### CustomerCartPanel
- `CustomerCartPanel()` (constructor) — Cart UI showing selected items and totals.
- `CustomerCartPanel(String appliedVoucher)` — Constructor with pre-applied voucher code.
- `configureUi()` — Setup card panels and cart UI behaviors.
- `bringToFront(Component component)` — Bring a particular cart panel to front.
- `makeButtonInvisible(JButton button)` — UI helper for hiding unused buttons.
- `setEmptyState()` — Set the cart UI to an empty-state view.
- `clearPanel(JPanel panel, JLabel imageLabel, JLabel nameLabel, JLabel quantityLabel, JLabel priceLabel)` — Clear one card slot.
- `loadCartItems()` — Load cart items from backend/document into UI.
- `renderCart()` — Render cart items into the visible panels.
- `showPanel(JPanel panel, JLabel imageLabel, JLabel nameLabel, JLabel quantityLabel, JLabel priceLabel, CartItemData item)` — Populate a single card with CartItemData.
- `readOptionalDiscountCents(JsonNode document)` — Read discount cents from cart/source doc.
- `formatPrice(long priceCents)` — Format money for display in cart.
- `loadMenuImage(String imagePath, int width, int height)` and `resolveImageResource(String imagePath)` — Image helpers for product thumbnails.
- `submitCart()` — Submit the current cart to create an order.
- `clearAppliedVoucherState()` — Remove any applied voucher from UI state.
- `setAppliedVoucher(String voucherCode)` — Public setter to apply voucher code programmatically.
- `applyVoucherToCartAsync(String voucherCode)` — Validate/apply voucher asynchronously.

### CustomerCashInPanel
- `CustomerCashInPanel()` (constructor) — Cash-in UI for customers.
- `bringToFront(Component component)` — Bring a child component to the front.
- `makeButtonInvisible(JButton button)` — Helper to hide buttons.
- `makeIconButton(JButton button)` — Style helper for icon buttons.
- `makeTextFieldTransparent(JTextField field)` — UI appearance helper.
- `loadIcon(String resourcePath)` — Load small icon resources.
- `setBusy(boolean busy)` — Toggle busy indicator while loading/processing.
- `toggleWalletBalanceVisibility()` — Show/hide masked wallet balance.
- `updateWalletToggleButton()` — Update toggle button visuals/state.
- `refreshWalletLabel()` — Refresh displayed wallet amount.
- `formatWalletBalance(Long walletBalanceCents)` and `maskWalletBalance(Long walletBalanceCents)` — Formatting helpers.
- `parsePesosToCents(String input)` — Parse input to cents.
- `loadWalletBalance()` — Async load of wallet balance.
- `cashInTypedAmount()` — Handler when user types a custom cash-in amount.
- `cashInAmount(long amountCents)` — Execute cash-in operation (creates update request).

### CustomerCheckPhone
- `CustomerCheckPhone()` (constructor) — Phone code verification screen.
- `configureForm()` — Setup digit fields and form behavior.
- `bringToFront(Component component)` — Bring component forward in the layout.
- `makeButtonInvisible(JButton button)`, `makeTextFieldTransparent(JTextField field)` — UI helpers.
- `configureDigitField(JTextField field, JTextField next, JTextField previous)` — Wire digit fields for OTP input.
- `enteredCode()` — Read concatenated entered OTP digits.
- `attemptResetCode()` — Trigger resending a verification code.
- `clearCodeFields()` — Clear input fields.
- `startResendTimer()` / `stopResendTimer()` / `updateTimerLabel()` / `updateResendButtonState()` — Timer helpers for resend flow.
- `resendCode()` — Send a new verification code.

### CustomerLandingPage
- `CustomerLandingPage()` (constructor) — Landing page for customer flows (entry point to menu/cart).

### CustomerLoginOptions
- `CustomerLoginOptions()` (constructor) — Offers login choices to customer.

### CustomerHomePagePanel
- `CustomerHomePagePanel()` (constructor) — Main customer home UI.
- `configureUI()` — Setup menu/category click targets and dynamic UI.
- `loadOrderBannerVisibility()` — Async check whether to show order banner (pending orders).
- `bringToFront(Component component)` — Helper to bring components to front.
- `makePanelClickable(JPanel panel)` — Attach click handlers to a panel.

### CustomerLoginPanel
- `CustomerLoginPanel()` (constructor) — Login form for customers.
- `bringToFront(Component component)` — UI helper.
- `makeButtonInvisible(JButton button)`, `makeTextFieldTransparent(JTextField field)`, `makePasswordFieldTransparent(JPasswordField field)` — Visual helpers.
- `setBusy(boolean busy)` — Toggle busy state while authenticating.
- `attemptLogin()` — Perform login (runs async AuthSession retrieval).
- `friendlyAuthError(String message)` — Map backend messages to friendlier text.
- `togglePasswordVisibility()` / `updatePasswordToggleButton()` — Password visibility helpers.
- `loadIcon(String resourcePath)` — Icon loader.

### CustomerLoginPasswordRecovery
- `CustomerLoginPasswordRecovery()` (constructor) — Password recovery screen.
- `bringToFront(Component component)` — UI helper.
- `makeButtonInvisible(JButton button)` / `makeTextFieldTransparent(JTextField field)` — Visual helpers.
- `setBusy(boolean busy)` — Toggle busy indicator during network checks.
- `attemptEmailCheck()` — Verify email exists / send reset link.

### CustomerMenuPanel
- `CustomerMenuPanel()` (constructor) — Customer-facing menu browser.
- `configureMenuUi()` — Prepare card grid and scroller.
- `loadMenuItemsAsync()` — Load menu items asynchronously (reuses `StaffMenuItem`).
- `renderMenuCards()` — Render menu cards for customers.
- `applyCard(StaffMenuItem item, JPanel panel, JLabel imageLabel, JLabel nameLabel, JLabel priceLabel)` — Populate card UI.
- `formatPrice(long priceCents)` — Price formatting helper.
- `loadMenuImage(String imagePath, int width, int height)` / `resolveImageResource(String imagePath)` — Image helpers.

### CustomerOrderConfirmed
- `CustomerOrderConfirmed()` (constructor) — Success screen after an order is placed.
- `configureUi()` — Setup confirmation UI elements.

### CustomerOrderDetails
- `CustomerOrderDetails()` (constructor) — Order details and progress screen.
- `configureUi()` — Configure labels and progress visuals.
- `loadOrderStatusAsync()` — Periodically load order status asynchronously.
- `updateProgressByStatus(String status)` — Update progress UI based on status string.
- `hideProgressLabels()` — Hide progress UI elements.
- `readStatus(QueryDocumentSnapshot document)` — Helper to read status from Firestore doc.
- `isFinishedStatus(String status)` — Check if order is in finished state.

### CustomerPayment
- `CustomerPayment()` (constructor) — Payment screen for customers.
- `configureUi()` — Prepare payment method options and summary.
- `bringToFront(Component component)` — UI helper.
- `makeButtonInvisible(JButton button)` — UI helper.
- `setBusy(boolean busy)` — Toggle busy UI while processing payment.
- `processWalletPayment()` — Process payment using customer's wallet (SwingWorker).
- `readCartTotals(FirestoreRestClient firestore, AuthSession session)` — Read subtotal/discount/final totals from cart documents.
- `buildCustomerWalletUpdate(JsonNode customerDoc, long updatedWalletBalanceCents)` — Build JSON patch for wallet update.
- `buildOrderDocument(String orderId, String customerId, String customerName, long totalCents, String orderSummary)` — Assemble order document to save in DB.

### CustomerProfilePanelVisible
- `CustomerProfilePanelVisible()` (constructor) — Profile panel for visible/editable customer profile.
- `makeButtonInvisible(JButton button)` — UI helper.


### StaffInventoryService
- `getAllProducts()` / `getAllInventoryItems()` (depending on name) — Retrieve inventory list from backend (Firestore).
- `getProductById(String id)` — Retrieve single item by id.
- `saveInventoryItem(...)` — Persist new or updated inventory item.
- `deleteInventoryItem(...)` — Delete inventory item by id.

### StaffMenuService
- `getAllMenuItems()` — Retrieve menu items.
- `saveMenuItem(...)` — Persist menu item.
- `deleteMenuItem(...)` — Delete menu item.

### StaffOrderService / StaffOrderRecord
- `getPendingOrders()` / `listOrders()` — Retrieve orders for staff view.
- `updateOrderStatus(orderId, newStatus)` — Update order status (e.g., fulfilled).

### StaffStatisticsService / StaffStatisticsSummary
- `computeDailyRevenue()` / `fetchSummary()` — Aggregate sales and compute totals for dashboard.

### Model records/classes
- `StaffInventoryItem` — fields and simple helpers to represent an inventory doc.
- `StaffMenuItem` — menu product model with fields like name, price, quantity, image path.
- `StaffOrderRecord` — order model: items, status, timestamps.
- `AuthSession` record — `AuthSession(String uid, String idToken)` represents signed-in session.

## Utilities / Helpers

- `safeText(String)` — Null-safe helper appears in multiple classes to avoid NPE when rendering labels.
- `formatPrice`, `formatPesos`, `formatTotal`, `formatCreatedAt` — Money/date formatting utilities used across screens.
- `generateDocumentId()` — Small generator helper used for Firestore document ids.
- Image/resource helpers: `loadMenuImage`, `resolveImageResource`.

## Differences vs attached UML diagram

- UML Entities: `Product`, `FoodItem`, `BeverageItem`, `Order`, `Payment`, `InventoryManager`, `OrderManager`, `CustomerGUI`, `StaffGUI`.
- Actual code: domain objects are split into `StaffMenuItem` / `StaffInventoryItem` / `StaffOrderRecord` and services such as `StaffMenuService`, `StaffInventoryService`, `StaffOrderService`. There is no single `InventoryManager` or `OrderManager` class — instead logic is implemented in service classes and UI screens.
- `Payment` class (with `processPayment()`) does not appear in the codebase; payment flows are likely handled externally or via Firestore documents.
- The UML's `CustomerGUI` is represented by `CustomerForms` (e.g., `CustomerCartPanel`) and many of the UI helpers (image loading, formatPrice) are implemented there.
- Additional changes observed vs UML:
  - Extensive use of SwingWorkers for async loads (`loadXAsync()` methods) to keep UI responsive.
  - Card-based menu rendering with image loading and click handlers (methods like `renderMenuCards`, `applyCard`).
  - Navigation split into small screen-specific `openStaffX()` helpers rather than a centralized `OrderManager`/`InventoryManager` controller visible in UML.
  - Utilities for formatting monetary values and safe text helpers are centralized in multiple classes.

If you want a side-by-side mapping for every UML method → implementation, I can produce a table mapping UML methods to the exact Java method names and files.

---

If you'd like, I can:
- produce a more exhaustive file-by-file method list (including signatures and line links), or
- create a UML-like diagram updated from the current code, or
- generate the side-by-side mapping table from the UML to the code.
